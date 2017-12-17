package com.ambi.formula.gamemodel;

import java.util.List;

import com.ambi.formula.gamemodel.datamodel.Formula;
import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Polyline;
import com.ambi.formula.gamemodel.utils.Calc;

/**
 * This class holds the data and information about objects which are painted.
 *
 * @author Jiri Ambroz
 */
public class CompSimul {

    private Formula f2;
    private final GameModel model;
    private int trackIndex;

    public CompSimul(GameModel model) {
        this.model = model;
        trackIndex = 0;
    }

    /**
     * Metoda vytvori tah pocitace. Prochazi se dobre moznosti a vybere se ta,
     * ktera ma nejvzdalenejsi kolizni prusecik. Prochazeni hran trati je
     * postupne od startu k cili (stridani leve a prave). Metoda vraci bod, kam
     * pocitac tahne
     *
     * @return Point of next computer turn
     */
    public Point compTurn() {
        f2 = model.getTurn().getFormula(2);
        List<Polyline> checkLines = model.getCheckLines();
        int pointCount = model.getTurns().getPoints().getLength();
        Point farestPoint = new Point();//bod s nejvzdalenejsi kolizi
        int minLimit = 5000;

        if (pointCount > 0) { //pocitac ma alespon 1 dobry tah
            int newIndex = trackIndex;
            double maxDist = 0;
            double finishDist = minLimit;
            //________________ ZARAZENI JEDNE MOZNOSTI TAHU ___________________
            for (int i = 0; i < pointCount; i++) {
                Point actPoint = model.getTurns().getPoints().getPoint(i);
                //timto tahem protne pocitac cil:
                if (actPoint.getPosition().equals("finish")) {
                    farestPoint = actPoint;
                    break;
                } //timto tahem pocitac skonci na cilove care:
                else if (actPoint.getPosition().equals("finishLine")) {
                    finishDist = 0;//uprednostneni tohoto tahu pred tim, ktery by skoncil pred cilem
                    farestPoint = actPoint;
                } //bezny tah pocitace nekde na trati:
                else if (finishDist == minLimit) {
                    boolean search = true;
                    //nalezeni nejblizsi prujezdove usecky smerem vpredu:
                    int k = 1;
                    int basic = 20;
                    while (search == true) {
                        if ((int) Calc.crossing(f2.getLast(), actPoint, checkLines.get(trackIndex + k))[0] != -1) {
                            k++;
                            basic = basic + 20;
                        } else {
                            search = false;
                        }
                    }
                    //____________________________________________________________
                    //kontrola, zda dalsim tahem formule nenaboura do nejblizsi steny
                    if (checkColision(checkLines.get(trackIndex + k - 1), checkLines.get(trackIndex + k), actPoint) == false) {
                        //vzdalelnosti k pomocnym bodum na trati:
                        double[] dist = lineDist(trackIndex + k, actPoint, checkLines);
                        //brzdna draha formule:
                        int breakDist = brakingDist(actPoint);
                        //kontrola, zda to ubrzdi do dalsi usecky, pokud ano, tak tam muze jet
                        if (trackIndex + k == checkLines.size() - 1) {
                            //pristi C H E C K L I N E  J E  C I L
                            if (basic - dist[2] > maxDist) {
                                maxDist = basic - dist[2];
                                farestPoint = actPoint;
                                if ((int) Calc.crossing(f2.getLast(), farestPoint, checkLines.get(trackIndex + k - 1))[0] != -1) {
                                    newIndex = trackIndex + k - 1;
                                }
                            }
                        } else {//nasledujici checkLine neni cil
                            if ((dist[0] >= breakDist || dist[1] >= breakDist) && basic - dist[2] > maxDist) {
                                //pocitac zabrzdi
                                maxDist = basic - dist[2];
                                if (k > 1) {
                                    newIndex = trackIndex + k - 1;
                                }
                                farestPoint = actPoint;
                            }
                        }
                    }//_________________________________________________________
                }
            }
            if (farestPoint.isEqual(new Point())) {
                //zadny z moznych tahu neni dobry a vybere se "nejlepsi z horsich"
                System.out.println("zadna moznost nevyhovuje");
                farestPoint = (Point) Calc.findNearest(f2.getLast(), model.getTurns().getPoints()).get(1);
                if ((int) Calc.crossing(f2.getLast(), farestPoint, checkLines.get(trackIndex + 1))[0] != -1) {
                    newIndex = trackIndex + 1;
                }
            }
            trackIndex = newIndex;
        } else { //pocitac nema zadny dobry tah a boura
            farestPoint = (Point) Calc.findNearest(f2.getLast(), model.getTurns().getBadPoints()).get(1);
        }
        return farestPoint;
    }

    private boolean checkColision(Polyline prev, Polyline next, Point actPoint) {
        /* Metoda vypocte souradnice bodu, na kterem formule zabrzdi v pomalejsim smeru
         * a zjisti, zda to ubrzdi vcas pred krajnici.
         * vstup: prev je posledni checkline, ktera byla projeta
         *        next je nasledujici neprojeta checkline
         *        actPoint je bod, kam by formule jela
         * Metoda vraci true, pokud nastane kolize a formule to neubrzdi */
        boolean intersect = false;
        int moveX = f2.getSide(actPoint); //posun na ose X pri zabrzdeni o 1
        int moveY = f2.getSpeed(actPoint);//posun na ose Y pri zabrzdeni o 1
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
        if (prev.getPreLast().isEqual(next.getPreLast()) == false) {
            if ((int) Calc.crossing(f2.getLast(), actPoint, prev.getPreLast(), next.getPreLast())[0] != -1) {
                intersect = true;
            }
        }
        //kontrola srazky s pravou hranou:
        if (intersect == false && prev.getLast().isEqual(next.getLast()) == false) {
            if ((int) Calc.crossing(f2.getLast(), actPoint, prev.getLast(), next.getLast())[0] != -1) {
                intersect = true;
            }
        }
        return intersect;
    }

    private int brakingDist(Point turn) {
        /* metoda vraci brzdnou drahu formule f2 pri tahu turn
         * pri rychlosti 5 je brzdna draha = 4+3+2 = 9 */
        int dist = 0;
        int speed = f2.maxSpeed(turn);
        while (speed > 1) {
            speed--;
            dist = dist + speed;
        }
        return dist;
    }

    /**
     * Metoda pocita vzdalenosti ke dvema nasledujicim prujezdovym useckam a
     * vzdalenost ke stredu mezi nimi.
     *
     * @param actIndex index nejblizsi neprotnute checkLine
     * @param click bod, kam hrac jede s formuli
     * @return
     */
    private double[] lineDist(int actIndex, Point click, List<Polyline> checkLines) {
        Polyline actLine = checkLines.get(actIndex);
        Polyline nextLine;
        if (actIndex + 1 > checkLines.size() - 1) {
            //nasledujici linie byla cil, takze dalsi by byla mimo rozsah
            nextLine = actLine;
        } else {
            nextLine = checkLines.get(actIndex + 1);
        }
        Point mid1 = actLine.getMidPoint();
        Point mid2 = nextLine.getMidPoint();
        //prostredni bod mezi dvema nasledujicimi checkLines:
        Point midPoint = new Polyline(mid1, mid2).getMidPoint();
        //prevladajici smer jizdy (side - smer X nebo speed - smer Y):
        String direct = f2.maxDirect(click);
        double midDist = Calc.dist(midPoint, click);
        //formule je rychlejsi ve vertikalnim smeru - osa Y
        if (direct.equals("speed")) {
            double dist1 = Math.floor(Math.abs((click.getY() - mid1.getY())));
            double dist2 = Math.floor(Math.abs((click.getY() - mid2.getY())));
            //kontrola, zda stred blizsi linie neni v protismeru:
            if ((click.getY() > f2.getLast().getY() && mid1.getY() >= click.getY())
                    || (click.getY() < f2.getLast().getY() && mid1.getY() <= click.getY())) {
            } else {
                dist1 = -dist1;
            }
            //kontrola, zda stred vzdalenejsi linie neni v protismeru:
            if ((click.getY() > f2.getLast().getY() && mid2.getY() >= click.getY())
                    || (click.getY() < f2.getLast().getY() && mid2.getY() <= click.getY())) {
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
            if ((click.getX() > f2.getLast().getX() && mid1.getX() >= click.getX())
                    || (click.getX() < f2.getLast().getX() && mid1.getX() <= click.getX())) {
            } else {
                dist1 = -dist1;
            }
            //kontrola, zda stred vzdalenejsi linie neni v protismeru:
            if ((click.getX() > f2.getLast().getX() && mid2.getX() >= click.getX())
                    || (click.getX() < f2.getLast().getX() && mid2.getX() <= click.getX())) {
            } else {
                dist2 = -dist2;
            }
            double[] result = {dist1, dist2, midDist};
            return result;
        }
    }

    public void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }

}
