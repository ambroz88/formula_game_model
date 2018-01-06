package com.ambi.formula.gamemodel;

import java.util.ArrayList;
import java.util.List;

import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Polyline;
import com.ambi.formula.gamemodel.datamodel.Track;
import com.ambi.formula.gamemodel.labels.HintLabels;
import com.ambi.formula.gamemodel.utils.Calc;
import com.ambi.formula.gamemodel.utils.TrackUtils;

/**
 * This class is used when the user builds the track. Basically there is tested if the point which
 * user clicked could be use as a part of the track and which side.
 *
 * @author Jiri Ambroz
 */
public class TrackBuilder extends TrackEditor {

    private final GameModel model;
    private int side, oppSide;
    private Polyline points;
    private List<Polyline> checkLines;
    private String message;

    public TrackBuilder(GameModel gModel) {
        this.model = gModel;
        points = new Polyline(Polyline.GOOD_SET);
        checkLines = new ArrayList<>();
    }

    /**
     * This is main method for building track. It controls if input <code>point click</code> can be
     * use in track. If it is a good point the method add it to given side. If it is has a bad
     * position it will be shown some hint.
     *
     * @param click is point where user clicked.
     * @param newSide is side which is build.
     */
    public void buildTrack(Point click, int newSide) {
        this.side = newSide;
        if (Track.LEFT == side) {
            oppSide = Track.RIGHT;
        } else {
            oppSide = Track.LEFT;
        }
        Polyline actLine = getLine(side);
        Polyline oppLine = getLine(oppSide);
        //OPPOSITE SIDE WAS ALLREADY STARTED
        message = HintLabels.EMPTY;
        if (oppLine.getLength() > 0) {
            //builded side is still empty and user clicked on one of the start points
            if (actLine.getLength() == 0 && points.isInside(click)) {
                drawFinishTurns();
                points.addPoint(click);//in point of click there will be drawn a point
                addPoint(side, click);
            } //builded side is still empty but user clicked out of the start points
            else if (actLine.getLength() == 0) {
                message = HintLabels.WRONG_START;
            } //point click is good and IT IS POSSIBLE TO ADD IT to the track
            else if (actLine.getLast().isEqual(click) == false && buildSecondSide(click)) {
                addPoint(side, click);

                boolean ready = isReadyForDraw() && getPoints().isInside(click);
                if (ready) {
                    finishIndexes();
                }
                getModel().fireTrackReady(ready);
            }
        } //OPPOSITE SIDE WASN'T STILL STARTED
        else {
            //create start
            if (actLine.getLength() <= 1) {
                if (actLine.getLength() == 0) {
                    points.addPoint(click); //first point in side is drawn
                }
                addPoint(side, click);
            } //point click is not identical with the last point in builded side
            else if (!actLine.getLast().isEqual(click)) {
                //new edge of builded side don't cross any other edge
                if (!actLine.checkOwnCrossing(click)) {
                    if (correctDirection(actLine, click)) {
                        addPoint(side, click);
                    }
                } else {
                    message = HintLabels.CROSSING;
                }
            } else {
                message = HintLabels.IDENTICAL_POINTS;
            }
        }
    }

    /**
     * It checks if the future construction move won't be backwards inside the track.
     *
     * @param actLine is line that is builded
     * @param click is point where should be next part of the track line
     * @return true if the position of the point click is OK, false otherwise
     */
    private boolean correctDirection(Polyline actLine, Point click) {
        //check bad direction of constructed side
        if (actLine.getLength() > 1) {
            Polyline lastSegment = actLine.getLastSegment();
            if (Calc.sidePosition(click, lastSegment) == oppSide
                    && Calc.distance(lastSegment.getPoint(0), lastSegment.getPoint(1))
                    >= Calc.distance(actLine.getPreLast(), Calc.baseOfAltitude(lastSegment, click))) {
                message = HintLabels.FORWARD;
                return false;
            }
        }
        return true;
    }

    /**
     * This method generates points where it is possible to place last point of the track so the
     * finish line would be in vertical or horizontal plane.
     *
     * @return points as polyline
     */
    private void drawFinishTurns() {
        Polyline oppLine = getLine(oppSide);
        Point start = oppLine.getPreLast();
        Point finish = oppLine.getLast();

        int quad = TrackUtils.findQuad(start, finish);
        setPoints(TrackUtils.generateGoalPoints(quad, finish, side));
    }

    /**
     * This method generates points where it is possible to place first point of the second side of
     * the track so the start line would be in vertical or horizontal plane.
     *
     * @return points as polyline
     */
    private void drawStartTurns() {
        Polyline oppLine = getLine(oppSide);
        Point start = oppLine.getPoint(0);
        Point finish = oppLine.getPoint(1);

        int quad = TrackUtils.findQuad(start, finish);
        setPoints(TrackUtils.generateGoalPoints(quad, start, side));
    }

    /**
     * This method tests if it's possible to add <code>point click</code> to the track. It controls
     * both side of track.
     *
     * @param click is point which is tested or added
     * @return true if <code>click</code> is possible to add
     */
    private boolean buildSecondSide(Point click) {
        Polyline actLine = getLine(side);
        Polyline oppLine = getLine(oppSide);
        Polyline trackEnd = new Polyline(actLine.getLast(), oppLine.getPoint(getIndex(oppSide)));
        boolean success = true;

        if ((int) Calc.crossing(actLine.getLast(), click, getStart())[0] == Calc.INSIDE) {
            message = HintLabels.THROUGH_START;
            success = false;
        } else if (actLine.getLength() == 1 && Calc.sidePosition(click, trackEnd) != side) {
            // check if new side is building on right direction from the start
            message = HintLabels.FORWARD;
            success = false;
        } else if (actLine.checkOwnCrossing(click)) {
            // check crossing of constructed side:
            message = HintLabels.CROSSING;
            success = false;
        } else if (oppLine.checkSegmentCrossing(actLine.getLast(), click)) {
            // check crossing of opposite side:
            message = HintLabels.CROSSING;
            success = false;
        } else if (!correctDirection(actLine, click)) {
            //check bad direction of constructed side
            success = false;
        } else if (freeDrawing(side, oppSide)) {
            success = true;
        } else {
            //turn is OK but it is necessary to check "building index" on opposite side
            boolean search = true;
            Point prev, center, next, sidePoint;
            int index = getIndex(oppSide);
            while (search) {
                if (index < oppLine.getLength() - 2) {
                    //create next segment that should be crossed by point click:
                    prev = oppLine.getPoint(index);
                    center = oppLine.getPoint(index + 1);
                    next = oppLine.getPoint(index + 2);
                    sidePoint = Calc.calculateAngle(prev, center, next, side);

                    if ((int) Calc.crossing(actLine.getLast(), click, center, sidePoint)[0] >= Calc.EDGE) {
                        //point click went through "control segment"
                        setIndex(index + 1, oppSide);
                    }
                } else if (index == oppLine.getLength() - 2) {
                    prev = oppLine.getPoint(index - 1);
                    center = oppLine.getPoint(index);
                    next = oppLine.getPoint(index + 1);
                    sidePoint = Calc.calculateAngle(prev, center, next, side);

                    if ((int) Calc.crossing(actLine.getLast(), click, center, sidePoint)[0] >= Calc.EDGE) {
                        //point click went through "control segment"
                        setIndex(index + 1, oppSide);
                    }
                } else {
                    search = false;
                }
                index++;
            }
            setIndex(actLine.getLength(), side);
        }
        return success;
    }

    public void setSide(int side) {
        if (side == Track.LEFT) {
            this.oppSide = Track.RIGHT;
        } else {
            this.oppSide = Track.LEFT;
        }
        this.side = side;
        setWidth(side);
    }

    /**
     * Metoda vytvori pole "prujezdovych" usecek, ktere pocitac projizdi pri prujezdu trati. Prvni
     * useckou je start a posledni je cil. Ke kazdemu bodu z delsi krajnice je prirazen bod z
     * protejsi strany. Delka pole se rovna delce delsi krajnice.
     */
    public void analyzeTrack() {
        int lowIndex = 0;
        int maxLength = getLong().getLength();
        Polyline longSide = getLong();
        Polyline shortSide = getShort();
        checkLines = new ArrayList<>();//seznam prujezdovych usecek
        checkLines.add(getStart()); // prvni checkLine je start

        //prochazeni delsi krajnice a hledani od kazdeho bodu vhodny protejsi bod
        for (int k = 1; k < maxLength - 1; k++) {
            int actIndex = lowIndex;
            boolean intersect = false;
            Point start = longSide.getPoint(k);//z tohoto bodu bude spustena kolmice
            Polyline segment = longSide.getSegment(k - 1);
            Point end = Calc.rightAngle(segment, getLongStr());

            // ------------------ PROCHAZENI KRATSI STRANY -------------------
            for (int i = lowIndex; i < shortSide.getLength() - 1; i++) {
                Point opPoint1 = shortSide.getPoint(i);
                Point opPoint2 = shortSide.getPoint(i + 1);
                Object[] cross = Calc.crossing(start, end, opPoint1, opPoint2);

                if ((int) cross[0] == Calc.INSIDE) {
                    if (Calc.distance(opPoint1, (Point) cross[1]) <= Calc.distance(opPoint2, (Point) cross[1])) {
                        actIndex = i;
                    } else {
                        actIndex = i + 1;
                    }
                    intersect = true;
                    break;
                } else if ((int) cross[0] == Calc.EDGE) {
                    actIndex = i + 1;
                    intersect = true;
                    break;
                }
            }//-------------------------------------------------------------------
            if (intersect == false && actIndex < shortSide.getLength() - 1) {
                /* kolmice neprotnula zadnou protejsi stranu, a vybere se nejblizsi mezi
                 * aktualnim poslednim a poslednim v linii */
                actIndex = shortSide.getLength() - 1;
            }
            actIndex = findNearest(segment, lowIndex, actIndex);
            if (lowIndex != actIndex) {
                //pokud se na kratsi strane vynechaji body, uz se nepocita posledni bod na kratke strane
                lowIndex++;
            }

            Point opPoint = shortSide.getPoint(actIndex);
            if (getLongStr() == Track.LEFT) {
                checkLines.add(new Polyline(start, opPoint));
            } else {
                checkLines.add(new Polyline(opPoint, start));
            }

            //prirazeni bodu z delsi strany i pro vynechane body na kratsi strane
            while (lowIndex < actIndex) {
                opPoint = shortSide.getPoint(lowIndex);
                start = Calc.findNearestPoint(opPoint, segment.getPoints());
                //je zachovano poradi: prvni bod je na leve strane a druhy na prave:
                if (getLongStr() == Track.LEFT) {
                    checkLines.add(checkLines.size() - 1, new Polyline(start, opPoint));
                } else {
                    checkLines.add(checkLines.size() - 1, new Polyline(opPoint, start));
                }
                lowIndex++;
            }
            lowIndex = actIndex;
        }

        //k poslednim bodum kratsi strany nejsou prirazeny zadne body z delsi strany:
        lowIndex++;
        Point start = getLong().getPreLast();
        while (lowIndex < getShort().getLength() - 1) {
            Point opPoint = shortSide.getPoint(lowIndex);
            //je zachovano poradi: prvni bod je na leve strane a druhy na prave:
            if (getLongStr() == Track.LEFT) {
                checkLines.add(new Polyline(start, opPoint));
            } else {
                checkLines.add(new Polyline(opPoint, start));
            }
            lowIndex++;
        }
        checkLines.add(getFinish());
    }

    /**
     * Metoda najde ve vstupni polylinii "edge" index bodu, ktery je nejblizsi od vstupniho bodu
     * last. Hledani ve vstupni linii je vymezeno dolnim a hornim indexem (min a max).
     *
     * @param edge - polylinie, ve ktere se hleda nejblizsi bod
     * @param min - dolni mez, od ktere se hleda nejblizsi bod
     * @param max - horni mez, ke ktere se hleda nejblizsi bod
     * @return - index nejblizsiho bodu
     */
    private int findNearest(Polyline edge, int min, int max) {
        Point last = edge.getLast();
        int index = min;
        for (int i = min + 1; i <= max; i++) {
            Point actPoint = getShort().getPoint(i);
            if (Calc.distance(last, getShort().getPoint(index)) > Calc.distance(last, actPoint)
                    && Calc.sidePosition(actPoint, edge) == getShortStr()) {
                index = i;
            }
        }
        return index;
    }

    // ---------------- METHOD FROM TRACK MENU --------------------
    public void startBuild(int side) {
        if (getOppLine(side).getLength() != 1) {
            getModel().repaintScene();
            setSide(side);
            //vykresleni moznosti tvorby pocatecnich a koncovych bodu:
            if (getOppLine(side).getLength() > 1 && getLine(side).getLength() == 0) {
                drawStartTurns();
            } else if (getOppLine(side).getLength() > 1 && getLine(side).getLength() > 0) {
                drawFinishTurns();
            }
            if (side == Track.LEFT) {
                getModel().setStage(GameModel.BUILD_LEFT);
            } else {
                getModel().setStage(GameModel.BUILD_RIGHT);
            }
        } else {
            if (side == Track.LEFT) {
                getModel().fireHint(HintLabels.RIGHT_SIDE_FIRST);
                //caught by TrackMenu:
                getModel().firePropertyChange("rightSide", false, true);
            } else {
                getModel().fireHint(HintLabels.LEFT_SIDE_FIRST);
                //caught by TrackMenu:
                getModel().firePropertyChange("leftSide", false, true);
            }
        }
    }

    /**
     * It prepares track for editing mode, so all points in track will be visibly marked.
     */
    public void clearTrackInside() {
        getCheckLines().clear();
        getPoints().clear();
    }

    public void deletePoint(int actSide, int oppSide) {
        //mazani poslednich bodu pri tvorbe trati - podle toho, jaka se krajnice vybrana
        int actSize = getLine(actSide).getLength();
        int oppSize = getLine(oppSide).getLength();

        if (actSize > 0) {
            removeLast(actSide);
            //kdyz zbyde v krajnici pouze jeden bod, tak bude vykreslen
            if (actSize == 1) {
                points.addPoint(getLine(actSide).getLast());
            } //kdyz se smaze i posledni bod, smaze se take tecka znacici prvni bod
            else if (actSize == 0) {
                points.clear();
                if (oppSize > 0) {
                    drawStartTurns();
                }
            } else if (oppSize > 1) {
                drawFinishTurns();
            }
        }
    }

    private void removeLast(int side) {
        removeLastPoint(side);
        if (getLine(side).getLength() > 0) {
            boolean ready = isReady() && getPoints().isInside(getLine(side).getLast());
            if (ready) {
                getModel().fireTrackReady(ready);
            }
        }
        getModel().repaintScene();
    }

    public GameModel getModel() {
        return model;
    }

    public List<Polyline> getCheckLines() {
        return checkLines;
    }

    public Polyline getPoints() {
        return points;
    }

    public void setPoints(Polyline points) {
        this.points = points;
    }

    public String getMessage() {
        return message;
    }

}
