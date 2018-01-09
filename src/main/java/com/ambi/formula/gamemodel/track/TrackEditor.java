package com.ambi.formula.gamemodel.track;

import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Polyline;
import com.ambi.formula.gamemodel.datamodel.Track;
import com.ambi.formula.gamemodel.utils.Calc;

/**
 *
 * @author Jiri Ambroz
 */
public class TrackEditor extends Track {

    private int movePointIndex;
    private int side;

    public TrackEditor() {
    }

    /**
     * It finds out which point will be moved, resp it's side and position. Start and finish can't
     * be moved.
     *
     * @param click is point where user clicked - potentional point that will be moved
     * @return true if the point is part of the track
     */
    public boolean clickOnTrack(Point click) {
        //check if player clicked on the point from left side of the track
        for (int i = 1; i < getLeft().getLength() - 1; i++) {
            if (click.isEqual(getLeft().getPoint(i))) {
                movePointIndex = i;
                side = Track.LEFT;
                break;
            }
        }
        if (movePointIndex == 0) {
            //check if player clicked on the point from right side of the track
            for (int i = 1; i < getRight().getLength() - 1; i++) {
                if (click.isEqual(getRight().getPoint(i))) {
                    movePointIndex = i;
                    side = Track.RIGHT;
                    break;
                }
            }
        }

        return movePointIndex > 0;
    }

    /**
     * It validates if user moves the point in correct position so newly created segments don't have
     * intersect with any other segments.
     *
     * @param click is point where user placed certain point of the track
     * @return true if there is no intersection with the rest of the track, false otherwise
     */
    public boolean isNewPointValid(Point click) {
        Point newEdgeStart = getLine(side).getPoint(movePointIndex - 1);
        Point newEdgeEnd = getLine(side).getPoint(movePointIndex + 1);
        // new segments can't cross start, finish or opposite side
        boolean intersect = crossEndLines(newEdgeStart, newEdgeEnd, click)
                || getOppLine(side).checkSegmentCrossing(newEdgeStart, click)
                || getOppLine(side).checkSegmentCrossing(newEdgeEnd, click);
        if (!intersect) {
            // new segments can't cross it own side but it can touch it
            for (int i = 0; i < getLine(side).getLength() - 1; i++) {
                if (i < movePointIndex - 1 || i > movePointIndex) {
                    Polyline actRight = getLine(side).getSegment(i);
                    if ((int) Calc.crossing(click, newEdgeStart, actRight)[0] == Calc.INSIDE
                            || (int) Calc.crossing(click, newEdgeEnd, actRight)[0] == Calc.INSIDE) {
                        intersect = true;
                    }
                }
            }

        }

        if (intersect == false) {
            //overwrite point of the track
            getLine(side).changePoint(click, movePointIndex);
        }
        movePointIndex = 0;
        return !intersect;
    }

    private boolean crossEndLines(Point edgeStart, Point edgeEnd, Point click) {
        boolean intersect = false;
        if (getStart() != null) {
            if ((int) Calc.crossing(edgeStart, click, getStart())[0] == Calc.INSIDE
                    || (int) Calc.crossing(edgeEnd, click, getStart())[0] == Calc.INSIDE) {
                intersect = true;
            } else if (getFinish() != null) {
                if ((int) Calc.crossing(edgeStart, click, getFinish())[0] == Calc.INSIDE
                        || (int) Calc.crossing(edgeEnd, click, getFinish())[0] == Calc.INSIDE) {
                    intersect = true;
                }
            }
        }
        return intersect;
    }
}
