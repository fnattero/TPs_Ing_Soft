package org.udesa.tp1_ing_sof.model;

import java.time.LocalDateTime;

public class Clock {
    LocalDateTime time;

    public Clock(LocalDateTime localDateTime) {
        this.time = localDateTime;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
