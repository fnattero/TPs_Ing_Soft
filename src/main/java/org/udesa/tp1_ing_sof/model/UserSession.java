package org.udesa.tp1_ing_sof.model;

import java.time.LocalDateTime;

public class UserSession {
    public int token;
    public String userName;
    public Clock expireTime;

    public UserSession(int token, String userName, Clock expireTime) {
        this.token = token;
        this.userName = userName;
        this.expireTime = expireTime;
    }

    public boolean isExpired(Clock now){
        return expireTime.getTime().isBefore(now.getTime());
    }

}
