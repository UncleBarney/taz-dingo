/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.neu.coe.platform.device.platform.device;


import com.tazdingo.core.Request;
import com.tazdingo.core.Response;
import com.tazdingo.core.util.ConstantUtil;
import com.tazdingo.core.util.Encryption;
import com.tazdingo.requestAPI.KDCAuthenticationAPI;
import com.tazdingo.requestAPI.ServiceRequestAPI;
import com.tazdingo.requestAPI.TGSAuthorizationAPI;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Cynthia
 */
public class DeviceImpl implements IDevice{
    
    private static DeviceConstructer constructer;
    private String devicesessionid;
    private String usersessionid;
    private String deviceticket;
    private String userticket;
    private String serviceticket;
    private String username;
    private String userstepid;
    private String devicestepid;
    private boolean isblocked;
    private static File data;
    private String defaultplatformurl;
    private String encryptedusername=null;
    private SecretKey localkey=null;
    
/**
 * 
 * @param devicename
 * @param devicepassword
 * @param adminpassword
 * @param defaultplatformurl 
 */
    public DeviceImpl(String devicename,String devicepassword,String adminpassword,String defaultplatformurl){
        SecretKey key=Encryption.generateSecretKey(adminpassword);
        String encryptedpassword=Encryption.encrypt(key, devicepassword);
        constructer=new DeviceConstructer(devicename, encryptedpassword);
        isblocked=false;
        this.defaultplatformurl=defaultplatformurl;
        data=new File("data.xml");
        try {
            data.createNewFile();
            String content="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>\n</root>";
            FileWriter fw = new FileWriter(data.getAbsoluteFile());
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(content);
	    bw.close();
        } catch (IOException ex) {
            Logger.getLogger(DeviceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String readFile(String key){
        String value=null;
         try {
             DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
             Document doc = dBuilder.parse(data);
             doc.getDocumentElement().normalize();
             NodeList nList=doc.getElementsByTagName(key);
             if(nList.getLength()>0){
                 Node node=(Node) nList.item(0);
                 value=node.getTextContent();
                 
             }
         } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(DeviceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return value;
        }
         return value;
    }
    private void readFile(){
       devicesessionid=readFile(ConstantUtil.DEVICESESSIONID);
       deviceticket=readFile(ConstantUtil.DEVICE_TICKET);
       devicestepid=readFile(ConstantUtil.DEVICESTEPID);
       username=readFile(ConstantUtil.USERNAME);
       encryptedusername=readFile(ConstantUtil.USERNAME);
               
        
            
    }
    
   
    public DeviceImpl(String devicename,String password,String defaultplatformurl){
        constructer=new DeviceConstructer(devicename, password);
        this.defaultplatformurl=defaultplatformurl;
        data=new File("data.xml");
        readFile();
    }
    
    private void saveToFile(String key,String value){
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(data);
            doc.getDocumentElement().normalize();
            NodeList nList=doc.getElementsByTagName(key);
            if(nList.getLength()>0)
                nList.item(0).setTextContent(value);
            else{
                Element e=doc.createElement(key);
                e.appendChild(doc.createTextNode(value));
                NodeList roots=doc.getElementsByTagName("root");
                roots.item(0).appendChild(e);
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
                         Transformer transformer = transformerFactory.newTransformer();
                         DOMSource source = new DOMSource(doc);
                         StreamResult result = new StreamResult(data);
                         transformer.transform(source, result);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(DeviceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(DeviceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(DeviceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    
    //remove data
    private void deleteData(){
        data.delete();
        isblocked=true;
        saveToFile(ConstantUtil.BLOCK, "true");
        devicesessionid=null;
        usersessionid=null;
        deviceticket=null;
        userticket=null;
        serviceticket=null;
        username=null;
        userstepid=null;
        devicestepid=null;
        encryptedusername=null;
        
    }
    /**
     * Method to login device through platform
     * @param adminpassword
     * @return 
     */
    @Override
    public String deviceLogin(String adminpassword){
        if(!isblocked){
        String status=ConstantUtil.DEFAULT_ERROR;
        Request request=KDCAuthenticationAPI.createDeviceAuthenticationRequest(constructer.getDeviceId());
        System.out.println();
        System.out.println("Device send device_id to Platform KDC for authentication");
        Response response=sendRequest(request);
        System.out.println();
        System.out.println("Device get response from Platform");
        SecretKey pkey=Encryption.generateSecretKey(adminpassword);
        String decryptedpassoword=Encryption.decrypt(pkey, constructer.getDevicePassword());
         if(decryptedpassoword.equals(ConstantUtil.WRONDKEY)){
            status=ConstantUtil.WRONGPASSWORD;
        }else{
        SecretKey key=Encryption.generateSecretKey(decryptedpassoword);
        localkey=key;
        Map<String,String> data=response.getData();
        String error=data.get(ConstantUtil.ERROR);
        if(error==null) status=ConstantUtil.NO_RESPONSE;
        else
        if(!error.isEmpty()){
        if(error.equals(ConstantUtil.NO_ERROR)){
        data.remove(ConstantUtil.ERROR);
        System.out.println("Decrpted data with key:"+pkey);
        data=Encryption.decrypt(pkey,data);
        if(!data.containsKey(ConstantUtil.ERROR)){
        devicesessionid=data.get(ConstantUtil.SESSIONID);
        deviceticket=data.get(ConstantUtil.TICKET);
        devicestepid=data.get(ConstantUtil.STEPID);
        status=ConstantUtil.SUCCESS_LOGIN;
            saveToFile(ConstantUtil.DEVICESESSIONID, devicesessionid);
            saveToFile(ConstantUtil.DEVICE_TICKET, deviceticket);
            saveToFile(ConstantUtil.DEVICESTEPID, devicestepid);
        }else status=ConstantUtil.INVALIDPLATFORM;
        }else status=error;
        }
        if(error!=null)
        if(error.equals(ConstantUtil.DEVICE_BLOCKED)){
            deleteData();
        }}
        System.out.println(status);
        return status;
        }
        else return ConstantUtil.DEVICE_BLOCKED;
    }
    /**
     * Method to login user when off line
     * @param username
     * @param password
     * @return 
     */
    @Override
    public String userLoginOffLine(String username,String password){
         if(username==null || username.isEmpty() || password==null ||password.isEmpty() && isblocked)
            return ConstantUtil.WRONGPASSWORD;
         String status=ConstantUtil.WRONGPASSWORD;
         SecretKey key=Encryption.generateSecretKey(password);
         String storedusername=Encryption.decrypt(key, encryptedusername);
         if(storedusername.equals(username))
         status=ConstantUtil.SUCCESS_LOGIN;
         return status;
         
    }
    /**
     * Method to login user through platform
     * @param username
     * @param password
     * @return 
     */
    
    @Override
    public String userLogin(String username, String password){
        if(username==null || username.isEmpty() || password==null ||password.isEmpty())
            return null;
        if(!isblocked){
        String status=ConstantUtil.DEFAULT_ERROR;
        Request request=KDCAuthenticationAPI.createUserAuthenticationRequest(constructer.getDeviceId(), username,deviceticket);
        System.out.println();
        System.out.println("User send username,deviceid ,deviceticket to KDC for authentication");
        System.out.println(request.getData());
        Response response=sendRequest(request);
        System.out.println();
        System.out.println("User get Response from platform");
        SecretKey key=Encryption.generateSecretKey(password);
        Map<String,String> data=response.getData();
        String error=data.get(ConstantUtil.ERROR);
        if(error==null) status=ConstantUtil.NO_RESPONSE;
        else
        if(!error.isEmpty())
        if(error.equals(ConstantUtil.NO_ERROR)){
        data.remove(ConstantUtil.ERROR);
        System.out.println("decrpted data with key:"+key);
        data=Encryption.decrypt(key, data);
        if(data.containsKey(ConstantUtil.ERROR)){
            status=ConstantUtil.WRONGPASSWORD;
        }else{
        this.username=username;
        usersessionid=data.get(ConstantUtil.SESSIONID);
        userticket=data.get(ConstantUtil.TICKET);
        userstepid=data.get(ConstantUtil.STEPID);
        status=ConstantUtil.SUCCESS_LOGIN;
        encryptedusername=Encryption.encrypt(key, username);
            saveToFile(ConstantUtil.ENCRPTEDUSERNAME, encryptedusername);
            saveToFile(ConstantUtil.USERNAME, username);
            
        }
        }else if(error.equals(ConstantUtil.DEVICE_BLOCKED)){
            deleteData();
        }else status=error;
         
        System.out.println(status);
        return status;
        }
        else return ConstantUtil.DEVICE_BLOCKED;
    }
    /**
     * Method to send request to service with additional data/parameters 
     * @param servicename
     * @param additionalInformation
     * @return 
     */
    @Override
    public Map<String,String> sendGeneralServiceRequest(String servicename,Map<String,String> additionalInformation){
        if(!isblocked){
        String status=ConstantUtil.DEFAULT_ERROR;
        SecretKey key=Encryption.stringToKey(userstepid);
        if(key!=null){
        System.out.println();
        Request request=ServiceRequestAPI.connectServiceRequest(username, constructer.getDeviceId(),servicename,additionalInformation);
        //System.out.println(request.getData());
        request = encryptRequest(request, key,serviceticket,ConstantUtil.SERVICEICKET);
        System.out.println("User send serviceticket, username,deviceid to Platform for connecting to service");
        Response response=sendRequest(request);
        System.out.println();
        System.out.println("User get response from platform");
        Map<String,String> data=response.getData();
        String error=data.get(ConstantUtil.ERROR);
        if(error==null) status=ConstantUtil.NO_RESPONSE;
        else
        if(!error.isEmpty())
        if(error.equals(ConstantUtil.NO_ERROR)){
        data.remove(ConstantUtil.ERROR);
        System.out.println("decrpted data with key:"+key);
        data=Encryption.decrypt(key, data);
        if(data.containsKey(ConstantUtil.ERROR)){
            status=ConstantUtil.INVALIDSTEPID;
        }else{
        userstepid=data.get(ConstantUtil.STEPID);
        status=data.get(ConstantUtil.MESSAGE);
        serviceticket=data.get(ConstantUtil.TICKET);
        data.remove(ConstantUtil.STEPID);
        data.remove(ConstantUtil.TICKET);
//            saveToFile(ConstantUtil.USERSSTEPID, userstepid);
//            saveToFile(ConstantUtil.SERVICEICKET, serviceticket);
        System.out.println("recevied message from service:"+status);
        } 
        }
        else status=error;
         if(error!=null)
        if(error.equals(ConstantUtil.DEVICE_BLOCKED)){
            deleteData();
        }
        data.put(ConstantUtil.ERROR, status);
        System.out.println(status);
        return data;
        }else return null;
    }else return  null;
    }
    /**
     * Method to send service default request with return message "Welcome" when success
     * @param servicename
     * @return 
     */
    @Override
    public String connectToService(String servicename){
        if(!isblocked){
        String status=ConstantUtil.DEFAULT_ERROR;
        SecretKey key=Encryption.stringToKey(userstepid);
        if(key!=null){
        System.out.println();
        Request request=ServiceRequestAPI.connectServiceRequest(username, constructer.getDeviceId(),servicename);
        //System.out.println(request.getData());
        request = encryptRequest(request, key,serviceticket,ConstantUtil.SERVICEICKET);
        System.out.println("User send serviceticket, username,deviceid to Platform for connecting to service");
        Response response=sendRequest(request);
        System.out.println();
        System.out.println("User get response from platform");
        Map<String,String> data=response.getData();
        String error=data.get(ConstantUtil.ERROR);
        if(error==null) status=ConstantUtil.NO_RESPONSE;
        else
        if(!error.isEmpty())
        if(error.equals(ConstantUtil.NO_ERROR)){
        data.remove(ConstantUtil.ERROR);
        System.out.println("decrpted data with key:"+key);
        data=Encryption.decrypt(key, data);
        if(data.containsKey(ConstantUtil.ERROR)){
            return ConstantUtil.INVALIDSTEPID;
        }else{
        userstepid=data.get(ConstantUtil.STEPID);
        status=data.get(ConstantUtil.MESSAGE);
        serviceticket=data.get(ConstantUtil.TICKET);
        System.out.println("recevied message from service:"+status);
//            saveToFile(ConstantUtil.USERSSTEPID, userstepid);
//            saveToFile(ConstantUtil.SERVICEICKET, serviceticket);
        }
        }else status=error;
         if(error!=null)
        if(error.equals(ConstantUtil.DEVICE_BLOCKED)){
            deleteData();
        }
        System.out.println(status);
        return status;
        }else return ConstantUtil.INVALIDSTEPID;
    }else return  ConstantUtil.DEVICE_BLOCKED;
    }
    /**
     * Method to authorize user through platform
     * @return 
     */
    
    @Override
    public String userAuthorization(){
        if(!isblocked){
        String status=ConstantUtil.DEFAULT_ERROR;
        SecretKey key=Encryption.stringToKey(userstepid);
        if(key!=null){
        Request request=TGSAuthorizationAPI.createUserAuthorizationRequest(username, constructer.getDeviceId());
        System.out.println();
        request = encryptRequest(request, key,userticket,ConstantUtil.TGT);
        System.out.println("User send userticket,username,deviceid to TGS for authorization");
        Response response=sendRequest(request);
        System.out.println();
        System.out.println("User received response from Platform");
        Map<String,String> data=response.getData();
        String error=data.get(ConstantUtil.ERROR);
        if(error==null) status=ConstantUtil.NO_RESPONSE;
        else
        if(!error.isEmpty())
        if(error.equals(ConstantUtil.NO_ERROR)){
        data.remove(ConstantUtil.ERROR);
        System.out.println("decrpted data with key:"+key);
        data=Encryption.decrypt(key, data);
        if(data.containsKey(ConstantUtil.ERROR)){
            return ConstantUtil.INVALIDSTEPID;
        }else{
        userstepid=data.get(ConstantUtil.STEPID);
        serviceticket=data.get(ConstantUtil.TICKET);
        status=ConstantUtil.SUCCESS_GET_SERVICE_TICKET;
//        saveToFile(ConstantUtil.USERSSTEPID, userstepid);
//        saveToFile(ConstantUtil.SERVICEICKET, serviceticket);
        }}else status=error;
         if(error!=null)
        if(error.equals(ConstantUtil.DEVICE_BLOCKED)){
            deleteData();
            return ConstantUtil.DEVICE_BLOCKED;
        }
         System.out.println(status);
        return status;
        }else return ConstantUtil.INVALIDSTEPID;
        }else return ConstantUtil.DEVICE_BLOCKED;
    }
    /**
     * Method to authorize device through platform 
     * @return 
     */
    
    @Override
    public String deviceAuthorization(){
        if(!isblocked){
        String status=ConstantUtil.DEFAULT_ERROR;
        SecretKey key=Encryption.stringToKey(devicestepid);
        if(key!=null){
        Request request=TGSAuthorizationAPI.createDeviceAuthorizationRequest(constructer.getDeviceId());
        System.out.println();
        request = encryptRequest(request, key,deviceticket,ConstantUtil.TGT);
        System.out.println("Device send deviceticket,deviceid to TGS for authorization");
        Response response=sendRequest(request);
        System.out.println();
        System.out.println("Device received response from platform");
        Map<String,String> data=response.getData();
        String error=data.get(ConstantUtil.ERROR);
        if(error==null) status=ConstantUtil.NO_RESPONSE;
        else
        if(!error.isEmpty())
        if(error.equals(ConstantUtil.NO_ERROR)){
        data.remove(ConstantUtil.ERROR);
        System.out.println("decrpted data with key:"+key);
        data=Encryption.decrypt(key, data);
        if(data.containsKey(ConstantUtil.ERROR)){
            return ConstantUtil.INVALIDSTEPID;
        }else{
        devicestepid=data.get(ConstantUtil.STEPID);
        deviceticket=data.get(ConstantUtil.TICKET);
        status=ConstantUtil.SUCCESS_GET_TICKET;
            saveToFile(ConstantUtil.DEVICESTEPID, devicestepid);
            saveToFile(ConstantUtil.DEVICE_TICKET,deviceticket);
        }}else status=error;
        if(error!=null)
        if(error.equals(ConstantUtil.DEVICE_BLOCKED)){
            deleteData();
            return ConstantUtil.DEVICE_BLOCKED;
        }
         System.out.println(status);
        return status;
        }else return ConstantUtil.INVALIDSTEPID;
        } else return  ConstantUtil.DEVICE_BLOCKED;
    }
      
    private Request encryptRequest(Request request,SecretKey key,String ticket,String type){
        Map<String,String> data=request.getData();
        data=Encryption.encrypt(key, data);
        data.put(ConstantUtil.TICKET, ticket);
        data.put(ConstantUtil.TICKET_TYPE, type);
        request.setData(data);
        return request;
    }
    /**
     * Method to send request to platform
     * @param request
     * @return 
     */
        
     
     protected Response sendRequest(Request request){
         //Config.Config();
         HttpClient client = HttpClientBuilder.create().build();
	 HttpPost post = new HttpPost(defaultplatformurl);
         Response response=new Response();
        try {
            post.setEntity(new UrlEncodedFormEntity(request.getParameterPair())); 
            HttpResponse httpresponse = client.execute(post);
            response=new Response(httpresponse);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DeviceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return new Response();
        } catch (IOException ex) {
            Logger.getLogger(DeviceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return new Response();
        }
        
         return response;
     }

     /**
      * Method to get device sessionid 
      * @return 
      */
    @Override
    public String getDevicesessionid() {
        return devicesessionid;
    }
    /**
     * Method to get user sessionid
     * @return 
     */

    @Override
    public String getUsersessionid() {
        return usersessionid;
    }
    /**
     * method to get device ticket
     * @return 
     */

    @Override
    public String getDeviceticket() {
        return deviceticket;
    }
    /**
     * Method to get user ticket
     * @return 
     */

    @Override
    public String getUserticket() {
        return userticket;
    }
    /**
     * Method to get service ticket
     * @return 
     */

    @Override
    public String getServiceticket() {
        return serviceticket;
    }
    /**
     * Method to get device stepid
     * @return 
     */

    @Override
    public String getDevicestepid() {
        return devicestepid;
    }
    /**
     * Method to get userstepid
     * @return 
     */

    @Override
    public String getUserstepid() {
        return userstepid;
    }
    /**
     * Method to return deviceid
     * @return 
     */

    @Override
    public String toString(){
       return constructer.getDeviceId();
        
    }
    /**
     * Method to set default platformurl
     * @param defaultplatformurl 
     */

    public void setDefaultplatformurl(String defaultplatformurl) {
        this.defaultplatformurl = defaultplatformurl;
    }
   /**
    * Method to get local key with adminpassword
    * @param adminpassword
    * @return 
    */
    @Override
    public SecretKey getLocalKey(String adminpassword){
        SecretKey pkey=Encryption.generateSecretKey(adminpassword);
        String decryptedpassoword=Encryption.decrypt(pkey, constructer.getDevicePassword());
         if(decryptedpassoword.equals(ConstantUtil.WRONDKEY)){
            return null;
        }else{
             localkey=Encryption.generateSecretKey(decryptedpassoword);
        return localkey;
         }
    }

    /**
     * Method to get local key
     * @return 
     */
    @Override
    public SecretKey getLocalkey() {
        if(localkey==null){
            System.out.println("Enter adminpassword:");
            Scanner scan=new Scanner(System.in);
            String adminpassword=scan.next();
            return getLocalKey(adminpassword);
        }
        return localkey;
    }
    
         
    
    
}
