package com.tazdingo.platform.devicemanager;

import com.tazdingo.core.util.ConstantUtil;
import java.util.ArrayList;
import java.util.List;
import com.tazdingo.core.Response;

/**
 *
 * @author Cynthia
 */
public class DeviceManagerImpl implements IDeviceManager {

    private ArrayList<String> blockedDeviceList;

    public DeviceManagerImpl() {
        blockedDeviceList = new ArrayList<String>();
    }

    @Override
    public boolean isBlock(String deviceid) {
        return blockedDeviceList.contains(deviceid);
    }

    @Override
    public String blockDevice(String deviceid, List<String> devicelist) {
        String message = ConstantUtil.SUCCESS_BLOCK;
        if (devicelist.contains(deviceid)) {
            blockedDeviceList.add(deviceid);
        } else {
            message = ConstantUtil.FAIL_BLOCK;
        }
        return message;
    }

    @Override
    public Response generateBlockedDeviceResponse() {
        Response response = new Response();
        response.getData().put(ConstantUtil.ERROR, ConstantUtil.DEVICE_BLOCKED);
        return response;
    }
    
}
