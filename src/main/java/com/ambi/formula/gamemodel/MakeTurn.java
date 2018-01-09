package com.ambi.formula.gamemodel;

import java.util.HashMap;
import java.util.List;

import com.ambi.formula.gamemodel.datamodel.Formula;
import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Polyline;
import com.ambi.formula.gamemodel.datamodel.Track;
import com.ambi.formula.gamemodel.datamodel.Turns;
import com.ambi.formula.gamemodel.labels.HintLabels;
import com.ambi.formula.gamemodel.utils.Calc;

/**
 *
 * @author Jiri Ambroz
 */
public class MakeTurn {

    public static final int WIN_COLLISION = 0;
    public static final int WIN_FIRST = 1;
    public static final int WIN_LAST_TURN = 2;

    public static final int FOUR_TURNS = 4;
    public static final int FIVE_TURNS = 5;
    public static final int NINE_TURNS = 9;

    public static final int LENGTH_3 = 3;
    public static final int LENGTH_5 = 5;
    public static final int LENGTH_10 = 10;
    public static final int LENGTH_20 = 20;
    public static final int LENGTH_MAX = 999;

    private final GameModel model;
    private final HashMap<Integer, Formula> racers;
    private Turns turns;
    private int actID;
    private int rivalID;
    private int finishType;
    private int turnsCount;
    private int lengthHist;

    public MakeTurn(GameModel menu) {
        this.model = menu;
        racers = new HashMap<>();
        racers.put(1, new Formula());
        racers.put(2, new Formula());
        turns = new Turns();
        actID = 1;
        rivalID = 2;
        lengthHist = LENGTH_MAX;
        finishType = WIN_FIRST;
        turnsCount = FOUR_TURNS;
    }

    /**
     * Hru hraji dva hraci - fAct je na tahu a fRival je formule soupere podle aktualni faze hry se
     * provede prislusna akce metoda vytvori tah hrace a vykresli moznosti tahu pro soupere.
     *
     * @param click is point where player clicked
     */
    public void turn(Point click) {
        Formula act = racers.get(actID);

        //--------------------- ZAHAJOVACI TAH --------------------------
        switch (model.getStage()) {
            case GameModel.FIRST_TURN: {
                firstTurn(click);
                break;
            } // ------------------------ NORMALNI TAH HRACE ------------------------
            case GameModel.NORMAL_TURN: {
                model.fireHint(HintLabels.EMPTY);
                List<Turns.Turn> points = getTurns().getFreeTurns();
                // prochazeni moznych tahu
                for (int i = 0; i < points.size(); i++) {
                    //uzivatel klikl na jeden z moznych tahu:
                    if (click.isEqual(points.get(i).getPoint())) {
                        act.addPoint(click);
                        act.movesUp();
                        //hrac protne cilovou caru:
                        if (points.get(i).getPoint().getLocation().contains(Point.FINISH)
                                && Track.LEFT == Calc.sidePosition(click, model.getBuilder().getFinish())) {
                            //nastaveni informaci o jizde:
                            act.lengthUp(act.getPreLast(), points.get(i).getCollision());
                            act.setWin(true);//hrac je v cili
                            waitTurn(act, "interFinish");
                        }// hrac skonci svuj tah na cilove care
                        else if (Point.FINISH_LINE.equals(points.get(i).getPoint().getLocation())) {
                            //nastaveni informaci o vzdalenosti:
                            act.lengthUp();
                            waitTurn(act, "normal");
                        } else { //normalni tah
                            //informace o vzdalenosti
                            act.lengthUp();
                            waitTurn(act, "normal");
                        }
                    } // konec if kliknu na jeden z bodu points
                }// konec prochazeni bezkoliznich moznosti
                // ------------------- HRAC SE VYBOURAL --------------------------
                for (int i = 0; i < getTurns().getCollisionTurns().size(); i++) {
                    //hrac 1 klikl na jeden z koliznich tahu:
                    if (click.isEqual(getTurns().getCollisionTurns().get(i).getPoint())) {
                        act.movesUp();
                        int maxSpeed = act.maxSpeed(click);
                        act.setWait(maxSpeed + 1);
                        model.fireCrash(maxSpeed);
                        //nastaveni poctu tahu hrace:
                        act.movesUp(maxSpeed);
                        //pridani pruseciku do tahu formule:
                        act.addPoint(getTurns().getCollisionTurns().get(i).getCollision());
                        act.lengthUp();
                        if (getFinishType() == WIN_COLLISION) {//konec hry po kolizi
                            racers.get(rivalID).setWin(true);
                            model.winnerAnnouncement();
                        } else {
                            // pokud hrac 2 take boural, zjisti se kdo vyjede driv
                            waitTurn(act, "bothCrash");//prida novy stred a prida ujetou vzdalenost
                        }
                    }
                } //konec prochazeni koliznich bodu
                break;
            } //--------------- AUTOMATICKY TAH - NARAZ ------------------------------
            case GameModel.AUTO_CRASH: {
                //vsechny spatne body jsou mimo okno = naraz
                //existuje prusecik s krajnici a hrac boura:
                act.movesUp();
                int maxSpeed = act.maxSpeed(Calc.findNearestPoint(act.getLast(), getTurns().getCollisionPoints()));
                act.setWait(maxSpeed + 1);
                model.fireCrash(maxSpeed);
                //nastaveni poctu tahu hrace:
                act.movesUp(maxSpeed);
                //pridani pruseciku do tahu formule:
                int closestIndex = Calc.findNearestIndex(act.getLast(), getTurns().getCollisionPoints());//najde nejkratsi tah
                act.addPoint(getTurns().getCollisionTurns().get(closestIndex).getCollision());
                act.lengthUp();
                //moznosti tahu:
                model.setStage(GameModel.NORMAL_TURN);
                if (getFinishType() == WIN_COLLISION) {//konec hry po kolizi
                    racers.get(rivalID).setWin(true);
                    model.winnerAnnouncement();
                } else {
                    // pokud hrac 2 take boural, zjisti se kdo vyjede driv
//                    model.setStage(6);
                    waitTurn(act, "bothCrash");//prida novy stred a prida ujetou vzdalenost
                }
                break;
            } //--------------- AUTOMATICKY TAH - PROJETI CILE ---------------------
            case GameModel.AUTO_FINISH: {
                //vsechny dobre tahy nejsou videt - projeti startem:
                int closestIndex = Calc.findNearestIndex(act.getLast(), getTurns().getFreePoints());
                act.addPoint(Calc.findNearestPoint(act.getLast(), getTurns().getFreePoints()));
                act.lengthUp(getTurns().getFreeTurns().get(closestIndex).getCollision(), act.getPreLast());
                act.movesUp();
                act.setWin(true);
                if (getActID() == 1) {//pokud hraje prvni hrac, da jeste sanci souperi na posledni tah
                    model.setStage(GameModel.NORMAL_TURN);
                    waitTurn(act, "interFinish");
                }
                model.checkWinner();
                break;
            }
            default:
                break;
        }
    }

    private void firstTurn(Point click) {
        Formula act = racers.get(actID);
        Polyline points = model.getBuilder().getPoints();
        for (int i = 0; i < points.getLength(); i++) {
            //uzivatel klikl na jeden z moznych tahu:
            if (click.isEqual(points.getPoint(i))) {
                act.addPoint(click);
                act.addPoint(new Point(click.x + act.getSide(), click.y + act.getSpeed()));
                //kdyz hraji dva hraci, smaze se startovni pozice prvniho
                if (getActID() == 1) {
                    points.removePoint(i);//druhy hrac si tuto pozici jiz nemuze vybrat
                } else {
                    points.clear();
                    model.setStage(GameModel.NORMAL_TURN);
                    nextTurn(rivalID, act.getLast());
                }
                rivalTurns();
            }
        }
    }

    public void rivalTurns() {
        //metoda, ktera nastavi, ze je na rade souper
        setID(rivalID, actID);
    }

    public void nextTurn(int formOnTurn, Point rivalLast) {
        /* Metoda, ktera vytvori moznosti dalsiho tahu (4 body).
         * Vola se na konci tahu hrace a vytvari moznosti tahu pro soupere - nikoliv
         * pro sebe. Tzn. ze formOnTurn udava pro koho se moznosti pocitaji. */
        Formula act = racers.get(formOnTurn);
        int side = act.getSide();
        int speed = act.getSpeed();

        //souradnice noveho stredu
        int cenX = act.getLast().getX() + side;
        int cenY = act.getLast().getY() + speed;
        Point center = new Point(cenX, cenY);//stred moznosti

        //create possibilities of next turn
        createTurns(center, false);
        divideTurns(rivalLast);

        //kontrola, jestli se mozne tahy nenachazeji mimo viditelnou oblast
        int turnLast = model.getPaper().outPaperNumber(getTurns().getFreePoints());//pocet moznosti koncici za cilem
        int turnOut = model.getPaper().outPaperNumber(getTurns().getCollisionPoints());//pocet moznosti koncici narazem

        //vsechny moznosti druheho hrace jsou mimo viditelnou oblast:
        // ----------------------- AUTOMATICKY TAH -------------------------
        //pocet moznych tahu: 4,5,9
        if ((turnOut == FOUR_TURNS && turnsCount == FOUR_TURNS) || (turnOut == FIVE_TURNS && turnsCount == FIVE_TURNS)
                || (turnOut == NINE_TURNS && turnsCount == NINE_TURNS)) {//vsechny moznosti zpusobi naraz
            model.setStage(GameModel.AUTO_CRASH);
            model.fireHint(HintLabels.NEXT_CLOSE_TURN);
        } else if (turnLast > 0 && turnLast == getTurns().getFreePoints().size()) {
            //zadna dobra moznost neni videt - tah na nejblizsi
            model.setStage(GameModel.AUTO_FINISH);
            model.fireHint(HintLabels.NEXT_CLOSE_TURN);
        }
    }

    /**
     * Metoda najde novy stred po havarii a vykresli nove moznosti novy stred je prunikem kolmice
     * kolizni hrany a kruznice se stredem v miste kolize a polomerem 0.6*velikost mrizky, pricemz
     * bod musi lezet na trati colision je bod, ve kterem doslo k vyjeti z trati.
     */
    private void crashTurn() {
        Formula act = racers.get(actID);
        Point crashCenter = new Point();
        Polyline collisionLine = act.getColision();

        //smerovy vektor kolizni usecky, ktery se vyuzije pro urceni kolmice
        double ux = collisionLine.getLast().x - collisionLine.getPreLast().x;
        double uy = collisionLine.getLast().y - collisionLine.getPreLast().y;

        if (ux == 0) {

            //crash into vertical edge - for quadratic equation bellow it has no solution
            if (Point.COLLISION_LEFT.equals(act.getLast().getLocation()) && uy > 0
                    || Point.COLLISION_RIGHT.equals(act.getLast().getLocation()) && uy < 0) {
                crashCenter = new Point(act.getLast().getX() - 1, act.getLast().getY());
            } else {
                crashCenter = new Point(act.getLast().getX() + 1, act.getLast().getY());
            }

        } else {
            //parametr c pro kolmici na kolizni usecku, prochazejici prusecikem:
            double C = -ux * act.getLast().x - uy * act.getLast().y;
            /* rovnice pro X na kolmici: X = (-uy*Y-c)/ux
             * stredova rovnice kruznice: r^2=(x-m)^2 + (y-n)^2
             * po dosazeni X do stredove rovnice:
             * (ux^2 + uy^2)* Y^2 + (2*uy*C + 2*m*uy*ux - 2*n*ux^2) * Y + ( C^2 + 2*C*m*ux + ux^2*(n^2+m^2-r^2) ) = 0
             */
            double m = act.getLast().x; //X stredu kruznice
            double n = act.getLast().y; //Y stredu kruznice
            //parametry kvadraticke rovnice:
            double a = ux * ux + uy * uy;
            double b = 2 * uy * C + 2 * m * uy * ux - 2 * n * ux * ux;
            double c = C * C + 2 * C * m * ux + ux * ux * (n * n + m * m - Math.pow(0.75, 2));
            //ziskani korenu Y1 a Y2:
            List<Double> quadRes = Calc.quadratic(a, b, c);
            double Y1 = quadRes.get(0);
            double Y2 = quadRes.get(1);
            //vypocet prislusnych souradnic X na kolmici:
            double X1 = (-uy * Y1 - C) / ux;
            double X2 = (-uy * Y2 - C) / ux;

            Point inter1 = new Point(X1, Y1);
            Point inter2 = new Point(X2, Y2);
            switch (act.getLast().getLocation()) {
                case Point.COLLISION_LEFT:
                    //novy stred musi byt vpravo od kolizni usecky
                    if (Track.RIGHT == Calc.sidePosition(inter1, act.getColision())) {
                        crashCenter = new Point(inter1.getX(), inter1.getY());
                    } else {
                        crashCenter = new Point(inter2.getX(), inter2.getY());
                    }
                    break;
                case Point.COLLISION_RIGHT:
                    //novy stred musi byt vlevo od kolizni usecky
                    if (Track.LEFT == Calc.sidePosition(inter1, act.getColision())) {
                        crashCenter = new Point(inter1.getX(), inter1.getY());
                    } else {
                        crashCenter = new Point(inter2.getX(), inter2.getY());
                    }
                    break;
            }
        }
        act.addPoint(crashCenter);
        //vykresleni X moznosti noveho tahu
        createTurns(crashCenter, true);
        divideTurns(racers.get(rivalID).getLast());
    }

    public void waitTurn(Formula act, String task) {
        /*metoda zaruci spravne vykreleni moznosti tahu soupere v duelu
         * urcuje take kdo ma vyjet jako prvni kdyz jsou oba hraci vybourani
         * parametr task udava v jake herni situaci se toto resi
         * task = interFinish, NORMAL, bothCrash */
        Formula rival = racers.get(rivalID);
        switch (rival.getWait()) {
            case 0:
                //souper se nevyboural a hraje
                rivalTurns();
                nextTurn(actID, act.getLast());
                break;
            case 1:
                //souper udela prvni tah po narazu
                rival.setWait(0);
                rivalTurns();
                crashTurn();
                break;
            default:
                //souper je vybourany
                switch (task) {
                    case "interFinish": //hrac na tahu projel cilem a jelikoz souper stoji, je konec hry
//                    racers.put(actID, act);
                        model.winnerAnnouncement();
                        break;
                    case "bothCrash"://hrac na tahu take boural
                        rival.setWait(rival.getWait() - 1);
                        if (rival.getWait() < act.getWait()) { //hrac 1 ceka dele
                            act.setWait(act.getWait() - rival.getWait() + 1);
                            rival.setWait(0);
                            rivalTurns();
                            crashTurn();
                        } else if (rival.getWait() > act.getWait()) { //hrac 2 ceka dele
                            crashTurn();
                            rival.setWait(rival.getWait() - act.getWait() + 1);
                            act.setWait(0);
                        } else { //oba cekaji stejne dlouho - pokracuje hrac 2
                            act.setWait(1);
                            rival.setWait(0);
                            rivalTurns();
                            crashTurn();
                        }
                        break;
                    case "normal"://hrac na tahu udelal normalni tah
                        //souperi se snizi cekani a pokracuje hrac na tahu
                        rival.setWait(rival.getWait() - 1);
                        nextTurn(actID, rival.getLast());
                        break;
                }
                break;
        }
    }

    private void createTurns(Point center, boolean crashMode) {
        //when Turns are creating again, all turns inside are non-colision
        turns = new Turns();
        // upper-LEFT corner
        if (!crashMode) {
            turns.getTurn(0).setPoint(new Point(center.x - 1, center.y - 1));
        } else {
            turns.getTurn(0).setExist(false);
        }
        // upper center
        if (turnsCount == NINE_TURNS || crashMode) {
            turns.getTurn(1).setPoint(new Point(center.x, center.y - 1));
        } else {
            turns.getTurn(1).setExist(false);
        }
        // upper-RIGHT corner
        if (!crashMode) {
            turns.getTurn(2).setPoint(new Point(center.x + 1, center.y - 1));
        } else {
            turns.getTurn(2).setExist(false);
        }
        // LEFT
        if (turnsCount == NINE_TURNS || crashMode) {
            turns.getTurn(3).setPoint(new Point(center.x - 1, center.y));
        } else {
            turns.getTurn(3).setExist(false);
        }
        // center
        if (turnsCount == FIVE_TURNS || turnsCount == NINE_TURNS) {
            turns.getTurn(4).setPoint(center);
        } else {
            turns.getTurn(4).setExist(false);
        }
        // RIGHT
        if (turnsCount == NINE_TURNS || crashMode) {
            turns.getTurn(5).setPoint(new Point(center.x + 1, center.y));
        } else {
            turns.getTurn(5).setExist(false);
        }
        // lower-LEFT corner
        if (!crashMode) {
            turns.getTurn(6).setPoint(new Point(center.x - 1, center.y + 1));
        } else {
            turns.getTurn(6).setExist(false);
        }
        // lower center
        if (turnsCount == NINE_TURNS || crashMode) {
            turns.getTurn(7).setPoint(new Point(center.x, center.y + 1));
        } else {
            turns.getTurn(7).setExist(false);
        }
        // lower-RIGHT corner
        if (!crashMode) {
            turns.getTurn(8).setPoint(new Point(center.x + 1, center.y + 1));
        } else {
            turns.getTurn(8).setExist(false);
        }
    }

    /**
     * It devides possible turns into "clean" and "dirty". Dirty turn means formula crashed. In case
     * that one possible turn is equal to rival position, that turns is not allowed.
     *
     * @param rivalLast is position of rival formula
     */
    private void divideTurns(Point rivalLast) {
        System.out.println("----------------");
        Formula act = racers.get(actID);
        Track track = model.getBuilder().getTrack();
        Polyline left = track.getLeft();
        Polyline right = track.getRight();
        for (int i = 0; i < turns.getSize(); i++) {
            Point actPoint = turns.getTurn(i).getPoint();

            if (actPoint.isEqual(rivalLast) == false && turns.getTurn(i).isExist()) {
                boolean colision = false;
                Polyline colLine = new Polyline(Polyline.SEGMENT);
                //----------- kontrola KOLIZE tahu s LEVOU STRANOU: -----------
                for (int k = 0; k < left.getLength() - 1; k++) {
                    Polyline actLeft = left.getSegment(k);
                    Object[] cross = Calc.crossing(act.getLast(), actPoint, actLeft);
                    if ((int) cross[0] != Calc.OUTSIDE) {
                        //novy bod ma prunik nebo se dotyka leve krajnice
                        colLine = actLeft;
                        Point colPoint = (Point) cross[1];
                        colPoint.setLocation(Point.COLLISION_LEFT);
                        turns.getTurn(i).setCollision(colPoint);
                        colision = true;
                        break;
                    }
                }
                if (colision == false) { //tah nekrizi levou krajnici
                    // ---------- kontrola KOLIZE novych moznosti s PRAVOU STRANOU: -------------
                    for (int k = 0; k < right.getLength() - 1; k++) {
                        Polyline actRight = right.getSegment(k);
                        Object[] cross = Calc.crossing(act.getLast(), actPoint, actRight);
                        if ((int) cross[0] != Calc.OUTSIDE) {
                            //novy bod ma prunik nebo se dotyka prave krajnice
                            colLine = actRight;
                            Point colPoint = (Point) cross[1];
                            colPoint.setLocation(Point.COLLISION_RIGHT);
                            turns.getTurn(i).setCollision(colPoint);
                            colision = true;
                            break;
                        }
                    }
                }
                if (colision == false) {
                    //tah nekrizi zadnou krajnici
                    Object[] start = Calc.crossing(act.getLast(), actPoint, track.getStart());
                    Object[] finish = Calc.crossing(act.getLast(), actPoint, track.getFinish());
                    if ((int) start[0] != Calc.OUTSIDE && Track.RIGHT == Calc.sidePosition(actPoint, track.getStart())) {
                        //tah protina start a konci vpravo od nej (projel se v protismeru)
                        colLine = track.getStart();
                        Point colPoint = (Point) start[1];
                        colPoint.setLocation(Point.COLLISION_RIGHT);
                        turns.getTurn(i).setCollision(colPoint);
                        colision = true;
                    } else if ((int) finish[0] == Calc.INSIDE) {
                        //tah protina cilovou caru:
                        turns.getTurn(i).setCollision((Point) finish[1]);
                        actPoint.setLocation(Point.FINISH);
                        System.out.println("new interPoint - through finish");
                    } else if ((int) finish[0] == Calc.EDGE) {
                        //tah se dotyka cilove cary:
                        turns.getTurn(i).setCollision((Point) finish[1]);
                        actPoint.setLocation(Point.FINISH_LINE);
                        System.out.println("new interPoint - in finish line");
                    }
                } else { //tah vede mimo trat
                    //kontrola zda hrac pred narazem projede cilem:
                    Object[] finish = Calc.crossing(act.getLast(), actPoint, track.getFinish());
                    if ((int) finish[0] != Calc.OUTSIDE && Calc.distance(act.getLast(), (Point) finish[1])
                            < Calc.distance(act.getLast(), turns.getTurn(i).getCollision())) {
                        //hrac protne cil pred narazem
                        turns.getTurn(i).setCollision((Point) finish[1]);
                        System.out.println("new interPoint - in crashed after finish line");
                        actPoint.setLocation(Point.FINISH);
                        colision = false;
                    }
                }
                if (colision) {
                    turns.getTurn(i).setType(Turns.Turn.COLLISION);
                    act.setColision(colLine);
                }
            } else {
                turns.getTurn(i).setExist(false);
            }
        }
    }

    public Polyline startPosition(Polyline startLine) {
        //identifikace startovni cary
        Polyline points = new Polyline(Polyline.GOOD_SET);
        Point first = startLine.getPreLast();
        Point second = startLine.getLast();
        if (first.x == second.x) { //startovni cara je vertikalni
            racers.get(1).setSpeed(0); //smer dopredu je v tomto pripade nulovy
            racers.get(2).setSpeed(0);
            //souradnice X je pri vertikalnim startu pro vsechny pozice stejna:
            int X = first.getX(), Y;
            if (second.y > first.y) { //prvni tah bude smerem vpravo
                racers.get(1).setSide(1); //nastaveni smeru vpravo o jedna
                racers.get(2).setSide(1);
                Y = first.getY() + 1; // prvni bod je mimo okraj drahy
                while (Y < second.y) { //prochazeni bodu, dokud nedojedu na konec startu
                    points.addPoint(new Point(X, Y));
                    Y = Y + 1; //popochazeni o jeden ctverecek dal
                }
            } else { // prvni tak je smerem vlevo
                racers.get(1).setSide(-1);
                racers.get(2).setSide(-1);
                Y = second.getY() + 1;
                while (Y < first.y) {
                    points.addPoint(new Point(X, Y));
                    Y = Y + 1;
                }
            }
        } else { //startovni cara je horizontalni
            racers.get(1).setSide(0); //smer dostrany je pri tomto startu nulovy
            racers.get(2).setSide(0);
            //souradnice Y je pri horizontalnim startu pro vsechny pozice stejna:
            int Y = first.getY(), X;
            if (second.x > first.x) {
                racers.get(1).setSpeed(-1);
                racers.get(2).setSpeed(-1);
                X = first.getX() + 1;
                while (X < second.x) {
                    points.addPoint(new Point(X, Y));
                    X = X + 1;
                }
            } else {
                racers.get(1).setSpeed(1);
                racers.get(2).setSpeed(1);
                X = second.getX() + 1;
                while (X < first.x) {
                    points.addPoint(new Point(X, Y));
                    X = X + 1;
                }
            }
        }
        return points;
    }

    public String createWinnerMessage() {
        String finalMessage;
        String winner = model.getHintLabel(HintLabels.WINNER);
        if (getFormula(1).getWin() && getFormula(2).getWin() == false) {
            finalMessage = getFormula(1).getName() + " " + winner;
        } else if (getFormula(2).getWin() && getFormula(1).getWin() == false) {
            finalMessage = getFormula(2).getName() + " " + winner;
        } //oba souperi projeli cilem a rozhoduje mensi ujeta vzdalenost
        else if (getFormula(1).getDist() < getFormula(2).getDist()) {
            finalMessage = getFormula(1).getName() + " " + winner;
        } else if (getFormula(1).getDist() > getFormula(2).getDist()) {
            finalMessage = getFormula(2).getName() + " " + winner;
        } else {
            finalMessage = model.getHintLabel(HintLabels.BOTH_WIN);
        }
        return finalMessage;
    }

    public void setFinishType(int finishType) {
        this.finishType = finishType;
    }

    public int getFinishType() {
        return finishType;
    }

    public void setID(int act, int rival) {
        actID = act;
        rivalID = rival;
    }

    public int getActID() {
        if (model.getStage() < GameModel.FIRST_TURN) {
            return 0;
        }
        return actID;
    }

    public Formula getFormula(int id) {
        return racers.get(id);
    }

    public int getFormulaCount() {
        return racers.size();
    }

    public void resetTurns() {
        turns.reset();
    }

    public Turns getTurns() {
        return turns;
    }

    public void setTurnsCount(int turnsCount) {
        this.turnsCount = turnsCount;
    }

    public int getTurnsCount() {
        return turnsCount;
    }

    public int getLengthHist() {
        return lengthHist;
    }

    public void setLengthHist(Object lengthHist) {
        String len = String.valueOf(lengthHist);
        try {
            this.lengthHist = Integer.valueOf(len);
        } catch (NumberFormatException e) {
            this.lengthHist = LENGTH_MAX;
        }
    }

}
