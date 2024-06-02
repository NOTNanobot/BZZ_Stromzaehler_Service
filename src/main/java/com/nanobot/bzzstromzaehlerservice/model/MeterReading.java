package com.nanobot.bzzstromzaehlerservice.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MeterReading {
    private LocalDateTime timestamp;
    private Double value;
    private String type;  // "consumption" or "meter"
}
