package com.ambi.formula.gamemodel.datamodel;

/**
 *
 * @author Jiri Ambroz
 */
public class Paper {

    private Polyline horizontal;
    private Polyline vertical;
    private int width;
    private int height;

    /**
     * It calculates two polylines which compose square paper.
     */
    public Paper() {
        this.width = 90;
        this.height = 50;
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
        this.width = paperWidth;
        updateGrid();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int paperHeight) {
        this.height = paperHeight;
        updateGrid();
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

    public Polyline getHorizontalLines() {
        return horizontal;
    }

    public Polyline getVerticalLines() {
        return vertical;
    }

}
