
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.neu.coe.platform.serviceserver;

import com.tazdingo.core.Request;
import com.tazdingo.core.Response;
import com.tazdingo.core.ServiceWorkRequest;
import com.tazdingo.core.WorkRequest;
import com.tazdingo.core.util.ConstantUtil;
import com.tazdingo.core.util.Encryption;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import com.tazdingo.requestAPI.ServiceRequestAPI;
import edu.neu.coe.platform.service.serviceserver.GeneralServer.Server;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;


/**
 *
 * @author Cynthia
 */
public class ServiceServer extends Server implements IServiceServer{
    
    public ServiceServer(String servicename,String password,String adminpassword,String defaultplatformurl){
        super(servicename, password, adminpassword,defaultplatformurl);
    }

  /**
   * Method to get account identification i.e. username and deviceid combination
   * @param data
   * @return 
   */

    @Override
    protected String getIdentifier(Map<String, String> data) {
       return data.get(ConstantUtil.USERNAME)+ConstantUtil.FROM+data.get(ConstantUtil.DEVICEID);
    }
/**
 * Method to handle request without ticket
 * @param reqeust
 * @return 
 */
    @Override
    protected Response handleNOTicketRequest(Request reqeust) {
        return new Response();
    }
/**
 * Method to execute request and return response
 * @param usersessionid
 * @return 
 */
    @Override
    protected Response excuteRequest(String usersessionid) {
        Response response=new Response();
        WorkRequest request=workqueue.pullWorkRequest(usersessionid);
        response=processRequest(request);
        SecretKey tempKey=Encryption.stringToKey(((ServiceWorkRequest)request).getTempkey());
        
        System.out.println("encrypted data with key:"+tempKey);
        response.setData(Encryption.encrypt(tempKey, response.getData()));
        response.getData().put(ConstantUtil.ERROR, ConstantUtil.NO_ERROR);
        System.out.println("Service send response back to platform");
       return response;
    }
    /**
     * Method to further process the request and return response
     * @param request
     * @return 
     */
    protected Response processRequest(WorkRequest request){
        Response response=new Response();
        response.getData().put(ConstantUtil.MESSAGE, ConstantUtil.DEFAULT_MESSAGE);
        return response;
    }
    /**
     * Method to get service password
     * @return 
     */

    @Override
    protected SecretKey getServicePassword() {
        return serverkey;
    }
/**
 * Method to get service key
 * @return 
 */
    @Override
    protected SecretKey getServiceKey() {
        SecretKey servicekey=null;
        String status=ConstantUtil.DEFAULT_ERROR;
        int i=0;
        while(tgt==null&&i<5){
           getNewTicket();
           i++;
        }
        SecretKey key=Encryption.stringToKey(stepid);
        Request request=ServiceRequestAPI.getServiceKey(constructor.getServerid());
        request = encryptRequest(request, key,tgt,ConstantUtil.SERVICEICKET);
        System.out.println("Send servicename,ticket to Platform for servicekey");
        Response response=sendRequestToPlatform(request);
        Map<String,String> data=response.getData();
        String error=data.get(ConstantUtil.ERROR);
        if(error==null) status=ConstantUtil.NO_RESPONSE;
        else
        if(!error.isEmpty())
        if(error.equals(ConstantUtil.NO_ERROR)){
        data.remove(ConstantUtil.ERROR);
        data=Encryption.decrypt(key, data);
        stepid=data.get(ConstantUtil.STEPID);
        String stringkey=data.get(ConstantUtil.KEY);
        servicekey=Encryption.stringToKey(stringkey);
        System.out.println("service recevied key from platform:"+servicekey);
       
        status=ConstantUtil.NO_ERROR;
        }else status=error;
        System.out.println(status);
        
        return servicekey;
    }
    
    private Response sendRequestToPlatform(Request request){
        HttpClient client = HttpClientBuilder.create().build();
	 HttpPost post = new HttpPost(defaultplatformurl);
         Response response=null;
        try {
            post.setEntity(new UrlEncodedFormEntity(request.getParameterPair())); 
            HttpResponse httpresponse = client.execute(post);
            response=new Response(httpresponse);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ServiceServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServiceServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         return response;
        
    }
/**
 * Method to login service in platform
 * @return 
 */
    @Override
    public String serverLogin() {
       return super.serverLogin(ConstantUtil.SERVICE_SERVER);
    }
    
    
}
