package com.ambi.formula.gamemodel.datamodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author Jiri Ambroz
 */
public class Paper {

    private final PropertyChangeSupport prop;
    private Polyline horizontal;
    private Polyline vertical;
    private int gridSize;
    private int width;
    private int height;

    /**
     * It calculates two polylines which compose square paper.
     */
    public Paper() {
        this.width = 90;
        this.height = 50;
        gridSize = 15;
        prop = new PropertyChangeSupport(this);
        updateGrid();
    }

    /**
     * It calculates two polylines which compose square paper.
     */
    private void updateGrid() {
        vertical = new Polyline(Polyline.SEGMENT);
        horizontal = new Polyline(Polyline.SEGMENT);
        //create vertical lines of square paper
        for (int x = 0; x <= getWidth(); x++) {
            vertical.addPoint(new Point(x, 0));
            vertical.addPoint(new Point(x, getHeight()));
        }
        //create horizontal lines of square paper
        for (int y = 0; y <= getHeight(); y++) {
            horizontal.addPoint(new Point(0, y));
            horizontal.addPoint(new Point(getWidth(), y));
        }
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int paperWidth) {
        int old = getWidth();
        this.width = paperWidth;
        updateGrid();
        firePropertyChange("paperWidth", old, getWidth()); //cought by Draw and Options
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int paperHeight) {
        int old = getHeight();
        this.height = paperHeight;
        updateGrid();
        firePropertyChange("paperHeight", old, height); //cought by Draw and Options
    }

    public int getGridSize() {
        return gridSize;
    }

    /**
     * Setter for size of square edge on the "paper".
     *
     * @param size is new grid size
     */
    public void setGridSize(int size) {
        int old = getGridSize();
        gridSize = size;
        firePropertyChange("grid", old, getGridSize()); //cought by Draw
    }

    /**
     * It checks whether the input point is inside this paper.
     *
     * @param click point with square coordinates
     * @return true if point is inside, false otherwise
     */
    public boolean isOutside(Point click) {
        return click.x > getWidth() || click.y > getHeight() || click.x < 0 || click.y < 0;
    }

    /**
     * It returns number of points, which are outside of the visible part of
     * drawing window.
     *
     * @param data is set of points that will be tested
     * @return number of points outside of paper
     */
    public int outPaperNumber(Polyline data) {
        int outBorder = 0;

        for (int i = 0; i < data.getLength(); i++) {
            if (isOutside(data.getPoint(i))) {
                outBorder++;
            }
        }
        return outBorder;
    }

    public Polyline getHorizontalLines() {
        return horizontal;
    }

    public Polyline getVerticalLines() {
        return vertical;
    }

    public void firePropertyChange(String prop, Object oldValue, Object newValue) {
        this.prop.firePropertyChange(prop, oldValue, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        prop.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        prop.removePropertyChangeListener(listener);
    }

}
