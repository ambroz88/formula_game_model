package com.ambi.formula.gamemodel.datamodel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jiri Ambroz
 */
public class Segment {

    protected List<Point> points;

    protected Segment() {
        points = new ArrayList<>();
    }

    /**
     * Constructor for creation line segment (two points).
     *
     * @param a is first point of line
     * @param b is second point of line
     */
    public Segment(Point a, Point b) {
        points = new ArrayList<>();
        points.add(new Point(a));
        points.add(new Point(b));
    }

    public List<Point> getPoints() {
        return points;
    }

    public Point getFirst() {
        if (!points.isEmpty()) {
            return points.get(0);
        }
        return null;
    }

    public Point getLast() {
        if (!points.isEmpty()) {
            return points.get(getLength() - 1);
        }
        return null;
    }

    /**
     * Method for counting coordinates of point in the middle of the first and last point in this
     * polyline.
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

    //=================== GETTERS ====================
    public int getLength() {
        return points.size();
    }

    @Override
    public String toString() {
        return "size = " + points.size();
    }

}
