package com.nanobot.bzzstromzaehlerservice.controller;

import com.nanobot.bzzstromzaehlerservice.model.MeterReading;
import com.nanobot.bzzstromzaehlerservice.service.MeterDataService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meters")
public class MeterDataController {
    @Autowired
    private MeterDataService service;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        try {
            service.uploadFiles(files);
            return ResponseEntity.ok("Files uploaded and processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing files: " + e.getMessage());
        }
    }

    @GetMapping("/processed")
    public ResponseEntity<List<MeterReading>> getProcessedReadings() {
        return ResponseEntity.ok(service.getProcessedReadings());
    }

    @GetMapping("/export/csv")
    public void exportToCsv(HttpServletResponse response) throws IOException {
        service.exportToCsv(response);
    }

    @GetMapping("/export/json")
    public ResponseEntity<List<MeterReading>> exportToJson() {
        return ResponseEntity.ok(service.exportToJson());
    }

    @GetMapping("/visualize/consumption")
    public ResponseEntity<Map<String, Object>> visualizeConsumption() {
        return ResponseEntity.ok(service.visualizeConsumption());
    }

    @GetMapping("/visualize/meter")
    public ResponseEntity<Map<String, Object>> visualizeMeterReadings() {
        return ResponseEntity.ok(service.visualizeMeterReadings());
    }
}
