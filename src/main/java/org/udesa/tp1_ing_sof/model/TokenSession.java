package org.udesa.tp1_ing_sof.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TokenSession {
    private LocalDateTime creationTime;
    private Clock clock;

    public TokenSession(Clock clock) {
        this.clock = clock;
        this.creationTime = clock.getTime();
    }

    public boolean isExpired(){
        return clock.getTime().isAfter(this.creationTime.plusMinutes(5));
    }
}
