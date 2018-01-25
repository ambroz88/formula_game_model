package com.ambi.formula.gamemodel.turns;

import com.ambi.formula.gamemodel.datamodel.Point;

/**
 *
 * @author Jiri Ambroz
 */
public abstract class ComputerTurnCore {

    private int checkLinesIndex;

    public ComputerTurnCore() {
        checkLinesIndex = 0;
    }

    public abstract Point selectComputerTurn();

    public abstract void reset();

    public void startAgain() {
        this.checkLinesIndex = 0;

    }

    public int getCheckLinesIndex() {
        return checkLinesIndex;
    }

    public void setCheckLinesIndex(int checkLinesIndex) {
        this.checkLinesIndex = checkLinesIndex;
    }

    public void increaseIndex() {
        checkLinesIndex++;
    }

}
