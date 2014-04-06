package com.tazdingo.config;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * @author Administrator
 */
public class Config {

    public static XMLConfiguration config(String path) {
         XMLConfiguration config=null;
        try {
            config= new XMLConfiguration(path);
        } catch (ConfigurationException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
        return config;
    }
}
