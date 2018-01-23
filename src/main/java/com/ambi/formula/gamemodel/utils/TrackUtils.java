package com.ambi.formula.gamemodel.utils;

import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Polyline;

/**
 *
 * @author Jiri Ambroz
 */
public class TrackUtils {

    public static final int NORTH = 1;
    public static final int NORTH_EAST = 2;
    public static final int EAST = 3;
    public static final int SOUTH_EAST = 4;
    public static final int SOUTH = 5;
    public static final int SOUTH_WEST = 6;
    public static final int WEST = 7;
    public static final int NORTH_WEST = 8;

    /**
     * <html>This method calculetes the code of direction kvadrant of segment which is given by two
     * points. It depends on order of the segment. Possible directions:<br>
     * 1 = north 3 = east 5 = south 7 = west <br>
     * 2 = northeast 4 = southeast 6 = southwest 8 = northwest
     *
     * @param first is first point of segment
     * @param second is second point of segment
     * @return code of direction (could be values from 1 to 8) </html>
     */
    public static int findQuad(Point first, Point second) {
        int quad = 0;
        //smerovy vektor vstupni usecky:
        double ux = second.x - first.x;
        double uy = second.y - first.y;
        if (ux == 0 && uy < 0) {
            quad = NORTH;
        } else if (ux > 0 && uy < 0) {
            quad = NORTH_EAST;
        } else if (ux > 0 && uy == 0) {
            quad = EAST;
        } else if (ux > 0 && uy > 0) {
            quad = SOUTH_EAST;
        } else if (ux == 0 && uy > 0) {
            quad = SOUTH;
        } else if (ux < 0 && uy > 0) {
            quad = SOUTH_WEST;
        } else if (ux < 0 && uy == 0) {
            quad = WEST;
        } else if (ux < 0 && uy < 0) {
            quad = NORTH_WEST;
        }
        return quad;
    }

    /**
     * It calculates 5 points which lies horizontaly and verticaly from central point. Direction is
     * based on octant which says if segment went to "north east", "south west" or "south" etc.
     *
     * @param octant determines direction of the segment
     * @param centralPoint is point from which the calculations start
     * @param side means which side is build (Track.LEFT or Track.RIGHT)
     * @return points as a polyline
     */
    public static Polyline generateGoalPoints(int octant, Point centralPoint, int side) {

        // NALEZENI KVADRANTU, KAM SMERUJE ZVOLENA USECKA
        Polyline points = new Polyline();
        switch (octant) {
            case NORTH:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x - side * i, centralPoint.y));
                }
                break;
            case NORTH_EAST:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x - side * i, centralPoint.y));
                    points.addPoint(new Point(centralPoint.x, centralPoint.y - side * i));
                }
                break;
            case EAST:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x, centralPoint.y - side * i));
                }
                break;
            case SOUTH_EAST:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x + side * i, centralPoint.y));
                    points.addPoint(new Point(centralPoint.x, centralPoint.y - side * i));
                }
                break;
            case SOUTH:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x + side * i, centralPoint.y));
                }
                break;
            case SOUTH_WEST:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x + side * i, centralPoint.y));
                    points.addPoint(new Point(centralPoint.x, centralPoint.y + side * i));
                }
                break;
            case WEST:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x, centralPoint.y + side * i));
                }
                break;
            case NORTH_WEST:
                for (int i = 3; i < 8; i++) {
                    points.addPoint(new Point(centralPoint.x - side * i, centralPoint.y));
                    points.addPoint(new Point(centralPoint.x, centralPoint.y + side * i));
                }
                break;
        }
        return points;
    }

}
