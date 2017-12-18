package com.ambi.formula.gamemodel.datamodel;

import java.util.ArrayList;

/**
 * This class <code>polyline</code> represent <code>ArrayList</code> of
 * <code>points</code>. It could means line or just separate point which hasn't
 * any order in <code>Arraylist</code>.
 *
 * @author Jiri Ambroz
 */
public class Polyline {

    public static final int GOOD_SET = 0, CROSS_SET = 1, SEGMENT = 2, POLYLINE = 3;
    private final int type;
    protected ArrayList<Point> points;

    /**
     * Basic constructor which create empty polyline.
     *
     * @param type defines if it will be set of point, segment or polyline
     */
    public Polyline(int type) {
        this.type = type;
        points = new ArrayList<>();
    }

    /**
     * Constructor for creation line segment (two points).
     *
     * @param a is first point of line
     * @param b is second point of line
     */
    public Polyline(Point a, Point b) {
        points = new ArrayList<>();
        points.add(new Point(a));
        points.add(new Point(b));
        type = SEGMENT;
    }

    /**
     * Constructor which create new polyline from input polyline (copy).
     *
     * @param poly
     */
    public Polyline(Polyline poly) {
        this.points = new ArrayList<>();
        for (Point p : poly.points) {
            this.points.add(new Point(p));
        }
        type = poly.getType();
    }

    public int getType() {
        return type;
    }

    /**
     * This method controls if the point click is one of the point in polyline
     *
     * @param click is <code>point</code> which is controlled
     * @return true if the point is part of polyline and false if not
     */
    public boolean isInside(Point click) {
        for (Point point : points) {
            if (click.isEqual(point) == true) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method adds <code>point p</code> at the end of this
     * <code>polyline</code>.
     *
     * @param p is point which we want to add
     */
    public void addPoint(Point p) {
        points.add(p);
    }

    public void addPoint(int position, Point p) {
        points.add(position, p);
    }

    /**
     * This method inserts <code>point p</code> on certain position.Rest of the
     * points will move over one position closer to the end.
     *
     * @param p is point which we want to add
     * @param pos is position in <code>polyline</code> where we want to add the
     * point.
     */
    public void insPoint(Point p, int pos) {
        points.add(pos, p);
    }

    /**
     * This method overwrite point on position <code>pos</code> in this polyline
     * with new <code>point p</code>. Point whis is on position <code>pos</code>
     * will be delete.
     *
     * @param p is new point which we want to add to
     * @param pos - pozice pridavaneho bodu
     */
    public void changePoint(Point p, int pos) {
        points.set(pos, new Point(p));
    }

    public Point getPoint(int index) {
        return points.get(index);
    }

    public void removePoint(int index) {
        points.remove(index);
    }

    public void removeLast() {
        if (!points.isEmpty()) {
            points.remove(points.size() - 1);
        }
    }

    public int getLength() {
        return points.size();
    }

    public Point getLast() {
        if (points.size() > 0) {
            return points.get(getLength() - 1);
        }
        return null;
    }

    public Point getPreLast() {
        if (points.size() > 1) {
            return points.get(getLength() - 2);
        }
        return null;
    }

    /**
     * Method for counting coordinates of point in the middle of the first and
     * last point in this polyline.
     *
     * @return point in the middle of polyline
     */
    public Point getMidPoint() {
        if (this.getLength() > 1) {
            double midX = (points.get(0).getX() + points.get(points.size() - 1).getX()) / 2;
            double midY = (points.get(0).getY() + points.get(points.size() - 1).getY()) / 2;
            return new Point((int) midX, (int) midY);
        } else {
            return null;
        }
    }

    /**
     * This method makes selection from polyline between two indexes.
     *
     * @param start is index of point where the selection starts
     * @param end is index of point where the selection ends
     * @return new "sub" polyline
     */
    public Polyline choose(int start, int end) {
        if (start <= end && start < this.getLength() && end < this.getLength()) {
            Polyline result = new Polyline(POLYLINE);
            for (int i = start; i <= end; i++) {
                result.addPoint(points.get(i));
            }
            return result;
        } else {
            return this;
        }
    }

    public void clear() {
        points.clear();
    }

    /**
     * This method makes opposite order of points in polyline. First point in
     * polyline become the last etc.
     *
     * @return new polyline with opposite points order
     */
    public Polyline reverse() {
        Polyline copy = new Polyline(POLYLINE);
        for (int i = points.size() - 1; i >= 0; i--) {
            copy.addPoint(points.get(i));
        }
        return copy;
    }

    @Override
    public String toString() {
        return "size = " + points.size();
    }

}