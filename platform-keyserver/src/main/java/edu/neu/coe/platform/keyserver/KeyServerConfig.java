/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.neu.coe.platform.keyserver;

/**
 *
 * @author Cynthia
 */
public class KeyServerConfig extends AbstractKeyServerConfig{

    public KeyServerConfig() {
    }
    
    

    @Override
    protected KeyServer newKeyServer(String keyservername, String password, String adminpassword, String platformurl, String keystorefilespath, String defaultplatformname) {
        return new KeyServer(keyservername, password, adminpassword, platformurl, keystorefilespath, defaultplatformname);
    }
    
}
