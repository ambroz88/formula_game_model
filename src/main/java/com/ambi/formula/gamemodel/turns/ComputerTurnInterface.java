package com.ambi.formula.gamemodel.turns;

import com.ambi.formula.gamemodel.datamodel.Point;

/**
 *
 * @author Jiri Ambroz
 */
public interface ComputerTurnInterface {

    Point selectComputerTurn(int computerFormulaPosition);

    void startAgain();
}
