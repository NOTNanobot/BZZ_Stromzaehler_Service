package com.nanobot.bzzstromzaehlerservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SdatRecord {
    private String documentID;
    private String timestamp;
    private String value;
    private String sequence;
}
