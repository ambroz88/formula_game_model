package com.ambi.formula.gamemodel.datamodel;

import java.awt.Dimension;

/**
 *
 * @author Jiri Ambroz
 */
public class Paper {

    private Polyline horizontal, vertical;
    private final Dimension dim;

    /**
     * It calculates two polylines which compose square paper.
     */
    public Paper() {
        this.dim = new Dimension(90, 50);
        updateGrid();
    }

    /**
     * It calculates two polylines which compose square paper.
     */
    private void updateGrid() {
        vertical = new Polyline(Polyline.SEGMENT);
        horizontal = new Polyline(Polyline.SEGMENT);
        //create vertical lines of square paper
        for (int x = 0; x <= dim.width; x++) {
            vertical.addPoint(new Point(x, 0));
            vertical.addPoint(new Point(x, dim.height));
        }
        //create horizontal lines of square paper
        for (int y = 0; y <= dim.height; y++) {
            horizontal.addPoint(new Point(0, y));
            horizontal.addPoint(new Point(dim.width, y));
        }
    }

    public int getWidth() {
        return dim.width;
    }

    public void setWidth(int width) {
        dim.width = width;
        updateGrid();
    }

    public int getHeight() {
        return dim.height;
    }

    public void setHeight(int height) {
        dim.height = height;
        updateGrid();
    }

    /**
     * It checks whether the input point is inside this paper.
     *
     * @param click point with square coordinates
     * @return true if point is inside, false otherwise
     */
    public boolean isOutside(Point click) {
        return click.x > dim.width || click.y > dim.height || click.x < 0 || click.y < 0;
    }

    public Polyline getHorizontalLines() {
        return horizontal;
    }

    public Polyline getVerticalLines() {
        return vertical;
    }

}
