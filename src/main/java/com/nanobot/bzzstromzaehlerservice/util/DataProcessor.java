package com.nanobot.bzzstromzaehlerservice.util;

import com.nanobot.bzzstromzaehlerservice.model.MeterReading;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataProcessor {
    public static List<MeterReading> removeDuplicates(List<MeterReading> data) {
        return data.stream().distinct().collect(Collectors.toList());
    }

    public static List<MeterReading> sortByTimestamp(List<MeterReading> data) {
        return data.stream().sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp())).collect(Collectors.toList());
    }

    public static List<MeterReading> integrateData(List<MeterReading> sdatData, List<MeterReading> eslData) {
        List<MeterReading> integratedData = new ArrayList<>(sdatData);
        integratedData.addAll(eslData);
        return integratedData;
    }

    public static List<MeterReading> calculateCumulativeValues(List<MeterReading> data) {
        List<MeterReading> sortedData = sortByTimestamp(data);
        double cumulativeValue = 0;
        for (MeterReading reading : sortedData) {
            cumulativeValue += reading.getValue();
            reading.setValue(cumulativeValue);
        }
        return sortedData;
    }
}
