package org.udesa.tp1_ing_sof.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TokenSession {
    private LocalDateTime creationTime;
    private Clock clock;
    private String username;

    public TokenSession(String username, Clock clock) {
        this.clock = clock;
        this.creationTime = clock.getTime();
        this.username = username;
    }

    public boolean isExpired(){
        return clock.getTime().isAfter(this.creationTime.plusMinutes(5));
    }

    public String getUsername() {
        return username;
    }
}
