package org.udesa.tp1_ing_sof.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TokenSession {
    private LocalDateTime lastAccess;
    private Clock clock;
    private String username;

    public TokenSession(String username, Clock clock) {
        this.clock = clock;
        this.lastAccess = clock.getTime();
        this.username = username;
    }

    public void updateLastAccess() {
        this.lastAccess = clock.getTime();
    }

    public boolean isExpired(){
        return clock.getTime().isAfter(this.lastAccess.plusMinutes(5));
    }

    public String getUsername() {
        return username;
    }
}
