package com.ambi.formula.gamemodel.datamodel;

import java.util.ArrayList;
import java.util.List;

import com.ambi.formula.gamemodel.utils.Calc;

/**
 * This class represents track of the race. It is composed from two polylines
 * (left and right) and next two polylines are: start and finish line. During
 * building the track there are recorded indexes of point (left and right) which
 * show where ends the last well placed track points.
 *
 * @author Jiri Ambroz
 */
public class Track {

    public static int LEFT = 1, RIGHT = -1, LIMIT_DIST = 15, LIMIT_NEXT = 5;
    private Polyline left, right, parallelLeft, parallelRight;
    private int leftIndex, rightIndex, leftWidth, rightWidth;
    private int maxWidth;
    private int maxHeight;
    private boolean ready;

    public Track() {
        left = new Polyline(Polyline.POLYLINE);
        right = new Polyline(Polyline.POLYLINE);
        parallelLeft = new Polyline(Polyline.POLYLINE);
        parallelRight = new Polyline(Polyline.POLYLINE);
        leftIndex = 0;
        rightIndex = 0;
        maxWidth = 0;
        maxHeight = 0;
        leftWidth = 3;
        rightWidth = 3;
        ready = false;
    }

    public Polyline getLine(int side) {
        if (side == LEFT) {
            return left();
        } else {
            return right();
        }
    }

    public Polyline getParallelLine(int side) {
        if (side == LEFT) {
            return getParallelLeft();
        } else {
            return getParallelRight();
        }
    }

    public Polyline getOppLine(int side) {
        if (side == LEFT) {
            return right();
        } else {
            return left();
        }
    }

    public void addPoint(int side, Point point) {
        getLine(side).addPoint(point);
        setReady((leftIndex == left().getLength() - 1 && left().getLength() > 1 && rightIndex >= right().getLength() - 4)
                || (rightIndex == right().getLength() - 1 && right().getLength() > 1 && leftIndex >= left().getLength() - 4));
        checkMaximum(point);
    }

    public void addParallelPoint(int side, Point point) {
        getParallelLine(side).addPoint(point);
    }

    public void addParallelPoint(int position, int side, Point point) {
        getParallelLine(side).addPoint(position, point);
    }

    public void removeLastPoint(int side) {
        getLine(side).removeLast();
        if (getIndex(side) > 0 && getIndex(side) >= getLine(side).getLength()) {
            setIndex(getIndex(side) - 1, side);
            if (side == LEFT) {
                if (getIndex(RIGHT) > getIndex(side)) {
                    setIndex(getIndex(RIGHT) - 1, RIGHT);
                }
            } else {
                if (getIndex(LEFT) > getIndex(side)) {
                    setIndex(getIndex(LEFT) - 1, LEFT);
                }
            }
        }
        calculateDimension();
    }

    public void calculateDimension() {
        for (int i = 0; i < getLong().getLength(); i++) {
            checkMaximum(getLong().getPoint(i));
            if (i < getShort().getLength()) {
                checkMaximum(getShort().getPoint(i));
            }
        }
    }

    public Polyline left() {
        return left;
    }

    public void setLeft(Polyline left) {
        this.left = left;
        leftIndex = left().getLength() - 1;
        leftWidth = 3;
    }

    public Polyline right() {
        return right;
    }

    public void setRight(Polyline right) {
        this.right = right;
        rightIndex = right().getLength() - 1;
        rightWidth = 3;
    }

    public Polyline getParallelLeft() {
        return parallelLeft;
    }

    public void setParallelLeft(Polyline parallelLeft) {
        this.parallelLeft = parallelLeft;
    }

    public Polyline getParallelRight() {
        return parallelRight;
    }

    public void setParallelRight(Polyline parallelRight) {
        this.parallelRight = parallelRight;
    }

    /**
     * This method selects longer side of the track when it is prepare for race.
     *
     * @return longer polyline
     */
    public Polyline getLong() {
        if (left().getLength() >= right().getLength()) {
            return left();
        } else {
            return right();
        }
    }

    /**
     * This method selects longer side of the track when it is prepare for race
     *
     * @return String of longer polyline (if it is LEFT or RIGHT side)
     */
    public int getLongStr() {
        //vrati delsi stranu trati jako text (left, right)
        if (left().getLength() >= right().getLength()) {
            return LEFT;
        } else {
            return RIGHT;
        }
    }

    /**
     * This method selects shorter side of the track when it is prepare for race
     *
     * @return String of shorter polyline (if it is LEFT or RIGHT side)
     */
    public int getShortStr() {
        if (left().getLength() < right().getLength()) {
            return LEFT;
        } else {
            return RIGHT;
        }
    }

    /**
     * This method selects shorter side of the track when it is prepare for
     * race.
     *
     * @return shorter polyline
     */
    public Polyline getShort() {
        if (left().getLength() < right().getLength()) {
            return left();
        } else {
            return right();
        }
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    private void checkMaximum(Point point) {
        if (point.x > maxWidth) {
            maxWidth = (int) point.x;
        }
        if (point.y > maxHeight) {
            maxHeight = (int) point.y;
        }
    }

    /**
     * This method returns actual index of well placed point to the track. You
     * have to specify in which side you are interested.
     *
     * @param side is String of side where you want to find index (right or
     * left)
     * @return left or right index
     */
    public int getIndex(int side) {
        if (side == LEFT) {
            return leftIndex;
        } else {
            return rightIndex;
        }
    }

    /**
     * This method sets actual index on specific side.
     *
     * @param index is position of new index
     * @param side means which side you want to set up
     */
    public void setIndex(int index, int side) {
        if (side == LEFT) {
            leftIndex = index;
        } else {
            rightIndex = index;
        }
    }

    public void setWidth(int side) {
        if (side == LEFT) {
            leftWidth = 4;
            rightWidth = 3;
        } else {
            rightWidth = 4;
            leftWidth = 3;
        }
    }

    public int getLeftWidth() {
        return leftWidth;
    }

    public void setLeftWidth(int leftWidth) {
        this.leftWidth = leftWidth;
    }

    public int getRightWidth() {
        return rightWidth;
    }

    public void setRightWidth(int rightWidth) {
        this.rightWidth = rightWidth;
    }

    //TODO: dokoncit drahu jak se slusi a patri
    public void finishIndexes() {
        if (left().getLength() - leftIndex >= right().getLength() - rightIndex) {
            for (int l = leftIndex + 1; l < left().getLength(); l++) {
                Point actPoint = left().getPoint(l);
                double oldDist = 5000, dist;
                int newIndex = rightIndex;
                for (int r = newIndex; r < right().getLength(); r++) {
                    dist = Calc.distance(actPoint, right().getPoint(r));
                    if (dist <= oldDist) {
                        oldDist = dist;
                        newIndex = r;
                    } else {
                        break;
                    }
                }
                if (newIndex != rightIndex) {
                    rightIndex = newIndex;
                }
                leftIndex++;
            }
        } else {
            for (int r = rightIndex + 1; r < right().getLength(); r++) {
                Point actPoint = right().getPoint(r);
                double oldDist = 5000, dist;
                int newIndex = leftIndex;
                for (int l = newIndex; l < left().getLength(); l++) {
                    dist = Calc.distance(actPoint, left().getPoint(l));
                    if (dist <= oldDist) {
                        oldDist = dist;
                        newIndex = l;
                    } else {
                        break;
                    }
                }
                if (newIndex != leftIndex) {
                    leftIndex = newIndex;
                }
                rightIndex++;
            }
        }
//        leftIndex = left.getLength() - 1;
//        rightIndex = right.getLength() - 1;
    }

    /**
     * This method returns two-points line which represents start line. Line
     * always leads from left to right side.
     *
     * @return polyline of start
     */
    public Polyline getStart() {
        Point begin = left().getPoint(0);
        Point end = right().getPoint(0);
        return new Polyline(begin, end);
    }

    /**
     * This method returns two-points line which represents finish line. Line
     * always leads from left to right side.
     *
     * @return polyline of finish
     */
    public Polyline getFinish() {
        Point begin = left().getLast();
        Point end = right().getLast();
        return new Polyline(begin, end);
    }

    public boolean isReadyForDraw() {
        return left().getLength() > 2 && right().getLength() > 2;
    }

    public boolean getReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * This method returns coordinates X and Y of whole track in order: left
     * side, right side from the end to the start. Also the length (in squares)
     * of the left and right side is measured.
     *
     * @param gridSize is size of the one square on the paper
     * @return 2-dimension array (2 rows) where row 0 means X and row 1 means Y
     */
    public int[][] getTrack(int gridSize) {
        int[] xPoints = new int[leftIndex + rightIndex + 2];
        int[] yPoints = new int[leftIndex + rightIndex + 2];
        for (int i = 0; i <= leftIndex; i++) {
            xPoints[i] = (int) left().getPoint(i).getX() * gridSize;
            yPoints[i] = (int) left().getPoint(i).getY() * gridSize;
        }
        for (int i = 0; i <= rightIndex; i++) {
            //right side has to be saved from the end to the start
            int opIndex = rightIndex - i;
            xPoints[leftIndex + i + 1] = (int) right().getPoint(opIndex).getX() * gridSize;
            yPoints[leftIndex + i + 1] = (int) right().getPoint(opIndex).getY() * gridSize;
        }
        int[][] track = {xPoints, yPoints};
        return track;
    }

    public List<Polyline> getTrackLines() {
        List<Polyline> lines = new ArrayList<>();
        int length;
        if (left().getLength() <= right().getLength()) {
            length = left().getLength();
        } else {
            length = right().getLength();
        }

        Polyline line;
        for (int i = 0; i < length; i++) {
            line = new Polyline(left().getPoint(i), right().getPoint(i));
            lines.add(line);
        }
        return lines;
    }

    public boolean freeDrawing(int side, int oppSide) {
        return getLine(side).getLength() > getIndex(side) && getIndex(oppSide) >= getLine(oppSide).getLength() - 2;
    }

    /**
     * This method change left side to right reverse side and right side change
     * to left reverse.
     */
    public void switchStart() {
        Polyline tempRight = new Polyline(right());
        Polyline tempLeft = new Polyline(left());
        left().clear();
        right().clear();
        left = tempRight.reverse();
        right = tempLeft.reverse();
        leftIndex = left().getLength() - 1;
        rightIndex = right().getLength() - 1;
    }

    public void reset() {
        left.clear();
        right.clear();
        parallelLeft.clear();
        parallelRight.clear();
        leftIndex = 0;
        rightIndex = 0;
        maxWidth = 0;
        maxHeight = 0;
    }

    public Track getTrack() {
        return this;
    }

    @Override
    public String toString() {
        return "left size = " + left().getLength() + ", right size = " + right().getLength();
    }

}
