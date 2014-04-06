/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.neu.coe.platform.service.serviceserver.GeneralServer;

import com.tazdingo.core.Request;
import com.tazdingo.core.Response;
import com.tazdingo.core.ServiceWorkRequest;
import com.tazdingo.core.WorkQueue;
import com.tazdingo.core.WorkRequest;
import com.tazdingo.core.util.ConstantUtil;
import com.tazdingo.core.util.Encryption;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tazdingo.requestAPI.KDCAuthenticationAPI;
import com.tazdingo.requestAPI.TGSAuthorizationAPI;
import com.tazdingo.ticket.ServiceTicket;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;


/**
 *
 * @author Cynthia
 */
public abstract class Server implements IServer{
     protected WorkQueue workqueue=new WorkQueue();
     protected String sessionid;
     protected ServerConstructor constructor;
     protected String stepid;
     protected String kdcticket;
     protected String tgt;
     protected SecretKey serverkey;
  
     protected String defaultplatformurl;
    
     @Override
    public HttpServletResponse takeRequest(HttpServletRequest httpRequest,HttpServletResponse httpResponse){
        Request request=new Request(httpRequest);
        return takeRequest(request).convertToHttpResponse(httpResponse);
    } 
     
    /**
     * Method to pull request and return response
     * @param request
     * @return 
     */
    public Response takeRequest(Request request){
        Response response=new Response();
        //System.out.println(request.getData());
        System.out.println("service recieved request");
       
        String ticket=request.getData().get(ConstantUtil.TICKET);
        if(ticket!=null&&!ticket.isEmpty()){
            SecretKey servicekey=getServiceKey();
            if(servicekey!=null){
            System.out.println("decrypted ticket with key:"+servicekey);
            
         String decrptedTicket=Encryption.decrypt(servicekey, ticket);
         if(!decrptedTicket.equals(ConstantUtil.WRONDKEY)){
         ServiceTicket serviceticket=new ServiceTicket(decrptedTicket);
         request.getData().remove(ConstantUtil.TICKET);
         SecretKey tempKey=Encryption.stringToKey(serviceticket.getStepid());
         System.out.println("decrypted data with key:"+tempKey);
         
         request.setData(Encryption.decrypt(tempKey, request.getData()));
         request.getData().put(ConstantUtil.TICKET, decrptedTicket);
        if(validateTicket(serviceticket, getIdentifier(request.getData()))){
            String operation=request.getData().get(ConstantUtil.OPERATION);
            if(checkprivilege(serviceticket.getPriviledge(), operation)){
        String usersessionid=request.getData().get(ConstantUtil.USERSSIONID);
        if(usersessionid==null)usersessionid=serviceticket.getKdcticket().getSessionID();
        if(usersessionid==null)usersessionid=sessionid;
            WorkRequest workRequest=new ServiceWorkRequest(usersessionid,request);
        ((ServiceWorkRequest)workRequest).setTempkey(Encryption.keyToString(tempKey));
      
        workqueue.addWorkRequest(usersessionid, workRequest);
        return excuteRequest(usersessionid);
            }else response.getData().put(ConstantUtil.ERROR, ConstantUtil.LACKOFPRIVILEGE);
        }else response.getData().put(ConstantUtil.ERROR, ConstantUtil.INVALID_TGS_TICKET);
         }else response.getData().put(ConstantUtil.ERROR, ConstantUtil.INVALID_TGS_TICKET);
        }else response.getData().put(ConstantUtil.ERROR, ConstantUtil.INVALIDSERVICE);
        }
        else return handleNOTicketRequest(request);
        
        return response;
    }
    /**
     * Method to get account identifier e.g. username and deviceid combination ,platformname
     * @param data
     * @return 
     */
    protected abstract String getIdentifier(Map<String,String>data);
    /**
     * Method to handle with request without ticket
     * @param reqeust
     * @return 
     */
    protected abstract Response handleNOTicketRequest(Request reqeust);
    /**
     * Method to execute request
     * @param usersessionid
     * @return 
     */
    protected abstract Response excuteRequest(String usersessionid);

    public Server(String servername,String serverpassword,String adminpassword,String defaultplatformurl) {
        SecretKey key=Encryption.generateSecretKey(adminpassword);
        String encryptedpassword=Encryption.encrypt(key, serverpassword);
        serverkey=Encryption.generateSecretKey(serverpassword);
        this.defaultplatformurl=defaultplatformurl;
        constructor=new ServerConstructor(servername, encryptedpassword);
    }
    /**
     * Method to login service to platform
     * @param type
     * @return 
     */
    public String serverLogin(String type){
        String status=ConstantUtil.DEFAULT_ERROR;
        Request request=KDCAuthenticationAPI.createServerAuthenticationRequest(constructor.getServerid(),type);
        System.out.println();
        System.out.println("Service send servername to PlatformKDC for authentication");
        Response response=sendRequest(request);
        System.out.println();
        System.out.println("Service received response from platform");
        SecretKey key=getServicePassword();
        //System.out.println(key);
        if(key!=null){
        Map<String,String> data=response.getData();
        String error=data.get(ConstantUtil.ERROR);
        if(error==null) status=ConstantUtil.NO_RESPONSE;
        else
        if(!error.isEmpty())
        if(error.equals(ConstantUtil.NO_ERROR)){
        data.remove(ConstantUtil.ERROR);
        System.out.println("decrypted data with key:"+key);
        data=Encryption.decrypt(key, data);
        if(data.containsKey(ConstantUtil.ERROR)){
            status=ConstantUtil.INVALIDPLATFORM;
        }else{
        sessionid=data.get(ConstantUtil.SESSIONID);
        kdcticket=data.get(ConstantUtil.TICKET);
        stepid=data.get(ConstantUtil.STEPID);
        status=ConstantUtil.SUCCESS_LOGIN;
        }
        }else status=error;
        }else status=ConstantUtil.WRONGPASSWORD;
        System.out.println(status);
        return status;
    }
    /**
     * Method to get the service password i.e.key
     * @return 
     */
    protected abstract SecretKey getServicePassword();
    /**
     * Method to authorize service to platform
     * @return 
     */
     @Override
    public String serverAuthorization(){
        String status=ConstantUtil.DEFAULT_ERROR;
        SecretKey key=Encryption.stringToKey(stepid);
        if(key!=null){
        Request request=TGSAuthorizationAPI.createServerAuthorizationRequest(constructor.getServerid());
        System.out.println();
        request = encryptRequest(request, key,kdcticket,ConstantUtil.TGT);
        System.out.println("Service send tgt,servicename to TGS for authorization");
       
        Response response=sendRequest(request);
        System.out.println();
        System.out.println("Service get response from platform");
        Map<String,String> data=response.getData();
        String error=data.get(ConstantUtil.ERROR);
        if(error==null) status=ConstantUtil.NO_RESPONSE;
        else
        if(!error.isEmpty())
        if(error.equals(ConstantUtil.NO_ERROR)){
            System.out.println("decrypted data with key"+key);
            data.remove(ConstantUtil.ERROR);
        data=Encryption.decrypt(key, data); 
        if(data.containsKey(ConstantUtil.ERROR)){
            status=ConstantUtil.INVALIDSTEPID;
        }else{
        stepid=data.get(ConstantUtil.STEPID);
        tgt=data.get(ConstantUtil.TICKET);
        status=ConstantUtil.SUCCESS_GET_SERVICE_TICKET;
        System.out.println("Authorizate Successfully");
        }
        }else status=error;
        }else status=ConstantUtil.INVALIDSTEPID;
        return status;
    }
    /**
     * Method to encrypted request
     * @param request
     * @param key
     * @param ticket
     * @param type
     * @return 
     */
     protected Request encryptRequest(Request request,SecretKey key,String ticket,String type){
        Map<String,String> data=request.getData();
        System.out.println("encrypted data with key:"+key);
        data=Encryption.encrypt(key, data);
        data.put(ConstantUtil.TICKET, ticket);
        data.put(ConstantUtil.TICKET_TYPE, type);
        request.setData(data);
        return request;
    }
    
     private Response sendRequest(Request request){
         //Config.Config();
         HttpClient client = HttpClientBuilder.create().build();
	 HttpPost post = new HttpPost(defaultplatformurl);
         Response response=new Response();
        try {
            post.setEntity(new UrlEncodedFormEntity(request.getParameterPair())); 
            HttpResponse httpresponse = client.execute(post);
            response=new Response(httpresponse);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            return new Response();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            return new Response();
        }
        
         return response;
     }
     
     private boolean validateTicket(ServiceTicket serviceticket,String identifier){
         
         if((System.currentTimeMillis()-serviceticket.getExpiredtime())<0)
         if(identifier.equals(serviceticket.getKdcticket().getAuthenticator()))
             return true;
         return false;
         
     }
     /**
      * Method to check the privilege
      * @param privilege
      * @param operation
      * @return 
      */
     
     protected boolean checkprivilege(String privilege,String operation){
         return true;
     };
     /**
      * Method to get SserviceKey
      * @return 
      */
     protected abstract SecretKey getServiceKey();
     
     /**
      * Method to get new ticket
      * @return 
      */
     protected synchronized String getNewTicket(){
        if(tgt==null||tgt.isEmpty())
        {
        String response=serverLogin();
        if(response.equals(ConstantUtil.SUCCESS_LOGIN))
            response=serverAuthorization();
        if(!response.equals(ConstantUtil.SUCCESS_GET_SERVICE_TICKET))
            tgt=null;
        }
        return tgt;
     }
    
}
