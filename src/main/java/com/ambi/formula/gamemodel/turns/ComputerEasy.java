package com.ambi.formula.gamemodel.turns;

import java.util.List;

import com.ambi.formula.gamemodel.GameModel;
import com.ambi.formula.gamemodel.datamodel.Formula;
import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Segment;
import com.ambi.formula.gamemodel.utils.Calc;

/**
 * This class holds the data and information about objects which are painted.
 *
 * @author Jiri Ambroz
 */
public class ComputerEasy extends ComputerTurnCore {

    private Formula comp;
    private final GameModel model;

    public ComputerEasy(GameModel model) {
        this.model = model;
    }

    @Override
    public void reset() {
        setCheckLinesIndex(0);
    }

    /**
     * Metoda vytvori tah pocitace. Prochazi se dobre moznosti a vybere se ta, ktera ma
     * nejvzdalenejsi kolizni prusecik. Prochazeni hran trati je postupne od startu k cili (stridani
     * leve a prave). Metoda vraci bod, kam pocitac tahne
     *
     * @return Point of next computer turn
     */
    @Override
    public Point selectComputerTurn() {
        comp = model.getTurnMaker().getFormula(model.getTurnMaker().getActID());
        List<Segment> checkLines = model.getAnalyzer().getCheckLines();
        int pointCount = model.getTurnMaker().getTurns().getFreePoints().size();
        Point farestCollisionPoint = new Point();
        int minLimit = 5000;

        if (pointCount > 0) {
            //computer has at least 1 clean turn
            int newIndex = getCheckLinesIndex();
            double maxDist = 0;
            double finishDist = minLimit;
            //________________ ZARAZENI JEDNE MOZNOSTI TAHU ___________________
            for (int i = 0; i < pointCount; i++) {
                Point actPoint = model.getTurnMaker().getTurns().getFreePoints().get(i);
                //timto tahem protne pocitac cil:
                if (actPoint.getLocation().equals(Point.FINISH)) {
                    farestCollisionPoint = actPoint;
                    break;
                } //timto tahem pocitac skonci na cilove care:
                else if (actPoint.getLocation().equals(Point.FINISH_LINE)) {
                    finishDist = 0;//uprednostneni tohoto tahu pred tim, ktery by skoncil pred cilem
                    farestCollisionPoint = actPoint;
                } //bezny tah pocitace nekde na trati:
                else if (finishDist == minLimit) {
                    boolean search = true;
                    //nalezeni nejblizsi prujezdove usecky smerem vpredu:
                    int k = 1;
                    int basic = 20;
                    while (search == true) {
                        if ((int) Calc.crossing(comp.getLast(), actPoint, checkLines.get(getCheckLinesIndex() + k))[0] != Calc.OUTSIDE) {
                            k++;
                            basic = basic + 20;
                        } else {
                            search = false;
                        }
                    }
                    //____________________________________________________________
                    //kontrola, zda dalsim tahem formule nenaboura do nejblizsi steny
                    if (checkColision(checkLines.get(getCheckLinesIndex() + k - 1), checkLines.get(getCheckLinesIndex() + k), actPoint) == false) {
                        //vzdalelnosti k pomocnym bodum na trati:
                        double[] dist = lineDist(getCheckLinesIndex() + k, actPoint, checkLines);
                        //brzdna draha formule:
                        int breakDist = brakingDist(actPoint);
                        //kontrola, zda to ubrzdi do dalsi usecky, pokud ano, tak tam muze jet
                        if (getCheckLinesIndex() + k == checkLines.size() - 1) {
                            //pristi C H E C K L I N E  J E  C I L
                            if (basic - dist[2] > maxDist) {
                                maxDist = basic - dist[2];
                                farestCollisionPoint = actPoint;
                                if ((int) Calc.crossing(comp.getLast(), farestCollisionPoint, checkLines.get(getCheckLinesIndex() + k - 1))[0] != Calc.OUTSIDE) {
                                    newIndex = getCheckLinesIndex() + k - 1;
                                }
                            }
                        } else {//nasledujici checkLine neni cil
                            if ((dist[0] >= breakDist || dist[1] >= breakDist) && basic - dist[2] > maxDist) {
                                //pocitac zabrzdi
                                maxDist = basic - dist[2];
                                if (k > 1) {
                                    newIndex = getCheckLinesIndex() + k - 1;
                                }
                                farestCollisionPoint = actPoint;
                            }
                        }
                    }//_________________________________________________________
                }
            }
            if (farestCollisionPoint.isEqual(new Point())) {
                //zadny z moznych tahu neni dobry a vybere se "nejlepsi z horsich"
                farestCollisionPoint = Calc.findNearestPoint(comp.getLast(), model.getTurnMaker().getTurns().getFreePoints());
                if ((int) Calc.crossing(comp.getLast(), farestCollisionPoint, checkLines.get(getCheckLinesIndex() + 1))[0] != Calc.OUTSIDE) {
                    newIndex = getCheckLinesIndex() + 1;
                }
            }
            setCheckLinesIndex(newIndex);
        } else {
            //computer does not have other possibility then to crash
            farestCollisionPoint = Calc.findNearestPoint(comp.getLast(), model.getTurnMaker().getTurns().getCollisionPoints());
        }
        return farestCollisionPoint;
    }

    /**
     * It calculates coordinations of point where formula stops in slower direction and finds out if
     * this point is in front of the given segment or behind.
     *
     * @prev is last checkline which where passed
     * @next is following non passed checkline
     * @actPoint is point where computer wants to go
     * @return true if computer will crash into the barier, false otherwise
     */
    private boolean checkColision(Segment prev, Segment next, Point actPoint) {
        boolean intersect = false;
        int moveX = comp.getSide(actPoint); //posun na ose X pri zabrzdeni o 1
        int moveY = comp.getSpeed(actPoint);//posun na ose Y pri zabrzdeni o 1
        //cyklus bezi dokud jedna rychlost nebude 0
        while (intersect == false) {
            //posun X:
            if (moveX < 0) {
                moveX++;
            } else {
                moveX--;
            }
            //posun Y:
            if (moveY < 0) {
                moveY++;
            } else {
                moveY--;
            }
            if (moveX == 0 || moveY == 0) {
                intersect = true;
            }
            actPoint = new Point(actPoint.x + moveX, actPoint.y + moveY);
        }
        intersect = false;
        //kontrola srazky s levou hranou:
        if (prev.getFirst().isEqual(next.getFirst()) == false) {
            if ((int) Calc.crossing(comp.getLast(), actPoint, prev.getFirst(), next.getFirst())[0] != Calc.OUTSIDE) {
                intersect = true;
            }
        }
        //kontrola srazky s pravou hranou:
        if (intersect == false && prev.getLast().isEqual(next.getLast()) == false) {
            if ((int) Calc.crossing(comp.getLast(), actPoint, prev.getLast(), next.getLast())[0] != Calc.OUTSIDE) {
                intersect = true;
            }
        }
        return intersect;
    }

    /**
     * It returns braking distance of computer when it will go to given point 'turn'. E.g. when
     * actual speed is 5, then braking distance is 4+3+2 = 9.
     *
     * @param turn is point where computer wants to go
     * @return approximation of braking distance
     */
    private int brakingDist(Point turn) {
        int dist = 0;
        int speed = comp.maxSpeed(turn);
        while (speed > 1) {
            speed--;
            dist = dist + speed;
        }
        return dist;
    }

    /**
     * Metoda pocita vzdalenosti ke dvema nasledujicim prujezdovym useckam a vzdalenost ke stredu
     * mezi nimi.
     *
     * @param actIndex index nejblizsi neprotnute checkLine
     * @param click bod, kam hrac jede s formuli
     * @return
     */
    private double[] lineDist(int actIndex, Point click, List<Segment> checkLines) {
        Segment actLine = checkLines.get(actIndex);
        Segment nextLine;
        if (actIndex + 1 > checkLines.size() - 1) {
            //nasledujici linie byla cil, takze dalsi by byla mimo rozsah
            nextLine = actLine;
        } else {
            nextLine = checkLines.get(actIndex + 1);
        }
        Point mid1 = actLine.getMidPoint();
        Point mid2 = nextLine.getMidPoint();
        //prostredni bod mezi dvema nasledujicimi checkLines:
        Point midPoint = new Segment(mid1, mid2).getMidPoint();
        //prevladajici smer jizdy (side - smer X nebo speed - smer Y):
        String direct = comp.maxDirect(click);
        double midDist = Calc.distance(midPoint, click);
        //formule je rychlejsi ve vertikalnim smeru - osa Y
        if (direct.equals(Formula.FORWARD)) {
            double dist1 = Math.floor(Math.abs((click.getY() - mid1.getY())));
            double dist2 = Math.floor(Math.abs((click.getY() - mid2.getY())));
            //kontrola, zda stred blizsi linie neni v protismeru:
            if ((click.getY() > comp.getLast().getY() && mid1.getY() >= click.getY())
                    || (click.getY() < comp.getLast().getY() && mid1.getY() <= click.getY())) {
            } else {
                dist1 = -dist1;
            }
            //kontrola, zda stred vzdalenejsi linie neni v protismeru:
            if ((click.getY() > comp.getLast().getY() && mid2.getY() >= click.getY())
                    || (click.getY() < comp.getLast().getY() && mid2.getY() <= click.getY())) {
            } else {
                dist2 = -dist2;
            }
            double[] result = {dist1, dist2, midDist};
            return result;
        } //formule je rychlejsi v horizontalnim smeru - osa X
        else {
            double dist1 = Math.floor(Math.abs((click.getX() - mid1.getX())));
            double dist2 = Math.floor(Math.abs((click.getX() - mid2.getX())));
            //kontrola, zda stred blizsi linie neni v protismeru:
            if ((click.getX() > comp.getLast().getX() && mid1.getX() >= click.getX())
                    || (click.getX() < comp.getLast().getX() && mid1.getX() <= click.getX())) {
            } else {
                dist1 = -dist1;
            }
            //kontrola, zda stred vzdalenejsi linie neni v protismeru:
            if ((click.getX() > comp.getLast().getX() && mid2.getX() >= click.getX())
                    || (click.getX() < comp.getLast().getX() && mid2.getX() <= click.getX())) {
            } else {
                dist2 = -dist2;
            }
            double[] result = {dist1, dist2, midDist};
            return result;
        }
    }

}
