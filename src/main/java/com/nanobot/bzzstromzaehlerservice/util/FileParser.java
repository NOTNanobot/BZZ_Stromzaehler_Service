package com.nanobot.bzzstromzaehlerservice.util;

import com.nanobot.bzzstromzaehlerservice.model.MeterReading;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileParser {
    public static List<MeterReading> parseSdat(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        List<MeterReading> readings = new ArrayList<>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("rsm:Observation");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Element element = (Element) nList.item(temp);
            MeterReading reading = new MeterReading();
            reading.setTimestamp(LocalDateTime.parse(element.getElementsByTagName("rsm:Position").item(0).getTextContent(), DateTimeFormatter.ISO_DATE_TIME));
            reading.setValue(Double.parseDouble(element.getElementsByTagName("rsm:Volume").item(0).getTextContent()));
            reading.setType("consumption");
            readings.add(reading);
        }

        return readings;
    }

    public static List<MeterReading> parseEsl(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        List<MeterReading> readings = new ArrayList<>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("ValueRow");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Element element = (Element) nList.item(temp);
            MeterReading reading = new MeterReading();
            reading.setTimestamp(LocalDateTime.parse(element.getAttribute("valueTimeStamp"), DateTimeFormatter.ISO_DATE_TIME));
            reading.setValue(Double.parseDouble(element.getAttribute("value")));
            reading.setType("meter");
            readings.add(reading);
        }

        return readings;
    }
}

