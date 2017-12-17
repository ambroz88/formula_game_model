package com.ambi.formula.gamemodel.datamodel;

/**
 * This class represent one point given by coordinate X and Y. The point has
 * also information about his position to track. It is good for special cases
 * <b>e.g.</b> position on FINISH line.
 *
 * @author Jiri Ambroz
 */
public class Point {

    public final static String LEFT = "leftCol", RIGHT = "rightCol";
    public final static String FINISH_LINE = "finishLine", FINISH = "finish", NORMAL = "normal";
    private String position;
    public double x, y;

    // =========== constructors ================
    public Point() {
        x = 0;
        y = 0;
        position = "normal";
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        position = "normal";
    }

    public Point(Point p) {
        x = p.x;
        y = p.y;
        position = p.getPosition();
    }
    //=======================================

    public int getX() {
        return (int) Math.round(x);
    }

    public int getY() {
        return (int) Math.round(y);
    }

    /**
     * This method checks this <code>point</code> has the same coordinates like
     * point p.
     *
     * @param p point which we want to check.
     * @return true if the points are the same. False if they are not.
     */
    public boolean isEqual(Point p) {
        return x == p.getX() && y == p.getY();
    }

    public void setPoint(Point p) {
        x = p.x;
        y = p.y;
    }

    /**
     * This setter sets the position of this point
     *
     * @param position could reach these values: leftCol, rightCol, FINISH,
     * FINISH_LINE, NORMAL
     */
    public void setPosition(String position) {
        this.position = position;
    }

    public String getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "x = " + getX() + ", y = " + getY();
    }

}
