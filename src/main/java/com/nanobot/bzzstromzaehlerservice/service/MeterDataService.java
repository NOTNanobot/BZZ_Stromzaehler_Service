package com.nanobot.bzzstromzaehlerservice.service;

import com.nanobot.bzzstromzaehlerservice.model.MeterReading;
import com.nanobot.bzzstromzaehlerservice.util.DataProcessor;
import com.nanobot.bzzstromzaehlerservice.util.Exporter;
import com.nanobot.bzzstromzaehlerservice.util.FileParser;
import com.nanobot.bzzstromzaehlerservice.util.Visualizer;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MeterDataService {
    private List<MeterReading> sdatReadings = new ArrayList<>();
    private List<MeterReading> eslReadings = new ArrayList<>();

    private List<MeterReading> readings = new ArrayList<>();

    public void uploadFiles(List<MultipartFile> files) throws Exception {
        List<MeterReading> sdatReadings = new ArrayList<>();
        List<MeterReading> eslReadings = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            if (fileName != null) {
                if (fileName.contains("sdat")) {
                    sdatReadings.addAll(FileParser.parseSdat(file.getInputStream()));
                } else if (fileName.contains("esl")) {
                    eslReadings.addAll(FileParser.parseEsl(file.getInputStream()));
                }
            }
        }

        List<MeterReading> integratedData = DataProcessor.integrateData(sdatReadings, eslReadings);
        this.readings = DataProcessor.calculateCumulativeValues(DataProcessor.sortByTimestamp(DataProcessor.removeDuplicates(integratedData)));
    }

    public List<MeterReading> processFiles() {
        List<MeterReading> allReadings = new ArrayList<>();
        allReadings.addAll(DataProcessor.integrateData(sdatReadings, eslReadings));
        return allReadings;
    }

    public void exportToCsv(HttpServletResponse response) throws IOException {
        Exporter.exportToCsv(processFiles(), response);
    }

    public List<MeterReading> exportToJson() {
        return Exporter.exportToJson(processFiles());
    }

    public Map<String, Object> visualizeConsumption() {
        return Visualizer.plotConsumption(readings);
    }

    public Map<String, Object> visualizeMeterReadings() {
        return Visualizer.plotMeterReadings(readings);
    }

    public List<MeterReading> getProcessedReadings() {
        return readings;
    }
}
