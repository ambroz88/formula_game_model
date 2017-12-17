package com.ambi.formula.gamemodel.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

/**
 *
 * @author Jiri Ambroz
 */
public abstract class FileIO {

    public static Properties loadProperties(String filePath) throws IOException {
        Properties prop = new Properties();
        FileInputStream inStream = new FileInputStream(filePath);
        prop.load(inStream);
        return prop;
    }

    public static String readFileToString(String filePath) throws IOException {
        return FileUtils.readFileToString(FileUtils.getFile(filePath), "UTF-8");
    }

    public static String getResourceFilePath(String path) {
        return FileIO.class.getClassLoader().getResource(path).getFile();
    }

}
