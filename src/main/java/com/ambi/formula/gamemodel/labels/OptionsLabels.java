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
public class OptionsLabels {

    public final static String TITLE = "title";
    public final static String PAPER_TITLE = "paper_title";
    public final static String PAPER_SIZE = "paper_size";
    public final static String PAPER_WIDTH = "paper_width";
    public final static String PAPER_HEIGHT = "papet_height";
    public final static String NOTE = "note";
    public final static String RULE_TITLE = "rule_title";
    public final static String NO_TURNS = "no_turns";
    public final static String END_RULES = "end_rules";
    public final static String RULE_FIRST = "rule_first";
    public final static String RULE_SECOND = "rule_second";
    public final static String RULE_COLISION = "rule_colision";
    public final static String PLAYERS = "players";
    public final static String PLAYER1 = "player1";
    public final static String PLAYER2 = "player2";
    public final static String SHOW_TURNS = "show_turns";

    private Properties properties;

    public OptionsLabels(String language) {
        String fileName = FileIO.getResourceFilePath(language + "/Options.properties");
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
            return "CORRECT LABEL NOT FOUND.";
        }
    }
}
