package com.ambi.formula.gamemodel.datamodel;

import com.ambi.formula.gamemodel.utils.Calc;

/**
 * This class <code>polyline</code> represent <code>ArrayList</code> of <code>points</code>. It
 * could means line or just separate point which hasn't any order in <code>Arraylist</code>.
 *
 * @author Jiri Ambroz
 */
public class Polyline extends Segment {

    /**
     * Basic constructor which create empty polyline.
     */
    public Polyline() {
        super();
    }

    /**
     * Constructor which create new polyline from input polyline (copy).
     *
     * @param poly
     */
    public Polyline(Polyline poly) {
        super();
        for (Point p : poly.points) {
            this.points.add(new Point(p));
        }
    }

    //=============== POINT OPERATIONS =================
    /**
     * This method adds <code>point p</code> at the end of this <code>polyline</code>.
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
     * This method inserts <code>point p</code> on certain position.Rest of the points will move
     * over one position closer to the end.
     *
     * @param p is point which we want to add
     * @param pos is position in <code>polyline</code> where we want to add the point.
     */
    public void insPoint(Point p, int pos) {
        points.add(pos, p);
    }

    /**
     * This method overwrite point on position <code>pos</code> in this polyline with new
     * <code>point p</code>. Point whis is on position <code>pos</code> will be delete.
     *
     * @param p is new point which we want to add to
     * @param pos is position where the point will be insert
     */
    public void changePoint(Point p, int pos) {
        points.set(pos, new Point(p));
    }

    public Point getPoint(int index) {
        return points.get(index);
    }

    public Point getPreLast() {
        if (points.size() > 1) {
            return points.get(getLength() - 2);
        }
        return null;
    }

    public Segment getSegment(int position) {
        return new Segment(getPoint(position), getPoint(position + 1));
    }

    public Segment getLastSegment() {
        return getSegment(getLength() - 2);
    }

    public void removePoint(int index) {
        points.remove(index);
    }

    public void removeLast() {
        if (!points.isEmpty()) {
            points.remove(points.size() - 1);
        }
    }

    //===================== OTHER OPERATIONS =====================
    public boolean isEmpty() {
        return points.isEmpty();
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
            Polyline result = new Polyline();
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
     * This method makes opposite order of points in polyline. First point in polyline become the
     * last etc.
     *
     * @return new polyline with opposite points order
     */
    public Polyline reverse() {
        Polyline copy = new Polyline();
        for (int i = points.size() - 1; i >= 0; i--) {
            copy.addPoint(points.get(i));
        }
        return copy;
    }

    /**
     * This method controls if segment <code>Polyline line.last()</code> and Point click crosses or
     * touches any of the rest of segment in line.
     *
     * @param click point where the track will be constructed
     * @return true if there is a colision
     */
    public boolean checkOwnCrossing(Point click) {
        boolean crossed = false;
        if (getLength() > 1) {
            Point last = getLast();
            //prochazeni usecek leve krajnice od prvni do posledni usecky
            for (int i = 0; i < getLength() - 2; i++) {
                //kontrola mozne kolize usecek:
                if ((int) Calc.crossing(last, click, getSegment(i))[0] != Calc.OUTSIDE) {
                    crossed = true;
                    break;
                }
            }
        }
        return crossed;
    }

    /**
     * This method controls if segment <code>line</code> and Point click crosses or touches any of
     * the rest of segment in line.
     *
     * @param last last point of line which is constructed
     * @param click point where the track will be constructed
     * @return true if there is a colision
     */
    public boolean checkSegmentCrossing(Point last, Point click) {
        if (getLength() > 1) {
            //prochazeni usecek leve krajnice od prvni do posledni usecky
            for (int i = 0; i < getLength() - 1; i++) {
                //kontrola mozne kolize usecek:
                if ((int) Calc.crossing(last, click, getSegment(i))[0] != Calc.OUTSIDE) {
                    return true;
                }
            }
        }
        return false;
    }

}
