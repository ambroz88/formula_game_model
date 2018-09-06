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
public class StartMenuLabels {

    public final static String START = "start";
    public final static String START_GAME = "newGame";
    public final static String START_AGAIN = "startAgain";
    public final static String QUIT = "quit";

    private Properties properties;

    public StartMenuLabels(String language) {
        String fileName = language + "/StartMenu.properties";
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
