package com.tazdingo.platform.devicemanager;

import java.util.List;
import com.tazdingo.core.Response;

/**
 *
 * @author Cynthia
 */
public interface IDeviceManager {

    public boolean isBlock(String deviceid);

    public String blockDevice(String deviceid, List<String> devicelist);

    public Response generateBlockedDeviceResponse();
    
}
