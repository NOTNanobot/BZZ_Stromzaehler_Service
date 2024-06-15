package com.nanobot.bzzstromzaehlerservice.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MeterResponse {
    private String sensorId;
    private List<MeterDataResponse> data;
}
