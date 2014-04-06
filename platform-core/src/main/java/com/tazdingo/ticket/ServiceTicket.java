package com.tazdingo.ticket;

import com.tazdingo.core.util.ConstantUtil;

/**
 *
 * @author Cynthia
 */
public class ServiceTicket {

    private final String Privilege;
    private TGT tgt = new TGT();
    private Long expiredtime;
    private String stepid;

    public ServiceTicket(String Privilege, TGT tgt, String stepid) {
        this.Privilege = Privilege;
        this.tgt = tgt;
        this.expiredtime = System.currentTimeMillis() + ConstantUtil.SERVICE_TICKET_DEFAULT_TIME_OUT;
        this.stepid = stepid;
    }

    public String converToString() {
        return tgt.converToString() + ConstantUtil.BLOCK + expiredtime + ConstantUtil.BLOCK + Privilege + ConstantUtil.BLOCK + stepid;
    }

    public ServiceTicket(String ticket) {
        String[] value = ticket.split(ConstantUtil.BLOCK);
        tgt.setTicket(value[0]);
        expiredtime = Long.parseLong(value[1]);
        Privilege = value[2];
        stepid = value[3];
    }

    public TGT getKdcticket() {
        return tgt;
    }

    public String getPriviledge() {
        return Privilege;
    }

    public Long getExpiredtime() {
        return expiredtime;
    }

    public void renewTicket(String stepid) {
        this.expiredtime = System.currentTimeMillis() + ConstantUtil.SERVICE_TICKET_ACTIVE_TIME;
        this.stepid = stepid;
    }

    public String getStepid() {
        return stepid;
    }

    public void setKdcticket(TGT tgt) {
        this.tgt = tgt;
    }
    
}
