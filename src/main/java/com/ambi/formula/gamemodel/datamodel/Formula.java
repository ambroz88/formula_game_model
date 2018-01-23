package com.ambi.formula.gamemodel.datamodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * This class represents formula. It is polyline which has some special functions and variables. In
 * drawing this polyline has the arrows in each point so it looks like a vector.
 *
 * @author Jiri Ambroz
 */
public class Formula extends Polyline {

    public static final String SIDE = "side";
    public static final String FORWARD = "forward";

    private int speed, side, moves, wait;
    private Segment colLine; //two-points line to which this formula crashed
    private int rgbColor;
    private String name;
    private double length; //actual distance which formula took
    private boolean winner;//is true when this formula finished the race
    private final PropertyChangeSupport prop;

    public Formula() {
        super();
        speed = 1; //size of movement on axis Y
        side = 0; //size of movement on axis X
        winner = false;
        moves = 1; //numbers of turns of this formula
        length = 1;
        wait = 0;
        prop = new PropertyChangeSupport(this); //every fire is cought by StatisticPanel
    }

    /**
     * Reset of global variables to start values.
     */
    public void reset() {
        winner = false;
        points.clear();
        moves = 1;
        length = 1;
        wait = 0;
        prop.firePropertyChange("reset", false, true);
    }

    @Override
    public void addPoint(Point p) {
        super.addPoint(p);
    }

    public int getColor() {
        return rgbColor;
    }

    public void setColor(int color) {
        int old = getColor();
        this.rgbColor = color;
        prop.firePropertyChange("color", old, color); //caught by PlayerPanel and Statistic panel
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String old = getName();
        this.name = name;
        prop.firePropertyChange("name", old, name);
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setSide(int side) {
        this.side = side;
    }

    public int getSpeed() {
        if (points.size() < 2) {
            return speed;
        } else {
            return (int) (points.get(points.size() - 1).getY() - points.get(points.size() - 2).getY());
        }
    }

    /**
     * This method counts the size of vertical movement of this formula if it goes to
     * <code>point turn</code>.
     *
     * @param turn is point where the formula is going
     * @return number of grid squeres
     */
    public int getSpeed(Point turn) {
        return (int) (turn.getY() - points.get(points.size() - 1).getY());
    }

    public int getSide() {
        if (points.size() < 2) {
            return side;
        } else {
            return (int) (points.get(points.size() - 1).getX() - points.get(points.size() - 2).getX());
        }
    }

    /**
     * This method counts the size of horizontal movement of this formula if it goes to
     * <code>point turn</code>.
     *
     * @param turn is point where the formula is going
     * @return number of grid squeres
     */
    public int getSide(Point turn) {
        if (points.size() < 2) {
            return side;
        } else {
            return (int) (turn.getX() - points.get(points.size() - 1).getX());
        }
    }

    public int maxSpeed() {
        int maxSpeed = Math.abs((int) (points.get(points.size() - 1).getY() - points.get(points.size() - 2).getY()));
        int maxSide = Math.abs((int) (points.get(points.size() - 1).getX() - points.get(points.size() - 2).getX()));
        if (maxSpeed > maxSide) {
            return maxSpeed;
        } else {
            return maxSide;
        }
    }

    /**
     * This method counts the maximum movement size of this formula if it goes to
     * <code>point click</code>.
     *
     * @param click is point where the formula is going
     * @return number of grid squeres
     */
    public int maxSpeed(Point click) {
        int maxSpeed = Math.abs((int) (click.getY() - points.get(points.size() - 1).getY()));
        int maxSide = Math.abs((int) (click.getX() - points.get(points.size() - 1).getX()));
        if (maxSpeed > maxSide) {
            return maxSpeed;
        } else {
            return maxSide;
        }
    }

    /**
     * This method counts the minimum movement size of this formula if it goes to
     * <code>point click</code>.
     *
     * @param click is point where the formula is going
     * @return number of grid squeres
     */
    public int minSpeed(Point click) {
        int maxSpeed = Math.abs((int) (click.getY() - points.get(points.size() - 1).getY()));
        int maxSide = Math.abs((int) (click.getX() - points.get(points.size() - 1).getX()));
        if (maxSpeed < maxSide) {
            return maxSpeed;
        } else {
            return maxSide;
        }
    }

    /**
     * This method finds out which movement direction would be more dominant if this formula would
     * move to <code>point click</code>. If it is horizontal or vertical movement.
     *
     * @param click is possible movement point
     * @return String with dominant direction
     */
    public String maxDirect(Point click) {
        int maxSpeed = Math.abs((int) (click.getY() - points.get(points.size() - 1).getY()));
        int maxSide = Math.abs((int) (click.getX() - points.get(points.size() - 1).getX()));
        if (maxSpeed > maxSide) {
            return FORWARD;
        } else {
            return SIDE;
        }
    }

    public void setColision(Segment colLine) {
        this.colLine = colLine;
    }

    /**
     * Get colision segment in which the formula crashed (in case of colision)
     *
     * @return colision segment
     */
    public Segment getColision() {
        return colLine;
    }

    public void setWin(boolean win) {
        winner = win;
    }

    public boolean getWin() {
        return winner;
    }

    public void setWait(int wait) {
        int old = getWait();
        this.wait = wait;
        prop.firePropertyChange("stop", old, wait);
    }

    public int getWait() {
        return wait;
    }

    public void movesUp() {
        moves++;
        prop.firePropertyChange("move", 0, moves);
    }

    public void movesUp(int count) {
        moves = moves + count;
        prop.firePropertyChange("move", 0, moves);
    }

    public int getMoves() {
        return moves;
    }

    /**
     * This method updates the distance of the formula about the distance between two last points.
     */
    public void lengthUp() {
        Point p1 = points.get(points.size() - 2);
        Point p2 = points.get(points.size() - 1);

        double dist = Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2)
                + Math.pow(p2.getY() - p1.getY(), 2));
        length = length + Math.round(dist * 100.0) / 100.0;
        prop.firePropertyChange("dist", 0, length);
    }

    /**
     * This method updates the distance of the formula about the distance between
     * <code>point p1</code> and <code>point p2</code>. Distance is rounded to 2 decimals.
     *
     * @param p1 first point
     * @param p2 second point
     */
    public void lengthUp(Point p1, Point p2) {
        double dist = Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2)
                + Math.pow(p2.getY() - p1.getY(), 2));
        length = Math.round((length + dist) * 100.0) / 100.0;
        prop.firePropertyChange("dist", 0, length);
    }

    public double getDist() {
        return length;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        prop.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        prop.removePropertyChangeListener(listener);
    }

}
