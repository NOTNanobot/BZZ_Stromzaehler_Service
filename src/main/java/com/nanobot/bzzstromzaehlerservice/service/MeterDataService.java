package com.nanobot.bzzstromzaehlerservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nanobot.bzzstromzaehlerservice.model.EslRecord;
import com.nanobot.bzzstromzaehlerservice.model.SdatRecord;
import com.nanobot.bzzstromzaehlerservice.model.response.MeterDataResponse;
import com.nanobot.bzzstromzaehlerservice.util.FileParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
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

    @Value("${docID.processing}")
    private List<String> processingDocIDs;

    private List<EslRecord> allEslRecords = new ArrayList<>();
    private List<List<SdatRecord>> allSdatRecords = new ArrayList<>();

    public String processFilesFromDirectories() throws Exception {


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
        return processRecords();
    }

    private String processRecords() {
        /// Create a map to keep track of unique timestamps globally
        Map<String, List<SdatRecord>> globalUniqueTimestamps = new LinkedHashMap<>();

        // Resulting list of lists without duplicates between lists
        List<List<SdatRecord>> resultListOfLists = new ArrayList<>();

        // Process each sublist
        for (List<SdatRecord> sublist : allSdatRecords) {

            if (!globalUniqueTimestamps.containsKey(sublist.get(1).getTimestamp())) {
                globalUniqueTimestamps.put(sublist.get(1).getTimestamp(), sublist);
                resultListOfLists.add(sublist);
            }

        }


        // Sum values for high tariff and low tariff
        Map<String, Double> consumptionMap = new HashMap<>();
        Map<String, Double> productionMap = new HashMap<>();

        for (EslRecord record : allEslRecords) {
            String key = record.getTimestamp();
            double value = Double.parseDouble(record.getValue());
            if ("1-1:1.8.1".equals(record.getObis()) || "1-1:1.8.2".equals(record.getObis())) {
                consumptionMap.merge(key, value, Double::sum);
            } else if ("1-1:2.8.1".equals(record.getObis()) || "1-1:2.8.2".equals(record.getObis())) {
                productionMap.merge(key, value, Double::sum);
            }
        }

        // Generate JSON
        return generateJson(resultListOfLists, consumptionMap, productionMap);
    }

    private String generateJson(List<List<SdatRecord>> sdatRecords, Map<String, Double> consumptionMap, Map<String, Double> productionMap) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        // Flatten the sdatRecords list of lists
        List<SdatRecord> flattenedSdatRecords = sdatRecords.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Generate meterData from SDAT records
        ArrayNode meterDataArray = mapper.createArrayNode();
        Map<String, List<SdatRecord>> sdatGroupedById = flattenedSdatRecords.stream()
                .collect(Collectors.groupingBy(SdatRecord::getDocumentID));

        for (Map.Entry<String, List<SdatRecord>> entry : sdatGroupedById.entrySet()) {
            ObjectNode sensorDataNode = mapper.createObjectNode();
            sensorDataNode.put("sensorId", entry.getKey());

            ArrayNode dataArray = mapper.createArrayNode();
            for (SdatRecord record : entry.getValue()) {
                ObjectNode dataNode = mapper.createObjectNode();
                dataNode.put("ts", record.getTimestamp());
                dataNode.put("value", entry.getKey().contains("ID742") ? consumptionMap.getOrDefault(record.getTimestamp(), 0.0) : productionMap.getOrDefault(record.getTimestamp(), 0.0));
                dataArray.add(dataNode);
            }

            sensorDataNode.set("data", dataArray);
            meterDataArray.add(sensorDataNode);
        }

        // Generate volumeData from SDAT records
        ArrayNode volumeDataArray = mapper.createArrayNode();
        Map<String, List<SdatRecord>> sdatGroupedByTimestamp = flattenedSdatRecords.stream()
                .collect(Collectors.groupingBy(SdatRecord::getTimestamp));

        for (Map.Entry<String, List<SdatRecord>> entry : sdatGroupedByTimestamp.entrySet()) {
            ObjectNode volumeNode = mapper.createObjectNode();
            volumeNode.put("ts", entry.getKey());

            ArrayNode dataArray = mapper.createArrayNode();
            for (SdatRecord record : entry.getValue()) {
                ObjectNode dataNode = mapper.createObjectNode();
                dataNode.put("sequence", record.getSequence() != null ? Integer.parseInt(record.getSequence()) : 0);
                dataNode.put("value", record.getValue() != null ? Double.parseDouble(record.getValue()) : 0.0);
                dataArray.add(dataNode);
            }

            volumeNode.set("data", dataArray);
            volumeDataArray.add(volumeNode);
        }

        root.set("meterData", meterDataArray);
        root.set("volumeData", volumeDataArray);

        // Convert to JSON string and log
        try {
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            log.info(jsonString);
            return jsonString;
        } catch (Exception e) {
            log.error("Failed to generate JSON", e);
        }
        return null;
    }
}
