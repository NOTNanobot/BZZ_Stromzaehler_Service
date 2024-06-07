package com.nanobot.bzzstromzaehlerservice.service;

import com.nanobot.bzzstromzaehlerservice.model.EslRecord;
import com.nanobot.bzzstromzaehlerservice.model.ProcessedDataRecord;
import com.nanobot.bzzstromzaehlerservice.model.SdatRecord;
import com.nanobot.bzzstromzaehlerservice.util.FileParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MeterDataService {

    @Autowired
    private FileParser fileParser;

    private String rootDirectory = System.getProperty("user.dir");

    @Value("${sdat.file.path}")
    private String sdatFilePath;

    @Value("${esl.file.path}")
    private String eslFilePath;

    private List<EslRecord> allEslRecords = new ArrayList<>();
    private List<List<SdatRecord>> allSdatRecords = new ArrayList<>();
    private List<ProcessedDataRecord> processedDataRecords = new ArrayList<>();

    public List<ProcessedDataRecord> processFilesFromDirectories() throws Exception {


        // Process SDAT files
        File sdatDirectory = new File(rootDirectory + sdatFilePath);
        if (sdatDirectory.isDirectory()) {
            File[] files = sdatDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        List<SdatRecord> records = fileParser.parseSdatFile(file);
                        allSdatRecords.add(records);
                    }
                }
            }
        }

        // Process ESL files
        File eslDirectory = new File(rootDirectory + eslFilePath);
        if (eslDirectory.isDirectory()) {
            File[] files = eslDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        List<EslRecord> records = fileParser.parseEslFile(file);
                        allEslRecords.addAll(records);
                    }
                }
            }
        }
    }

    private void processRecords() {
        List<List<SdatRecord>> allSdatRecords = new ArrayList<>();

        allSdatRecords.stream().flatMap(List::stream).
                forEach(sdatRecord -> {
                    String timestamp = sdatRecord.getTimestamp();
                    String sequence = sdatRecord.getSequence();
                    List<SdatRecord> sdatRecordList = allSdatRecords.stream().
                            flatMap(List::stream).
                            filter(record -> record.getTimestamp().equals(timestamp)).
                            collect(Collectors.toList());
                    sdatRecordList.sort((r1, r2) -> r1.getSequence().compareTo(r2.getSequence()));
                    allSdatRecords.add(sdatRecordList);
                });

}
