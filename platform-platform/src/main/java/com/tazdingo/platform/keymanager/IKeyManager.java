package com.tazdingo.platform.keymanager;

import java.util.Map;
import javax.crypto.SecretKey;

/**
 *
 * @author congliu
 */
public interface IKeyManager {

    public String platformLogin(String keyservername);

    public String platformAuthorization(String keyservername);

    public void addIdentifier(String identifier);

    public void addIdentifier(String identifier, String keyservername);

    public void addkeyserver(String keyservername, String keyserverurl);

    public Map<String, String> getKeyservermap();

    public void setTicket(String ticket, String keyservername);

    public String getTicket(String keyservername);

    public String getPlatformname();

    public SecretKey getKeyServerMasterKey();

    public SecretKey getSessionMasterKey();

    public ConstructKey getConstructkey();

    public void setTicket(String ticket);

    public String getTicket();

    public void setKeyServerMasterKey(SecretKey key);

    public SecretKey getKey(String identifier, String type, String sessionid);
    
}
