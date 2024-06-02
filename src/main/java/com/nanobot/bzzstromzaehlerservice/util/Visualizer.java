package com.nanobot.bzzstromzaehlerservice.util;

import com.nanobot.bzzstromzaehlerservice.model.MeterReading;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Visualizer {
    public static Map<String, Object> plotConsumption(List<MeterReading> data) {
        List<MeterReading> consumptionData = data.stream()
                .filter(reading -> "consumption".equals(reading.getType()))
                .collect(Collectors.toList());

        List<String> timestamps = consumptionData.stream()
                .map(reading -> reading.getTimestamp().toString())
                .collect(Collectors.toList());

        List<Double> values = consumptionData.stream()
                .map(MeterReading::getValue)
                .collect(Collectors.toList());

        return Map.of("timestamps", timestamps, "values", values);
    }

    public static Map<String, Object> plotMeterReadings(List<MeterReading> data) {
        List<MeterReading> meterData = data.stream()
                .filter(reading -> "meter".equals(reading.getType()))
                .collect(Collectors.toList());

        List<String> timestamps = meterData.stream()
                .map(reading -> reading.getTimestamp().toString())
                .collect(Collectors.toList());

        List<Double> values = meterData.stream()
                .map(MeterReading::getValue)
                .collect(Collectors.toList());

        return Map.of("timestamps", timestamps, "values", values);
    }
}
