package com.tazdingo.config;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * @author Administrator
 */
public class Test {

    public static void main(String[] args) {
        try {
            XMLConfiguration config = new XMLConfiguration("../conf/device.xml");
            System.out.println(config.getString("device.id"));
        } catch (ConfigurationException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
