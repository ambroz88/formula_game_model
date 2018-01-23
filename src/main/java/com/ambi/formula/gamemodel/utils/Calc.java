package com.ambi.formula.gamemodel.utils;

import java.util.ArrayList;
import java.util.List;

import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Segment;
import com.ambi.formula.gamemodel.datamodel.Track;
import com.ambi.formula.gamemodel.datamodel.Turns;

/**
 * This is a class with different matematical operations and methods which are static.
 *
 * @author Jiri Ambroz
 */
public abstract class Calc {

    public static final int OUTSIDE = -1;
    public static final int EDGE = 0;
    public static final int INSIDE = 1;

    /**
     * This method finds out if two segments have intersect. One segment is defined by two separated
     * points. Second segment is defined by polyline. It doesn't matter on order.
     *
     * @param a is first point of first segment
     * @param b is second point of first segment
     * @param edge is second segment (polyline with length = 2)
     * @return ArrayList of length 2. First value is Integer which means if there is intersect (1
     * for intersect, 0 for touch and -1 for no intersect. Second value in List is Point where is
     * the intersect.
     */
    public static Object[] crossing(Point a, Point b, Segment edge) {
        Point c = edge.getFirst();
        Point d = edge.getLast();
        return crossing(a, b, c, d);
    }

    /**
     * This method finds out if two segments have intersect. Both segments are defined by two
     * separated points. It doesn't matter on direction of the segment.
     *
     * @param a is first point of first segment
     * @param b is second point of first segment
     * @param c is first point of second segment
     * @param d is second point of second segment
     * @return ArrayList of length 2. First value is Integer which means if there is intersect (1
     * for intersect, 0 for touch and -1 for no intersect. Second value in List is Point where is
     * the intersect.
     */
    public static Object[] crossing(Point a, Point b, Point c, Point d) {
        int intersect = OUTSIDE;
        Point colPoint = calculateIntersect(a, b, c, d);

        if (colPoint != null) {
            if (pointPosition(a, b, colPoint) == INSIDE && pointPosition(c, d, colPoint) == INSIDE) {
                //usecky se protinaji uvnitr
                intersect = INSIDE;
            } else if (pointPosition(a, b, colPoint) == EDGE && pointPosition(c, d, colPoint) == EDGE) {
                //usecky se spolecne dotykaji v jednom konci
                intersect = EDGE;
            } else if ((pointPosition(a, b, colPoint) == EDGE && pointPosition(c, d, colPoint) == INSIDE)
                    || (pointPosition(c, d, colPoint) == EDGE && pointPosition(a, b, colPoint) == INSIDE)) {
                //konec jedne usecky se dotyka vnitrku druhe usecky
                intersect = EDGE;
            }
        }
        return new Object[]{intersect, colPoint};
    }

    public static Point halfLineAndSegmentIntersection(Segment segment, Point lineStart, Point lineEnd) {
        Point segmentStart = segment.getFirst();
        Point segmentEnd = segment.getLast();

        Point colPoint = calculateIntersect(segmentStart, segmentEnd, lineStart, lineEnd);
        if (colPoint != null) {
            int intersectPosition = pointPosition(segmentStart, segmentEnd, colPoint);
            if (intersectPosition == OUTSIDE || intersectPosition == INSIDE && isPointBehind(lineStart, lineEnd, colPoint)) {
                //line intersects the segment
                colPoint = null;
            }
        }
        return colPoint;
    }

    public static Point calculateCollisionPoint(Point segmentStart, Point segmentEnd, Point lineStart, Point lineEnd) {
        Point collision = calculateIntersect(segmentStart, segmentEnd, lineStart, lineEnd);

        if (collision != null && pointPosition(segmentStart, segmentEnd, collision) != INSIDE) {
            //line intersects the segment
            collision = null;
        }

        return collision;
    }

    private static Point calculateIntersect(Point a, Point b, Point c, Point d) {
        Point colPoint = null;

        //t vychazi z parametrickeho vyjadreni primky
        double t = (a.x * d.y - a.x * c.y - c.x * d.y - d.x * a.y + d.x * c.y + c.x * a.y)
                / ((d.x - c.x) * (b.y - a.y) - (b.x - a.x) * (d.y - c.y));

        if (!Double.isInfinite(t)) {
            //segments are not parallel
            double intersectX = a.x + (b.x - a.x) * t;
            double intersectY = a.y + (b.y - a.y) * t;
            //cannot be rounded!!!
            colPoint = new Point(intersectX, intersectY);
        }

        return colPoint;
    }

    /**
     * Metoda urcuje pozici bodu inter vuci usecce AB
     *
     * @param a zacatecni bod usecky
     * @param b koncovy bod usecky
     * @param inter
     * @return - hodnoty: 1 pro polohu uvnitr usecky, 0 pro polohu na kraji a -1 kdyz lezi mimo
     * usecku
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

    private static boolean isPointBehind(Point lineStart, Point lineEnd, Point collision) {
        boolean isBehind = false;
        if (!collision.isEqual(lineStart)) {

            double deltaX = lineEnd.x - lineStart.x;
            double deltaCollisionX = lineStart.x - collision.x;

            if (deltaX > 0 && deltaCollisionX > 0 || deltaX < 0 && deltaCollisionX < 0) {
                isBehind = true;
            } else if (deltaX == 0 && deltaCollisionX == 0) {
                double deltaY = lineEnd.y - lineStart.y;
                double deltaCollisionY = lineStart.y - collision.y;
                if (deltaY > 0 && deltaCollisionY > 0 || deltaY < 0 && deltaCollisionY < 0) {
                    isBehind = true;
                }
            }
        } else {
            isBehind = true;
        }

        return isBehind;
    }

    /**
     * This method finds the nearest point from the list of Points where the nearest one is
     * calculated from the point 'sourcePoint'.
     *
     * @param sourcePoint is the point from which the distance is measure
     * @param data is list of points where is searched the closest one
     * @return the nearest point in the list
     */
    public static Point findNearestPoint(Point sourcePoint, List<Point> data) {
        int minIndex = 0;
        for (int i = 1; i < data.size(); i++) {
            if (distance(sourcePoint, data.get(minIndex)) > distance(sourcePoint, data.get(i))) {
                minIndex = i;
            }
        }
        return data.get(minIndex);
    }

    /**
     * This method finds the nearest point from the list of Points where the nearest one is
     * calculated from the point 'sourcePoint'.
     *
     * @param sourcePoint is the point from which the distance is measure
     * @param data is list of points where is searched the closest one
     * @return the nearest point in the list
     */
    public static Point findNearestTurn(Point sourcePoint, List<Turns.Turn> data) {
        int minIndex = 0;
        for (int i = 1; i < data.size(); i++) {
            if (distance(sourcePoint, data.get(minIndex).getPoint()) > distance(sourcePoint, data.get(i).getPoint())) {
                minIndex = i;
            }
        }
        return data.get(minIndex).getPoint();
    }

    /**
     * This method finds position of the nearest point from the list of Points. The nearest one is
     * calculated from the point 'sourcePoint'.
     *
     * @param sourcePoint is the point from which the distance is measure
     * @param data is list of points where is searched the closest one
     * @return the nearest point in the list
     */
    public static int findNearestIndex(Point sourcePoint, List<Point> data) {
        int minIndex = 0;
        for (int i = 1; i < data.size(); i++) {
            if (distance(sourcePoint, data.get(minIndex)) > distance(sourcePoint, data.get(i))) {
                minIndex = i;
            }
        }
        return minIndex;
    }

    /**
     * This method create point in the angle axis which is given by tree points. It is possible to
     * say on which side that point should be create.
     *
     * @param prev is first point
     * @param mid is second point
     * @param next is third point
     * @param side is side from polyline where new point should be created (1 means left, 2 means
     * right)
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
        if (sidePosition(next, new Segment(prev, mid)) != side) {
            //subtrack angle from 180°
            gamma = 2 * Math.PI - gamma;
        }
        if (side == Track.RIGHT) {
            gamma = -gamma;
        }
        return rotatePoint(prev, mid, gamma / 2, 10);
    }

    /**
     * This method rotates one point around another point. Parameters of this rotation are: angle
     * and new distance from center of rotation.
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
     * @deprecated
     */
    public static double distFromSegment(Segment segment, Point p) {
        Point a = segment.getFirst();
        Point b = segment.getLast();

        double segmentLength = Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
        double res = Math.abs((p.x - a.x) * (b.y - a.y) - (p.y - a.y) * (b.x - a.x)) / segmentLength;
        return res;
    }

    /**
     * It computes coordinates of the base of altitude when there is point and segment.
     *
     * @param segment is two points line to which is searched the base
     * @param p is point from which leads the segment to base of altitude
     * @return coordinates (Point) of base of altitude
     */
    public static Point baseOfAltitude(Segment segment, Point p) {
        Point a = segment.getFirst();
        Point b = segment.getLast();

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

    public static double calculateHypotenuse(int side, int speed) {
        return Math.sqrt(side * side + speed * speed);
    }

    public static Point rightAngle(Segment edge, int side) {
        //kolmice z posledniho bodu vstupni usecky:
        Point start = edge.getLast();//z tohoto bodu bude spustena kolmice
        //smerovy vektor pro vychozi hranu na delsi strane:
        double ux = edge.getFirst().x - start.x;
        double uy = edge.getFirst().y - start.y;
        double nx;
        double ny;
        double t = 1000;
        if (side == Track.LEFT) {
            nx = uy;
            ny = -ux;
        } else {
            nx = -uy;
            ny = ux;
        }
        // souradnice potencialniho pruseciku:
        double X = (start.x + nx * t);
        double Y = (start.y + ny * t);
        return new Point((int) X, (int) Y);
    }

    /**
     * Metoda zjisti, na jake strane lezi bod center od kolizni usecky. Poloha na usecce je zahrnuta
     * do polohy vpravo.
     *
     * @param center vstupni porovnavany bod
     * @param colLine kolizni usecka, od ktere se uvazuje poloha bodu
     * @return 1 if point si on the left or 2 if it is on the right
     */
    public static int sidePosition(Point center, Segment colLine) {
        double ux = colLine.getLast().x - colLine.getFirst().x;
        double uy = colLine.getLast().y - colLine.getFirst().y;
        double vx = center.x - colLine.getFirst().x;
        double vy = center.y - colLine.getFirst().y;

        double t = ux * vy - uy * vx; // skalarni soucin dvou vektoru
        if (t >= 0) {
            return Track.RIGHT;
        } else {
            return Track.LEFT;
        }
    }

}
