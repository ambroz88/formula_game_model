package com.ambi.formula.gamemodel.turns;

import java.util.List;

import com.ambi.formula.gamemodel.GameModel;
import com.ambi.formula.gamemodel.datamodel.Formula;
import com.ambi.formula.gamemodel.datamodel.Point;
import com.ambi.formula.gamemodel.datamodel.Segment;
import com.ambi.formula.gamemodel.datamodel.Turns;
import com.ambi.formula.gamemodel.utils.Calc;

/**
 *
 * @author Jiri Ambroz
 */
public class ComputerModerate extends ComputerTurnCore {

    private Formula comp;
    private final GameModel model;
    private boolean sprint;

    public ComputerModerate(GameModel model) {
        this.model = model;
        sprint = false;
    }

    @Override
    public void reset() {
        setCheckLinesIndex(0);
        sprint = false;
    }

    @Override
    public Point selectComputerTurn() {
        comp = model.getTurnMaker().getFormula(model.getTurnMaker().getActID());
        List<Turns.Turn> possibleTurns = model.getTurnMaker().getTurns().getFreeTurns();
        Point farestCollisionPoint;

        if (!possibleTurns.isEmpty()) {
            if (possibleTurns.size() == 1) {
                farestCollisionPoint = possibleTurns.get(0).getPoint();
                correctLineIndex(farestCollisionPoint);
            } else {
                //computer has more than clean turn
                if (sprint) {
                    farestCollisionPoint = chooseTheFastest(possibleTurns);
                } else {
                    farestCollisionPoint = chooseTheBestMove(possibleTurns);
                }
            }
        } else {
            //computer does not have other possibility then to crash
            farestCollisionPoint = Calc.findNearestPoint(comp.getLast(), model.getTurnMaker().getTurns().getCollisionPoints());
        }
        return farestCollisionPoint;
    }

    private Point chooseTheFastest(List<Turns.Turn> possibleTurns) {
        int maxSpeed = 0;
        int speed;
        double finishDistance;
        double minFinishDistance = Double.MAX_VALUE;
        Point best = possibleTurns.get(0).getPoint();

        for (int i = 0; i < possibleTurns.size(); i++) {
            Point actPoint = possibleTurns.get(i).getPoint();
            finishDistance = Calc.distance(model.getBuilder().getFinish().getMidPoint(), actPoint);
            speed = comp.maxSpeed(actPoint);

            if (speed > maxSpeed) {
                maxSpeed = speed;
                minFinishDistance = finishDistance;
                best = actPoint;
            } else if (speed == maxSpeed && finishDistance < minFinishDistance) {
                maxSpeed = speed;
                minFinishDistance = finishDistance;
                best = actPoint;
            }
        }
        return best;
    }

    private Point chooseTheBestMove(List<Turns.Turn> possibleTurns) {
        int crossedCount;
        double brakingDistance;
        double collisionDistance;
        double maxCount = 0;
        Point collision;
        Point best = null;

        for (int i = 0; i < possibleTurns.size(); i++) {

            Point actPoint = possibleTurns.get(i).getPoint();
            crossedCount = calculateCrossedCheckLines(actPoint);
            collision = calculateTrackCollision(actPoint);

            if (collision != null) {
                possibleTurns.get(i).setCollision(collision);
                collisionDistance = Calc.distance(collision, actPoint);
            } else {
                collisionDistance = 0;
            }

            brakingDistance = calculateBreakingDistance(actPoint);

            if (collisionDistance > brakingDistance || collisionDistance == 0) {
                if (crossedCount > maxCount) {
                    maxCount = crossedCount;
                    best = actPoint;
                } else if (crossedCount == maxCount) {
                    if (best == null) {
                        best = actPoint;
                    }
                    if (comp.maxSpeed(actPoint) > comp.maxSpeed(best)) {
                        maxCount = crossedCount;
                        best = actPoint;
                    } else if (comp.maxSpeed(actPoint) == comp.maxSpeed(best)) {
                        if (Calc.distance(actPoint, model.getAnalyzer().getCheckLines().get(getCheckLinesIndex() + crossedCount + 1).getMidPoint())
                                < Calc.distance(best, model.getAnalyzer().getCheckLines().get(getCheckLinesIndex() + crossedCount + 1).getMidPoint())) {
                            maxCount = crossedCount;
                            best = actPoint;
                        }
                    }
                }
            }
        }

        if (best == null) {
            best = Calc.findNearestTurn(comp.getLast(), possibleTurns);
        }
        correctLineIndex(best);
        return best;
    }

    private int calculateCrossedCheckLines(Point actPoint) {
        int count = 0;
        List<Segment> checkLines = model.getAnalyzer().getCheckLines();
        for (int i = getCheckLinesIndex() + 1; i < checkLines.size(); i++) {
            if (Calc.halfLineAndSegmentIntersection(checkLines.get(i), comp.getLast(), actPoint) != null) {
                count++;
                if (count == checkLines.size() - 1) {
                    sprint = true;
                }
            } else {
                break;
            }
        }

        return count;
    }

    private Point calculateTrackCollision(Point actPoint) {
        List<Segment> checkLines = model.getAnalyzer().getCheckLines();
        Point collision = Calc.halfLineAndSegmentIntersection(checkLines.get(getCheckLinesIndex()), comp.getLast(), actPoint);
        if (collision == null) {

            for (int i = getCheckLinesIndex(); i < checkLines.size() - 1; i++) {
                collision = Calc.halfLineAndSegmentIntersection(new Segment(checkLines.get(i).getFirst(), checkLines.get(i + 1).getFirst()), comp.getLast(), actPoint);

                if (collision == null) {
                    collision = Calc.halfLineAndSegmentIntersection(new Segment(checkLines.get(i).getLast(), checkLines.get(i + 1).getLast()), comp.getLast(), actPoint);
                }
                if (collision != null) {
                    break;
                }
            }
        } else {
            collision = new Point(actPoint);
        }

        return collision;
    }

    /**
     * It returns braking distance of computer when it will go to given point 'turn'. E.g. when
     * actual speed is 5, then braking distance is 4+3+2 = 9.
     *
     * @param actPoint is point where computer wants to go
     * @return approximation of braking distance
     */
    private double calculateBreakingDistance(Point actPoint) {
        double dist = 0;
        int speed = comp.maxSpeed(actPoint) - 1;
        int side = comp.minSpeed(actPoint) - 1;
        while (speed > 0) {
            dist = dist + Calc.calculateHypotenuse(side, speed);
            speed--;
            if (side == 0) {
                side++;
            } else {
                side--;
            }
        }
        return dist;
    }

    private void correctLineIndex(Point best) {
        List<Segment> checkLines = model.getAnalyzer().getCheckLines();
        int index = getCheckLinesIndex();

        for (int i = index + 1; i < checkLines.size(); i++) {
            if ((int) Calc.crossing(comp.getLast(), best, checkLines.get(i))[0] != Calc.OUTSIDE) {
                increaseIndex();
            } else {
                break;
            }
        }

    }

}
