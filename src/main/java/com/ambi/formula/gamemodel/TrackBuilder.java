package com.ambi.formula.gamemodel;

import java.beans.PropertyChangeListener;

import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Polyline;
import com.ambi.formula.gamemodel.datamodel.Track;
import com.ambi.formula.gamemodel.labels.HintLabels;
import com.ambi.formula.gamemodel.utils.Calc;

/**
 * This class is used when the user builds the track. Basically there is tested
 * if the point which user clicked could be use as a part of the track and which
 * side.
 *
 * @author Jiri Ambroz
 */
public class TrackBuilder {

    public static final int NORTH = 1;
    public static final int NORTH_EAST = 2;
    public static final int EAST = 3;
    public static final int SOUTH_EAST = 4;
    public static final int SOUTH = 5;
    public static final int SOUTH_WEST = 6;
    public static final int WEST = 7;
    public static final int NORTH_WEST = 8;

    private final GameModel model;
    private Track track;
    private int side, oppSide;

    public TrackBuilder(GameModel menu) {
        this.model = menu;
        this.track = new Track(this.model);
    }

    /**
     * This is main method for building track. It controls if input
     * <code>point click</code> can be use in track. If it is a good point the
     * method add it to given side. If it is has a bad position it will be shown
     * some hint.
     *
     * @param click is point where user clicked.
     * @param newSide is side which is build.
     */
    public void buildTrack(Point click, int newSide) {
        Polyline points = new Polyline(model.getPoints());
        this.side = newSide;
        if (Track.LEFT == side) {
            oppSide = Track.RIGHT;
        } else {
            oppSide = Track.LEFT;
        }
        Polyline actLine = track.getLine(side);
        Polyline oppLine = track.getLine(oppSide);
        //OPPOSITE SIDE WAS ALLREADY STARTED
        if (oppLine.getLength() > 0) {
            //builded side is still empty and user clicked on one of the start points
            if (actLine.getLength() == 0 && points.isInside(click)) {
                points = drawFinishTurns();
                points.addPoint(click);//in point of click there will be drawn a point
                track.addPoint(side, click);
                model.setPoints(points);
                //first point of parallel side is identical
                track.addParallelPoint(0, side, click);
            } //builded side is still empty but user clicked out of the start points
            else if (actLine.getLength() == 0) {
                model.fireHint(HintLabels.WRONG_START);
            } //point click is good and IT IS POSSIBLE TO ADD IT to the track
            else if (actLine.getLast().isEqual(click) == false && buildSecondSide(click)) {
                track.addPoint(side, click);
                isTrackReady(click);
            }
        } //OPPOSITE SIDE WASN'T STILL STARTED
        else {
            //create start
            if (actLine.getLength() <= 1) {
                if (actLine.getLength() == 0) {
                    points.addPoint(click); //first point in side is drawn
                    model.setPoints(points);
                }
                track.addPoint(side, click);
            } //point click is not identical with the last point in builded side
            else if (!actLine.getLast().isEqual(click)) {
                //new edge of builded side don't cross any other edge
                if (!checkOwnCross(actLine, click)) {
                    if (correctDirection(actLine, click)) {
                        track.addPoint(side, click);
                    }
                }
            } else {
                model.fireHint(HintLabels.IDENTICAL_POINTS);
            }
        }
    }

    /**
     * It checks if the future construction move won't be backwards inside the
     * track.
     *
     * @param actLine is line that is builded
     * @param click is point where should be next part of the track line
     * @return true if the position of the point click is OK, false otherwise
     */
    private boolean correctDirection(Polyline actLine, Point click) {
        //check bad direction of constructed side
        if (actLine.getLength() > 1) {
            Polyline lastSegment = new Polyline(actLine.getPreLast(), actLine.getLast());
            if (Calc.sidePosition(click, lastSegment) == oppSide
                    && Calc.distance(actLine.getPreLast(), actLine.getLast())
                    >= Calc.distance(actLine.getPreLast(), Calc.baseOfAltitude(lastSegment, click))) {
                model.fireHint(HintLabels.FORWARD);
                return false;
            }
        }
        return true;
    }

    private void isTrackReady(Point click) {
        boolean ready = track.getReady() && model.getPoints().isInside(click);
        if (ready) {
            track.finishIndexes();
        }
        model.fireTrackReady(ready);
    }

    /**
     * This method generates points where it is possible to place last point of
     * the track so the finish line would be in vertical or horizontal plane.
     *
     * @return points as polyline
     */
    private Polyline drawFinishTurns() {
        Polyline oppLine = track.getLine(oppSide);
        Point start = oppLine.getPreLast();
        Point finish = oppLine.getLast();

        int quad = Calc.findQuad(start, finish);
        return generatePossibilities(quad, finish);
    }

    /**
     * This method generates points where it is possible to place first point of
     * the second side of the track so the start line would be in vertical or
     * horizontal plane.
     *
     * @return points as polyline
     */
    private Polyline drawStartTurns() {
        Polyline oppLine = track.getLine(oppSide);
        Point start = oppLine.getPoint(0);
        Point finish = oppLine.getPoint(1);

        int quad = Calc.findQuad(start, finish);
        return generatePossibilities(quad, start);
    }

    /**
     * It calculates 5 points which lies horizontaly and verticaly from central
     * point. Direction is based on octant which says if segment went to "north
     * east", "south west" or "south" etc.
     *
     * @param octant determines direction of the segment
     * @param centralPoint is point from which the calculations start
     * @return points as a polyline
     */
    private Polyline generatePossibilities(int octant, Point centralPoint) {
        int sideKoef;
        if (side == Track.RIGHT) {
            //for right side it is necessary to subtract coordinates
            sideKoef = -1;
        } else {
            sideKoef = 1;
        }

        // NALEZENI KVADRANTU, KAM SMERUJE ZVOLENA USECKA
        Polyline points = new Polyline(Polyline.GOOD_SET);
        switch (octant) {
            case NORTH:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x - sideKoef * i, centralPoint.y));
                }
                break;
            case NORTH_EAST:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x - sideKoef * i, centralPoint.y));
                    points.addPoint(new Point(centralPoint.x, centralPoint.y - sideKoef * i));
                }
                break;
            case EAST:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x, centralPoint.y - sideKoef * i));
                }
                break;
            case SOUTH_EAST:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x + sideKoef * i, centralPoint.y));
                    points.addPoint(new Point(centralPoint.x, centralPoint.y - sideKoef * i));
                }
                break;
            case SOUTH:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x + sideKoef * i, centralPoint.y));
                }
                break;
            case SOUTH_WEST:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x + sideKoef * i, centralPoint.y));
                    points.addPoint(new Point(centralPoint.x, centralPoint.y + sideKoef * i));
                }
                break;
            case WEST:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x, centralPoint.y + sideKoef * i));
                }
                break;
            case NORTH_WEST:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x - sideKoef * i, centralPoint.y));
                    points.addPoint(new Point(centralPoint.x, centralPoint.y + sideKoef * i));
                }
                break;
        }
        return points;
    }

    /**
     * This method tests if it's possible to add <code>point click</code> to the
     * track. It controls both side of track.
     *
     * @param click is point which is tested or added
     * @return true if <code>click</code> is possible to add
     */
    private boolean buildSecondSide(Point click) {
        Polyline actLine = track.getLine(side);
        Polyline oppLine = track.getLine(oppSide);

        if ((int) Calc.crossing(actLine.getLast(), click, track.getStart())[0] == Calc.INSIDE) {
            model.fireHint(HintLabels.THROUGH_START);
            return false;
        }

        Polyline trackEnd = new Polyline(actLine.getLast(), oppLine.getPoint(track.getIndex(oppSide)));
        // check if new side is building on right direction from the start
        if (actLine.getLength() == 1 && Calc.sidePosition(click, trackEnd) != side) {
            model.fireHint(HintLabels.FORWARD);
            return false;
        }

        // check crossing of constructed side:
        if (checkOwnCross(actLine, click)) {
            return false;
        }

        // check crossing of opposite side:
        if (checkOppositeCross(oppLine, actLine.getLast(), click)) {
            model.fireHint(HintLabels.CROSSING);
            return false;
        }

        //check bad direction of constructed side
        if (!correctDirection(actLine, click)) {
            return false;
        }

        if (track.freeDrawing(side, oppSide)) {
            return true;
        }

        //check if side is inside of opposite parallel side
        if (actLine.getLength() == 1) {
            Polyline segment = new Polyline(track.getParallelLine(side).getPoint(0), track.getParallelLine(side).getPoint(1));
            if (Calc.sidePosition(click, segment) == side) {
                model.fireHint(HintLabels.FORWARD);
                return false;
            }
        } else if (checkOppositeCross(track.getParallelLine(side), actLine.getLast(), click)) {
            model.fireHint(HintLabels.FORWARD);
            return false;
        }

        //turn is OK but it is necessary to check "building index" on opposite side
        boolean search = true;
        Point prev, center, next, sidePoint;
        int index = track.getIndex(oppSide);
        while (search) {
            if (index < oppLine.getLength() - 2) {
                //create next segment that should be crossed by point click:
                prev = oppLine.getPoint(index);
                center = oppLine.getPoint(index + 1);
                next = oppLine.getPoint(index + 2);
                sidePoint = Calc.calculateAngle(prev, center, next, side);

                if ((int) Calc.crossing(actLine.getLast(), click, center, sidePoint)[0] >= Calc.EDGE) {
                    //point click went through "control segment"
                    track.setIndex(index + 1, oppSide);
                }
            } else if (index == oppLine.getLength() - 2) {
                prev = oppLine.getPoint(index - 1);
                center = oppLine.getPoint(index);
                next = oppLine.getPoint(index + 1);
                sidePoint = Calc.calculateAngle(prev, center, next, side);

                if ((int) Calc.crossing(actLine.getLast(), click, center, sidePoint)[0] >= Calc.EDGE) {
                    //point click went through "control segment"
                    track.setIndex(index + 1, oppSide);
                }
            } else {
                search = false;
            }
            index++;
        }
        track.setIndex(actLine.getLength(), side);
        return true;
    }

    /**
     * This method controls if segment <code>Polyline line.last()</code> and
     * Point click crosses or touches any of the rest of segment in line.
     *
     * @param line is side of the track which
     * @param click point where the track will be constructed
     * @return true if there is a colision
     */
    private boolean checkOwnCross(Polyline line, Point click) {
        if (line.getLength() > 1) {
            Point last = line.getLast();
            //prochazeni usecek leve krajnice od prvni do posledni usecky
            for (int i = 0; i < line.getLength() - 2; i++) {
                //kontrola mozne kolize usecek:
                if ((int) Calc.crossing(last, click, line.getPoint(i), line.getPoint(i + 1))[0] != Calc.OUTSIDE) {
                    model.fireHint(HintLabels.CROSSING);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method controls if segment <code>line</code> and Point click crosses
     * or touches any of the rest of segment in line.
     *
     * @param line is side of the track which
     * @param last last point of line which is constructed
     * @param click point where the track will be constructed
     * @return true if there is a colision
     */
    private boolean checkOppositeCross(Polyline line, Point last, Point click) {
        if (line.getLength() > 1) {
            //prochazeni usecek leve krajnice od prvni do posledni usecky
            for (int i = 0; i < line.getLength() - 1; i++) {
                //kontrola mozne kolize usecek:
                if ((int) Calc.crossing(last, click, line.getPoint(i), line.getPoint(i + 1))[0] != Calc.OUTSIDE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method create polyline which defines possiblities of track's side
     * constuction. It is not allowed to cross this polyline.
     */
    private void createBounds() {
        Polyline oppLine = track.getLine(oppSide);
        if (oppLine.getLength() > 2) {
            track.getParallelLine(side).clear();
            if (track.getLine(side).getLength() > 0) {
                track.addParallelPoint(side, track.getLine(side).getPoint(0));
            }
            for (int i = 1; i < oppLine.getLength() - 1; i++) {
                //create point on the other parallel side
                Point prev = oppLine.getPoint(i - 1);
                Point center = oppLine.getPoint(i);
                Point next = oppLine.getPoint(i + 1);
                Point sidePoint = Calc.calculateAngle(prev, center, next, side);

                if (sidePoint != null) {
                    track.addParallelPoint(side, sidePoint);
                    if (track.getParallelLine(side).getLength() > 3) {
                        if (i == oppLine.getLength() - 2) {
                            track.addParallelPoint(side, oppLine.getLast());
                            straightParallel();
                            track.getParallelLine(side).removeLast();
                        } else {
                            straightParallel();
                        }
                    }
                }

            }
        }
    }

    /**
     * This method ensure that track's parallel line won't have any
     * self-intersections.
     */
    private void straightParallel() {
        Polyline line = track.getParallelLine(side); // it has minimally 4 points
        Polyline lastSegment = new Polyline(line.getPreLast(), line.getLast());
        Object[] cross;
        //go through all segments and check collision with the last one
        for (int i = 0; i < line.getLength() - 3; i++) {
            //check segments collision
            cross = Calc.crossing(line.getPoint(i), line.getPoint(i + 1), lastSegment);
            if ((int) cross[0] != Calc.OUTSIDE) {
                Point intersect = (Point) cross[1];
                //points between collision segments will be overwrited into collision point
                for (int k = i + 1; k < line.getLength() - 1; k++) {
                    track.getParallelLine(side).getPoint(k).setPoint(intersect);
                }
            }
        }
    }

    public void setSide(int side) {
        if (side == Track.LEFT) {
            this.oppSide = Track.RIGHT;
        } else {
            this.oppSide = Track.LEFT;
        }
        this.side = side;
        track.setWidth(side);
        createBounds();
    }

    public void changePoint(int side, Point click, int index) {
        track.getLine(side).changePoint(click, index);
    }

    private void removeLast(int side) {
        track.removeLastPoint(side);
        if (track.getLine(side).getLength() > 0) {
            boolean ready = track.getReady() && model.getPoints().isInside(track.getLine(side).getLast());
            if (ready) {
                model.fireTrackReady(ready);
            }
        }
        model.repaintScene();
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        getModel().endGame();
        this.track = track;
        getModel().fireTrackReady(true);
        getModel().repaintScene();
    }

    public GameModel getModel() {
        return model;
    }

    // ---------------- METHOD FROM TRACK MENU --------------------
    public void startBuild(int side) {
        if (getTrack().getOppLine(side).getLength() != 1) {
            model.repaintScene();
            setSide(side);
            //vykresleni moznosti tvorby pocatecnich a koncovych bodu:
            if (getTrack().getOppLine(side).getLength() > 1 && getTrack().getLine(side).getLength() == 0) {
                model.setPoints(drawStartTurns());
            } else if (getTrack().getOppLine(side).getLength() > 1 && getTrack().getLine(side).getLength() > 0) {
                model.setPoints(drawFinishTurns());
            }
            model.setStage(side);
        } else {
            if (side == Track.LEFT) {
                model.fireHint(HintLabels.RIGHT_SIDE_FIRST);
            } else {
                model.fireHint(HintLabels.LEFT_SIDE_FIRST);
            }
        }
    }

    /**
     * It prepares track for editing mode, so all points in track will be
     * visibly marked.
     */
    public void editPoints() {
        model.setStage(GameModel.EDIT_PRESS);
        model.fireHint(HintLabels.MOVE_POINTS);
        Polyline bad = new Polyline(Polyline.CROSS_SET);
        for (int i = 1; i < getTrack().left().getLength() - 1; i++) {
            bad.addPoint(getTrack().left().getPoint(i));
        }
        for (int i = 1; i < getTrack().right().getLength() - 1; i++) {
            bad.addPoint(getTrack().right().getPoint(i));
        }
        model.setBadPoints(bad);
    }

    public void deletePoint(int actSide, int oppSide) {
        if (actSide == 0) {
            model.fireHint(HintLabels.CHOOSE_SIDE);
        } else {
            //mazani poslednich bodu pri tvorbe trati - podle toho, jaka se krajnice vybrana
            int actSize = getTrack().getLine(actSide).getLength();
            int oppSize = getTrack().getLine(oppSide).getLength();

            if (actSize > 0) {
                removeLast(actSide);
                track.getParallelLine(oppSide).removeLast();
                Polyline points = model.getPoints();
                //kdyz zbyde v krajnici pouze jeden bod, tak bude vykreslen
                if (actSize == 1) {
                    points.addPoint(getTrack().getLine(actSide).getLast());
                } //kdyz se smaze i posledni bod, smaze se take tecka znacici prvni bod
                else if (actSize == 0) {
                    points.clear();
                    if (oppSize > 0) {
                        points = drawStartTurns();
                    }
                } else if (oppSize > 1) {
                    points = drawFinishTurns();
                }
                model.setPoints(points);
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        model.addPropertyChangeListener(listener);
    }

}
