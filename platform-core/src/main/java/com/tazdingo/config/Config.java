package com.tazdingo.config;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * @author Hechen Gao
 */
public class Config {

    /**
     * Get the instance of org.apache.commons.configuration.XMLConfiguration
     * in order the get the attributes in the configuration file.
     * 
     * @param path, the path to the configuration file
     * @return 
     */
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
