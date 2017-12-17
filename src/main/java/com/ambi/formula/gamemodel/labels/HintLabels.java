package com.ambi.formula.gamemodel.labels;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ambi.formula.gamemodel.utils.FileIO;

/**
 *
 * @author Jiri Ambroz
 */
public class HintLabels {

    public final static String EMPTY = "empty";
    public final static String WRONG_START = "wrongStart";
    public final static String IDENTICAL_POINTS = "identicalPoints";
    public final static String THROUGH_START = "throughStart";
    public final static String CROSSING = "crossing";
    public final static String FORWARD = "forward";
    public final static String LEFT_SIDE_FIRST = "leftSideFirst";
    public final static String RIGHT_SIDE_FIRST = "rightSideFirst";
    public final static String MOVE_POINTS = "movePoints";
    public final static String CHOOSE_SIDE = "chooseSide";
    public final static String NO_POINT = "noPoint";
    public final static String OUTSIDE = "outside";
    public final static String TRACK_READY = "trackReady";
    public final static String HINT_SAVED = "saveHint";
    public final static String HINT_FAILED = "saveFailed";
    public final static String START_POSITION = "startPosition";
    public final static String NEXT_COMP_TURN = "nextCompTurn";
    public final static String NEXT_CLOSE_TURN = "nextCloseTurn";
    public final static String WRONG_TRACK = "wrongTrack";
    public final static String OUCH = "ouch";
    public final static String CRASH = "crash";
    public final static String WINNER = "winner";
    public final static String BOTH_WIN = "bothWin";

    private Properties properties;

    public HintLabels(String language) {
        String fileName = FileIO.getResourceFilePath(language + "/Hints.properties");
        try {
            this.properties = FileIO.loadProperties(fileName);
        } catch (IOException ex) {
            Logger.getLogger(DialogLabels.class.getName()).log(Level.SEVERE, null, ex);
            this.properties = new Properties();
        }
    }

    public String getValue(String propertyName) {
        Object label = properties.get(propertyName);
        if (label != null) {
            return label.toString();
        } else {
            return "LABEL " + propertyName + " WAS NOT FOUND.";
        }
    }

}
