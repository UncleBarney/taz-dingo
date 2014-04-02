package com.tazdingo.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import com.tazdingo.core.util.ConstantUtil;

import org.apache.http.HttpResponse;

/**
 *
 * @author Cynthia
 */
public class Response {

    private Map<String, String> data;

    public Response() {
        data = new HashMap<>();
    }

    public Response(HttpResponse response) {
        BufferedReader rd = null;
        try {
            data = new HashMap<>();
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            String prekey = "";
            while ((line = rd.readLine()) != null) {
                //System.out.println(line);
                String[] pair = line.split(ConstantUtil.DOUBLEDELIMITER);
                if (pair.length == 2) {
                    data.put(pair[0], pair[1]);
                    prekey = pair[0];
                } else if (pair.length == 1) {
                    String value = data.get(prekey);
                    data.put(prekey, value + pair[0]);
                }
            }
        } catch (IOException | IllegalStateException ex) {
            Logger.getLogger(Response.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                rd.close();
            } catch (IOException ex) {
                Logger.getLogger(Response.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public HttpServletResponse convertToHttpResponse(HttpServletResponse response) {
        Iterator<Entry<String, String>> iterator = data.entrySet().iterator();
        while (iterator.hasNext()) {
            try {
                Map.Entry<String, String> mapEntry = (Map.Entry<String, String>) iterator.next();
                response.addHeader(mapEntry.getKey(), mapEntry.getValue());
                response.getWriter().println(mapEntry.getKey() + ConstantUtil.DOUBLEDELIMITER + mapEntry.getValue());
            } catch (IOException ex) {
                Logger.getLogger(Response.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return response;
    }
}
