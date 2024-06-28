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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

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

        // Generate JSON
        return generateJson();
    }

    private String generateJson() {
        ObjectNode root = mapper.createObjectNode();

        // Flatten and sort the sdatRecords list of lists
        List<SdatRecord> flattenedSdatRecords = allSdatRecords.stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(r -> LocalDateTime.parse(r.getTimestamp(), dateTimeFormatter)))
                .collect(Collectors.toList());

        // Generate meterData from SDAT records
        ArrayNode meterDataArray = mapper.createArrayNode();
        Map<String, List<SdatRecord>> sdatGroupedById = flattenedSdatRecords.stream()
                .collect(Collectors.groupingBy(SdatRecord::getDocumentID));

        for (Map.Entry<String, List<SdatRecord>> entry : sdatGroupedById.entrySet()) {
            ObjectNode sensorDataNode = mapper.createObjectNode();
            sensorDataNode.put("sensorId", entry.getKey());

            ArrayNode dataArray = mapper.createArrayNode();
            LocalDateTime currentTimestamp = null;
            int sequenceCount = 0;

            for (SdatRecord record : entry.getValue()) {
                LocalDateTime recordTimestamp = LocalDateTime.parse(record.getTimestamp(), dateTimeFormatter);

                if (currentTimestamp == null) {
                    currentTimestamp = recordTimestamp;
                }

                // Add padding records if there's a gap
                while (currentTimestamp.isBefore(recordTimestamp)) {
                    if (sequenceCount == 96) {
                        sequenceCount = 0;
                    }

                    ObjectNode paddingNode = mapper.createObjectNode();
                    paddingNode.put("ts", currentTimestamp.format(dateTimeFormatter));
                    paddingNode.put("value", 0); // Assuming 0 for padding
                    paddingNode.put("sequence", sequenceCount);
                    dataArray.add(paddingNode);

                    currentTimestamp = currentTimestamp.plusMinutes(15);
                    sequenceCount++;
                }

                if (sequenceCount == 96) {
                    sequenceCount = 0;
                }

                ObjectNode dataNode = mapper.createObjectNode();
                dataNode.put("ts", record.getTimestamp());
                dataNode.put("value", record.getValue());
                dataNode.put("sequence", sequenceCount);
                dataArray.add(dataNode);

                currentTimestamp = currentTimestamp.plusMinutes(15);
                sequenceCount++;
            }

            sensorDataNode.set("data", dataArray);
            meterDataArray.add(sensorDataNode);
        }

        // Generate volumeData from ESL records
        ArrayNode volumeDataArray = mapper.createArrayNode();
        Map<String, Map<String, EslRecord>> eslGroupedByTimestampAndObis = allEslRecords.stream()
                .collect(Collectors.groupingBy(EslRecord::getTimestamp, Collectors.toMap(EslRecord::getObis, e -> e, (e1, e2) -> e1)));

        // For building consumption and production data
        ArrayNode consumptionProductionArray = mapper.createArrayNode();

        for (Map.Entry<String, Map<String, EslRecord>> timestampEntry : eslGroupedByTimestampAndObis.entrySet()) {
            ObjectNode volumeNode = mapper.createObjectNode();
            volumeNode.put("ts", timestampEntry.getKey());

            ArrayNode dataArray = mapper.createArrayNode();
            double consumption = 0.0;
            double production = 0.0;

            for (Map.Entry<String, EslRecord> obisEntry : timestampEntry.getValue().entrySet()) {
                EslRecord record = obisEntry.getValue();
                ObjectNode dataNode = mapper.createObjectNode();
                dataNode.put("obis", obisEntry.getKey());
                dataNode.put("value", record.getValue());
                dataArray.add(dataNode);

                // Aggregate consumption and production values
                if (obisEntry.getKey().equals("1-1:1.8.1") || obisEntry.getKey().equals("1-1:1.8.2")) {
                    consumption += Double.parseDouble(record.getValue());
                } else if (obisEntry.getKey().equals("1-1:2.8.1") || obisEntry.getKey().equals("1-1:2.8.2")) {
                    production += Double.parseDouble(record.getValue());
                }
            }

            volumeNode.set("data", dataArray);
            volumeDataArray.add(volumeNode);

            // Add to consumption and production array
            ObjectNode cpNode = mapper.createObjectNode();
            cpNode.put("ts", timestampEntry.getKey());
            cpNode.put("consumption", consumption);
            cpNode.put("production", production);
            consumptionProductionArray.add(cpNode);
        }

        root.set("meterData", meterDataArray);
        root.set("volumeData", volumeDataArray);
        root.set("consumptionProduction", consumptionProductionArray);

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
