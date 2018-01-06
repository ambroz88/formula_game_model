package com.ambi.formula.gamemodel.datamodel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jiri Ambroz
 */
public class Turns {

    private Turn[] turns;

    public Turns() {
        turns = new Turn[9];
        for (int i = 0; i < 9; i++) {
            turns[i] = new Turn();
        }
    }

    public int getSize() {
        return turns.length;
    }

    public List<Point> getPoints() {
        List<Point> points = new ArrayList<>();
        for (Turn turn : turns) {
            if (turn.isExist() && turn.getType() == 1) {
                points.add(turn.getPosition());
            }
        }
        return points;
    }

    public List<Point> getBadPoints() {
        List<Point> points = new ArrayList<>();
        for (Turn turn : turns) {
            if (turn.isExist() && turn.getType() == 0) {
                points.add(turn.getPosition());
            }
        }
        return points;
    }

    public Turn getTurn(int pos) {
        return turns[pos];
    }

    public void reset() {
        turns = new Turn[9];
        for (int i = 0; i < 9; i++) {
            turns[i] = new Turn();
        }
    }

    @Override
    public String toString() {
        return "Size: " + turns.length;
    }

    public class Turn {

        private Point position;
        private int type; //0 is bad, 1 is good
        private boolean exist;

        public Turn() {
            position = new Point();
            type = 1;
            exist = true;
        }

        public Point getPosition() {
            return position;
        }

        public void setPosition(Point position) {
            this.position = position;
            setExist(true);
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
            setExist(true);
        }

        public boolean isExist() {
            return exist;
        }

        public void setExist(boolean exist) {
            this.exist = exist;
        }

    }

}
