package com.nanobot.bzzstromzaehlerservice.controller;

import com.nanobot.bzzstromzaehlerservice.model.ProcessedDataRecord;
import com.nanobot.bzzstromzaehlerservice.service.MeterDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meters")
@Slf4j
public class MeterDataController {
    @Autowired
    private MeterDataService service;


    @GetMapping("/process")
    public ResponseEntity<List<ProcessedDataRecord>> processFilesFromDirectories() throws Exception {
        return ResponseEntity.ok(service.processFilesFromDirectories());
    }

}
