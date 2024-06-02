package com.nanobot.bzzstromzaehlerservice.util;

import com.nanobot.bzzstromzaehlerservice.model.MeterReading;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Exporter {
    public static void exportToCsv(List<MeterReading> data, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        PrintWriter writer = response.getWriter();
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("timestamp", "value", "type"));

        for (MeterReading reading : data) {
            csvPrinter.printRecord(reading.getTimestamp(), reading.getValue(), reading.getType());
        }

        csvPrinter.flush();
    }

    public static List<MeterReading> exportToJson(List<MeterReading> data) {
        return data;
    }

    public static ResponseEntity<String> postToServer(List<MeterReading> data, String url) {
        // Implement HTTP POST logic
        return ResponseEntity.ok("Data posted successfully");
    }
}
