/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.neu.coe.platform.keyserver.ticket;

import com.tazdingo.core.util.ConstantUtil;
import com.tazdingo.ticket.TGT;



/**
 *
 * @author Cynthia
 */
public class PlatformTGT extends TGT{
    
    public PlatformTGT(String sessionid,String platformname){
        super(sessionid);
        super.authenticator=platformname;
        super.expiredTime=System.currentTimeMillis()+ConstantUtil.PLATFORM_TICKET_ACTIVE_TIME;
        
    }
    
}
