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

    public List<Point> getFreePoints() {
        List<Point> freePoints = new ArrayList<>();
        for (Turn turn : turns) {
            if (turn.isExist() && turn.getType() == Turn.FREE) {
                freePoints.add(turn.getPoint());
            }
        }
        return freePoints;
    }

    public List<Turn> getFreeTurns() {
        List<Turn> freeTurns = new ArrayList<>();
        for (Turn turn : turns) {
            if (turn.isExist() && turn.getType() == Turn.FREE) {
                freeTurns.add(turn);
            }
        }
        return freeTurns;
    }

    public List<Point> getCollisionPoints() {
        List<Point> collisionPoints = new ArrayList<>();
        for (Turn turn : turns) {
            if (turn.isExist() && turn.getType() == Turn.COLLISION) {
                collisionPoints.add(turn.getPoint());
            }
        }
        return collisionPoints;
    }

    public List<Turn> getCollisionTurns() {
        List<Turn> collisionTurns = new ArrayList<>();
        for (Turn turn : turns) {
            if (turn.isExist() && turn.getType() == Turn.COLLISION) {
                collisionTurns.add(turn);
            }
        }
        return collisionTurns;
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

        public static final int FREE = 1;
        public static final int COLLISION = 0;
        private Point point;
        private Point collision;
        private int type; //0 is bad, 1 is good
        private boolean exist;

        public Turn() {
            point = new Point();
            type = FREE;
            exist = true;
        }

        public Point getPoint() {
            return point;
        }

        public void setPoint(Point point) {
            this.point = point;
            setExist(true);
        }

        public Point getCollision() {
            return collision;
        }

        public void setCollision(Point collision) {
            this.collision = collision;
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
