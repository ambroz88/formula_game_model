package com.ambi.formula.gamemodel.enums;

import com.ambi.formula.gamemodel.labels.PrepareGameLabels;

/**
 *
 * @author Jiri Ambroz
 */
public enum FormulaType {

    Player(PrepareGameLabels.PLAYER), ComputerEasy(PrepareGameLabels.COMPUTER_EASY), ComputerMedium(PrepareGameLabels.COMPUTER_MEDIUM);

    private final String type;

    FormulaType(String formulaType) {
        type = formulaType;
    }

    public static FormulaType getType(int formuleType) {
        FormulaType formula;
        if (formuleType == 0) {
            formula = Player;
        } else if (formuleType == 1) {
            formula = ComputerEasy;
        } else if (formuleType == 2) {
            formula = ComputerMedium;
        } else {
            formula = null;
        }
        return formula;
    }

    @Override
    public String toString() {
        return type;
    }

}
