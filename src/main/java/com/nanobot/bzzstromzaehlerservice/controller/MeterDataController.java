package com.nanobot.bzzstromzaehlerservice.controller;

import com.nanobot.bzzstromzaehlerservice.service.MeterDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/meters")
@Slf4j
public class MeterDataController {
    @Autowired
    private MeterDataService service;


    @GetMapping("/process")
    public ResponseEntity<String> processFilesFromDirectories() throws Exception {
        return ResponseEntity.ok(service.processFilesFromDirectories());
    }

    @GetMapping("/download/csv")
    public ResponseEntity<FileSystemResource> exportConsumptionProductionCsv() {
        try {
            File csvFile = service.exportConsumptionProductionToCsv();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=consumption_production.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(new FileSystemResource(csvFile));
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/send-json")
    public ResponseEntity<String> sendJsonToServer() {
        boolean success = service.sendJsonToServer();

        if (success) {
            return ResponseEntity.ok("JSON data successfully sent to the server.");
        } else {
            return ResponseEntity.status(500).body("Failed to send JSON data to the server.");
        }
    }

}
