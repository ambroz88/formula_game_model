package com.ambi.formula.gamemodel.utils;

import java.util.ArrayList;
import java.util.List;

import com.ambi.formula.gamemodel.TrackBuilder;
import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Polyline;
import com.ambi.formula.gamemodel.datamodel.Track;

/**
 * This is a class with different matematical operations and methods which are
 * static.
 *
 * @author Jiri Ambroz
 */
public abstract class Calc {

    public static final int OUTSIDE = -1;
    public static final int EDGE = 0;
    public static final int INSIDE = 1;

    /**
     * This method finds out if two segments have intersect. One segment is
     * defined by two separated points. Second segment is defined by polyline.
     * It doesn't matter on order.
     *
     * @param a is first point of first segment
     * @param b is second point of first segment
     * @param edge is second segment (polyline with length = 2)
     * @return ArrayList of length 2. First value is Integer which means if
     * there is intersect (1 for intersect, 0 for touch and -1 for no intersect.
     * Second value in List is Point where is the intersect.
     */
    public static Object[] crossing(Point a, Point b, Polyline edge) {
        Point c = edge.getPreLast();
        Point d = edge.getLast();
        return crossing(a, b, c, d);
    }

    /**
     * This method finds out if two segments have intersect. Both segments are
     * defined by two separated points. It doesn't matter on direction of the
     * segment.
     *
     * @param a is first point of first segment
     * @param b is second point of first segment
     * @param c is first point of second segment
     * @param d is second point of second segment
     * @return ArrayList of length 2. First value is Integer which means if
     * there is intersect (1 for intersect, 0 for touch and -1 for no intersect.
     * Second value in List is Point where is the intersect.
     */
    public static Object[] crossing(Point a, Point b, Point c, Point d) {
        int intersect = OUTSIDE;
        Point colPoint = new Point();

        //t vychazi z parametrickeho vyjadreni primky
        double t = (a.x * d.y - a.x * c.y - c.x * d.y - d.x * a.y + d.x * c.y + c.x * a.y)
                / ((d.x - c.x) * (b.y - a.y) - (b.x - a.x) * (d.y - c.y));

        if (!Double.isInfinite(t)) { //kdyz usecky nejsou rovnobezne
            // souradnice potencialniho pruseciku
            double X = a.x + (b.x - a.x) * t;
            double Y = a.y + (b.y - a.y) * t;
            colPoint = new Point(X, Y); //cannot be rounded!!!
            //usecky se protinaji uvnitr
            if (pointPosition(a, b, colPoint) == 1 && pointPosition(c, d, colPoint) == 1) {
                intersect = INSIDE;
            } //usecky se spolecne dotykaji v jednom konci
            else if (pointPosition(a, b, colPoint) == 0 && pointPosition(c, d, colPoint) == 0) {
                intersect = EDGE;
            } //konec jedne usecky se dotyka vnitrku druhe usecky
            else if ((pointPosition(a, b, colPoint) == 0 && pointPosition(c, d, colPoint) == 1)
                    || (pointPosition(c, d, colPoint) == 0 && pointPosition(a, b, colPoint) == 1)) {
                intersect = EDGE;
            }
        }
        return new Object[]{intersect, colPoint};
    }

    /**
     * Metoda urcuje pozici bodu inter vuci usecce AB
     *
     * @param a zacatecni bod usecky
     * @param b koncovy bod usecky
     * @param inter
     * @return - hodnoty: 1 pro polohu uvnitr usecky, 0 pro polohu na kraji a -1
     * kdyz lezi mimo usecku
     */
    private static int pointPosition(Point a, Point b, Point inter) {
        double ix = inter.x;
        double iy = inter.y;

        if (inter.isEqual(a) || inter.isEqual(b)) {
            return EDGE;
        } else if ((ix >= a.x && ix <= b.x || ix <= a.x && ix >= b.x)
                && (iy >= a.y && iy <= b.y || iy <= a.y && iy >= b.y)) {
            return INSIDE;
        } else {
            return OUTSIDE;
        }
    }

    /**
     * This method finds the nearest point in Polyline to point last.
     *
     * @param last is the point from which the distance is measure
     * @param data is polyline where is searching the closest point
     * @return the point from polyline (array 1) and it's position in polyline
     * (array 0).
     */
    public static List<Object> findNearest(Point last, Polyline data) {
        int minIndex = 0;
        for (int i = 1; i < data.getLength(); i++) {
            if (distance(last, data.getPoint(minIndex)) > distance(last, data.getPoint(i))) {
                minIndex = i;
            }
        }
        List<Object> result = new ArrayList<>();
        result.add(minIndex);
        result.add(data.getPoint(minIndex));
        return result;
    }

    /**
     * This method create point in the angle axis which is given by tree points.
     * It is possible to say on which side that point should be create.
     *
     * @param prev is first point
     * @param mid is second point
     * @param next is third point
     * @param side is side from polyline where new point should be created (1
     * means left, 2 means right)
     * @return point in the angle axis on given side from polyline
     */
    public static Point calculateAngle(Point prev, Point mid, Point next, int side) {
        double a = distance(mid, next);
        double b = distance(prev, mid);
        double c = distance(prev, next);
        double gamma;
        if (b == a) {
            gamma = Math.PI / 2;
        } else {
            gamma = Math.acos((c * c - a * a - b * b) / (-2 * a * b));
        }
        if (sidePosition(next, new Polyline(prev, mid)) != side) {
            //subtrack angle from 180°
            gamma = 2 * Math.PI - gamma;
        }
        if (side == Track.RIGHT) {
            gamma = -gamma;
        }
        return rotatePoint(prev, mid, gamma / 2, 10);
    }

    /**
     * This method rotates one point around another point. Parameters of this
     * rotation are: angle and new distance from center of rotation.
     *
     * @param rotated is point which is rotated
     * @param center is central point of rotation
     * @param angle is rotation angle in radians
     * @param newLength is new distance between center and rotated point
     * @return point with new coordinates
     */
    public static Point rotatePoint(Point rotated, Point center, double angle, double newLength) {
        double koef = newLength / distance(rotated, center);
        double tempX = rotated.x + (center.x - rotated.x) * (1 - koef);
        double tempY = rotated.y + (center.y - rotated.y) * (1 - koef);
        double endX = (tempX - center.x) * Math.cos(angle) - (tempY - center.y) * Math.sin(angle);
        double endY = (tempX - center.x) * Math.sin(angle) + (tempY - center.y) * Math.cos(angle);
        return new Point((int) (center.x + endX), (int) (center.y + endY));
    }

    /**
     * This method calculate distance between segment (2 points) and Point.
     *
     * @param segment is polyline with 2 points
     * @param p - distance of this point is calculated
     * @return distance in pixels
     */
    public static double distFromSegment(Polyline segment, Point p) {
        Point a = segment.getPoint(0);
        Point b = segment.getPoint(1);

        double segmentLength = Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
        double res = Math.abs((p.x - a.x) * (b.y - a.y) - (p.y - a.y) * (b.x - a.x)) / segmentLength;
        return res;
    }

    /**
     * It computes coordinates of the base of altitude when there is point and
     * segment.
     *
     * @param segment is two points line to which is searched the base
     * @param p is point from which leads the segment to base of altitude
     * @return coordinates (Point) of base of altitude
     */
    public static Point baseOfAltitude(Polyline segment, Point p) {
        Point a = segment.getPoint(0);
        Point b = segment.getPoint(1);

        double ux = b.x - a.x;
        double uy = b.y - a.y;
        double nx = -uy;
        double ny = ux;

        double s = (p.y * ux - a.y * ux - p.x * uy + a.x * uy) / (nx * uy - ny * ux);
        double X = p.x + s * nx;
        double Y = p.y + s * ny;

        return new Point((int) X, (int) Y);
    }

    /**
     * This method calculates the distance between two points.
     *
     * @param p1 is first point
     * @param p2 is second point
     * @return distance between point p1 and p2 in 2 decimal numbers
     */
    public static double distance(Point p1, Point p2) {
        double dist = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
        return Math.round(dist * 100.0) / 100.0;
    }

    /**
     * This method calculates quadratic equation (with two roots).
     *
     * @param a is first parameter of equation
     * @param b is second parameter of equation
     * @param c is third parameter of equation
     * @return List of roots (length can be 0-2)
     */
    public static List<Double> quadratic(double a, double b, double c) {
        List result = new ArrayList<>();
        double d = b * b - 4 * a * c;
        if (d > 0) {
            double t1 = (-b + Math.sqrt(d)) / (2 * a);
            double t2 = (-b - Math.sqrt(d)) / (2 * a);
            result.add(t1);
            result.add(t2);
        } else if (d == 0) {
            result.add(-b / (2 * a));
        }
        return result;
    }

    /**
     * <html>This method calculetes the code of direction kvadrant of segment
     * which is given by two points. It depends on order of the segment.
     * Possible directions:<br>
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
            quad = TrackBuilder.NORTH;
        } else if (ux > 0 && uy < 0) {
            quad = TrackBuilder.NORTH_EAST;
        } else if (ux > 0 && uy == 0) {
            quad = TrackBuilder.EAST;
        } else if (ux > 0 && uy > 0) {
            quad = TrackBuilder.SOUTH_EAST;
        } else if (ux == 0 && uy > 0) {
            quad = TrackBuilder.SOUTH;
        } else if (ux < 0 && uy > 0) {
            quad = TrackBuilder.SOUTH_WEST;
        } else if (ux < 0 && uy == 0) {
            quad = TrackBuilder.WEST;
        } else if (ux < 0 && uy < 0) {
            quad = TrackBuilder.NORTH_WEST;
        }
        return quad;
    }

    public static Point rightAngle(Polyline edge, int side) {
        //kolmice z posledniho bodu vstupni usecky:
        Point start = edge.getLast();//z tohoto bodu bude spustena kolmice
        //smerovy vektor pro vychozi hranu na delsi strane:
        double ux = edge.getPreLast().x - start.x;
        double uy = edge.getPreLast().y - start.y;
        double nx = -uy;
        double ny = ux;
        double t = 1000;
        if (side == Track.LEFT) {
            nx = uy;
            ny = -ux;
        }
        // souradnice potencialniho pruseciku:
        double X = (start.x + nx * t);
        double Y = (start.y + ny * t);
        return new Point((int) X, (int) Y);
    }

    /**
     * Metoda zjisti, na jake strane lezi bod center od kolizni usecky. Poloha
     * na usecce je zahrnuta do polohy vpravo.
     *
     * @param center vstupni porovnavany bod
     * @param colLine kolizni usecka, od ktere se uvazuje poloha bodu
     * @return 1 if point si on the left or 2 if it is on the right
     */
    public static int sidePosition(Point center, Polyline colLine) {
        double ux = colLine.getLast().x - colLine.getPreLast().x;
        double uy = colLine.getLast().y - colLine.getPreLast().y;
        double vx = center.x - colLine.getPreLast().x;
        double vy = center.y - colLine.getPreLast().y;

        double t = ux * vy - uy * vx; // skalarni soucin dvou vektoru
        if (t >= 0) {
            return Track.RIGHT;
        } else {
            return Track.LEFT;
        }
    }

}
