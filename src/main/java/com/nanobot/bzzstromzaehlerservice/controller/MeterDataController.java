package com.nanobot.bzzstromzaehlerservice.controller;

import com.nanobot.bzzstromzaehlerservice.service.MeterDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
