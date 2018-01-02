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
public class StatisticLabels {

    public final static String MOVES = "moves";
    public final static String DISTANCE = "distance";
    public final static String WAIT = "wait";

    private Properties properties;

    public StatisticLabels(String language) {
        try {
            this.properties = FileIO.loadProperties(language + "/StatPanel.properties");
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
