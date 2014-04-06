package com.tazdingo.core.util;

/**
 *
 * @author apple
 */
public class ConstantUtil {

    //Request Type
    public final static String REQUEST_TYPE = "requestType";
    public final static String DEVICE_AUTHENTICATE_REQUEST = "DeviceAuthenticateRequest";
    public final static String DEVICE_SERVICE_AUTHENTICATE_REQUEST = "DeviceServiceAuthenticateRequest";
    public final static String DEVICE_SERVICE_REQUEST = "DeviceServiceRequest";
    public final static String DEVICE_AUTHERIZATION_REQUEST = "DeviceAuthorizationRequest";
    public final static String USER_AUTHENTICATE_REQUEST = "UserAuthenticateRequest";
    public final static String USER_AUTHERIZATION_REQUEST = "UserAuthorizationRequest";
    public final static String USER_CONNECT_SERVICE_REQUEST = "UserConnectServiceRequest";
    public final static String SERVICE_AUTHENTICATE_REQUEST = "ServiceAuthenticateRequest";
    public final static String SERVICE_AUTHERIZATION_REQUEST = "ServiceAuthorizationRequest";
    public final static String KEYSERVER_AUTHENTICATE_REQUEST = "KeyServerAuthenticateRequest";
    public final static String KEYSERVER_AUTHERIZATION_REQUEST = "KeyServerAuthorizationRequest";
    public final static String PLATFORM_AUTHENTICATE_REQUEST = "PlatformAuthenticateRequest";
    public final static String PLATFORM_AUTHORIZATION_REQUEST = "PlatformAuthorizationRequest";
    public final static String PLATFORM_KEY_REQUEST = "PlatformKeyRequest";
    public final static String SERVICE_KEY_REQUEST = "ServiceKeyRequest";
    //request attribute
    public final static String OPERATION = "operation";
    public final static String ACCOUNTNAME = "account_name";
    public final static String ACCOUNTTYPE = "account_type";
    public final static String DELETE = "delete";
    public final static String ADD_OR_UPDATE = "add_or_update";
    public final static String FIND = "find";
    //
    public final static String DEVICEID = "DeviceId";
    public final static String USERNAME = "Username";
    public final static String DEVICESESSIONID = "DeviceSessionID";
    public final static String DEVICESTEPID = "DeviceStepID";
    public final static String USERSSIONID = "UserSessionID";
    public final static String USERSSTEPID = "UserStepID";
    public final static String CREATED_DATE = "CreatedDate";
    public final static String ENCRPTEDUSERNAME="encryptedusername";
    //response
    public final static String NO_ERROR = "None";
    public final static String ERROR = "error";
    public final static String DEFAULT_ERROR = "unexpected error";
    public final static String SUCCESS_LOGIN = "Login sucessefully";
    public final static String SUCCESS_GET_SERVICE_TICKET = "Get service ticket sucessefully";
    public final static String SUCCESS_GET_TICKET = "Get ticket sucessefully";
    public final static String MESSAGE = "message";
    public final static String NO_RESPONSE = "no response";
    public final static String KEY = "key";
    public final static String DEFAULT_MESSAGE = "Welcome";
    public final static String SUCCESS_BLOCK = "device is blocked successfully";
    public final static String FAIL_BLOCK = "deviceid does not exist";
    //Time
    public final static long SECOND = (long) 1000;
    public final static long MINUTE = 60 * SECOND;
    public final static long HOUR = 60 * MINUTE;
    public final static long DAY = 24 * HOUR;
    public final static long MONTH = 30 * DAY;
    //session
    public final static long DEVICE_SESSION_EXTEND = 7 * DAY;
    public final static long USER_SESSION_EXTEND = 30 * MINUTE;
    public final static long SERVICE_SESSION_EXTEND = 1 * HOUR;
    public final static String SESSIONID = "session_id";
    public final static String STEPID = "step_id";
    //Ticket
    public final static String TICKET = "ticket";
    public final static long DEVICE_TICKET_ACTIVE_TIME = 7 * DAY;
    public final static long USER_TICKET_ACTIVE_TIME = 5 * MINUTE;
    public final static long SERVICE_TICKET_ACTIVE_TIME = 1 * MONTH;
    public final static long PLATFORM_TICKET_ACTIVE_TIME = 1 * MONTH;
    public final static long SERVICE_TICKET_DEFAULT_TIME_OUT = 30 * MINUTE;
    public final static String TICKET_TYPE = "ticket_type";
    public final static String TGT = "tgt";
    public final static String SERVICEICKET = "ServiceTicket";
    public final static String DEVICE_TICKET = "DeviceTicket";
    public final static String SERVICE_NAME = "ServiceName";
    public final static String KEYSERVER_NAME = "KeyServerName";
    public final static String PLATFORM_NAME = "PlatformName";
    //Exception
    public final static String EXCEPTION = "Exception";
    public final static String INVALIDDEVICE = "InvalidDevice";
    public final static String INVALIDUSERNAME = "InvalidUserName";
    public final static String INVALIDSERVICE = "InvalidService";
    public final static String INVALIDKEYSERVER = "InvalidKeyServer";
    public final static String INVALIDPLATFORM = "InvalidPlatform";
    public final static String INVALID_DEVICE_TICKET = "InvalidDeviceTicket";
    public final static String INVALID_KDC_TICKET = "InvalidKDCTicket";
    public final static String INVALID_TGS_TICKET = "InvalidTGT";
    public final static String SESSION_TIME_OUT = "session time out";
    public final static String DEVICE_BLOCKED = "device has been blocked";
    public final static String WRONDKEY = "wrong key";
    public final static String INVALIDTICKET = "invalid ticket";
    public final static String INVALIDREQUEST = "invalid request";
    public final static String WRONGPASSWORD = "wrong password";
    public final static String PASSWORDRESET = "reset password";
    public final static String INVALIDSESSION = "invalid session";
    public final static String INVALIDSTEPID="invalid stepid";
    public final static String LACKOFPRIVILEGE="lack of privilege";
    //Data
    public final static String DATA = "Data";
    public final static String SERVICE_TICKET_PACKET_DEVICEID = "SERVICE_TICKET_PACKET_DEVICEID";
    public final static String SERVICE_TICKET_PACKET_USERNAME = "SERVICE_TICKET_PACKET_USERNAME";
    public final static String SERVICE_TICKET_PACKET_EXPIRY_TIME = "SERVICE_TICKET_PACKET_EXPIRY_TIME";
    public final static String SERVICE_TICKET_PACKET_SERVICE_SESSION_ID = "SERVICE_TICKET_PACKET_SERVICE_SESSION_ID";
    public final static String SERVICE_SESSION_ID = "SERVICE_SESSION_ID";
    public final static String AUTHENTICATOR = "AUTHENTICATOR";
    //Other
    public final static String FROM = " from ";
    public final static String DELIMITER = ":";
    public final static String DOUBLEDELIMITER = "::";
    public final static String BLOCK = ";";
    public final static String DEFAULT_IDENTIFIER = "id";
    public final static String SERVICE_SERVER = "service";
    public final static String KEYSERVER = "keyserver";
    public final static String DEFAULT_PLATFORM_NAME = "platform";
    public final static String DEFAULT_KEYSERVER_NAME = "keyserver";
    public final static String DEFAULT_SERVICESERVER_NAME = "serviceserver";
    public final static String PASSWORD = "@mobileplatform";
    public final static String DEFAULT_PLATFORM_PASSWORD = "platform" + PASSWORD;
    public final static String DEFAULT_KEYSERVER_PASSWORD = "keyserver" + PASSWORD;
    public final static String DEFAULT_SERVICESERVER_PASSWORD = "serviceserver" + PASSWORD;
    public final static String DEFALUT_ADMIN_PASSWORD = "admin" + PASSWORD;
    public final static String DEFAULT_SERVICE_KEY = "servicekey" + PASSWORD;
    //Privilege
    public final static String CONNECT = "connect";
    public final static String DEFAULT_PRIVILEDGE = "none";
    //KeyStore Type
    public final static String DEVICE = "Device";
    public final static String SERVICE = "Service";
    public final static String PLATFORM = "Platform";
    public final static String SERVICEPASSWORD = "ServicePassword";
    public final static String USER = "User";
    public final static String KEYSTORE = "Keystore";
    public final static String DEVICE_KEYSTORE = "Device" + KEYSTORE;
    public final static String SERVICE_KEYSTORE = "Service" + KEYSTORE;
    public final static String PLATFORM_KEYSTORE = "Platform" + KEYSTORE;
    public final static String SERVICEPASSWORD_KEYSTORE = "ServicePassword" + KEYSTORE;
    public final static String USER_KEYSTORE = "User" + KEYSTORE;
    public final static String PLATFORMPASSWORD_KEYSTORE = "PlatformPassword" + KEYSTORE;
    public final static String DEFALUT_USER_NAME = "user";
    public final static String DEFALUT_USER_PASSWORD = "User@123";
    public final static String DEFAULT_DEVICE_ID = "mydevice23";
    public final static String BLOCK_DEVICE_ID = "mydevice123";
    public final static String DEFAULT_DEVICE_PASSWORD = "mydevice@mobile";
    
}
