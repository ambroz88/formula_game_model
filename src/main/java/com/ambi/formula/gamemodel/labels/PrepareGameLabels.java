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
public class PrepareGameLabels {

    public final static String TITLE = "title";
    public final static String PLAYER = "player";
    public final static String COMPUTER_EASY = "computerEasy";
    public final static String COMPUTER_MEDIUM = "computerMedium";
    public final static String START_GAME = "startGame";

    private Properties properties;

    public PrepareGameLabels(String language) {
        String fileName = language + "/PrepareGame.properties";
        try {
            this.properties = FileIO.loadProperties(fileName);
        } catch (IOException ex) {
            try {
                this.properties = FileIO.loadProperties(this.getClass().getClassLoader().getResource(fileName).getFile());
            } catch (IOException ex1) {
                Logger.getLogger(DialogLabels.class.getName()).log(Level.SEVERE, null, ex);
                this.properties = new Properties();
            }
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
