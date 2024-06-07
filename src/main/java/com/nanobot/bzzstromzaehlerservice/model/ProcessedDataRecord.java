package com.nanobot.bzzstromzaehlerservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProcessedDataRecord {
    private String timestamp;
    private String meterValue;
    private List<SdatRecord> volume;
}
