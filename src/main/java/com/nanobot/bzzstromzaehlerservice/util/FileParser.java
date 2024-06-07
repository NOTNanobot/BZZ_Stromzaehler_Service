package com.nanobot.bzzstromzaehlerservice.util;

import com.nanobot.bzzstromzaehlerservice.model.EslRecord;
import com.nanobot.bzzstromzaehlerservice.model.SdatRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileParser {



    public List<EslRecord> parseEslFile(File inputFile) throws IOException, SAXException, ParserConfigurationException {
        try {

            // Create a DocumentBuilderFactory and configure it
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            // Parse the XML file and build the Document object
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // Print root element
            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

            // Retrieve all elements with the tag name "TimePeriod"
            NodeList timePeriodList = doc.getElementsByTagName("TimePeriod");

            // Create a list to store the extracted EslRecord data
            List<EslRecord> records = new ArrayList<>();

            // Loop through each TimePeriod element
            for (int i = 0; i < timePeriodList.getLength(); i++) {
                Node timePeriodNode = timePeriodList.item(i);

                if (timePeriodNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element timePeriodElement = (Element) timePeriodNode;

                    // Extract the end attribute of TimePeriod
                    String end = timePeriodElement.getAttribute("end");

                    // Retrieve all ValueRow elements within this TimePeriod
                    NodeList valueRowList = timePeriodElement.getElementsByTagName("ValueRow");

                    // Loop through each ValueRow element
                    for (int j = 0; j < valueRowList.getLength(); j++) {
                        Node valueRowNode = valueRowList.item(j);

                        if (valueRowNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element valueRowElement = (Element) valueRowNode;

                            // Extract the attributes of ValueRow
                            String obis = valueRowElement.getAttribute("obis");

                            // Filter based on the required obis values
                            if (obis.equals("1-1:1.8.1") || obis.equals("1-1:1.8.2") || obis.equals("1-1:2.8.1") || obis.equals("1-1:2.8.2")) {
                                String timestamp = end;
                                String value = valueRowElement.getAttribute("value");

                                // Create a new EslRecord object and add it to the list
                                EslRecord eslRecord = new EslRecord(timestamp, value, obis);
                                records.add(eslRecord);
                            }
                        }
                    }
                }
            }

            // Print all extracted data
            for (EslRecord record : records) {
                System.out.println(record);
            }

            return records;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<SdatRecord> parseSdatFile(File inputFile) {
        try {

            // Create a DocumentBuilderFactory and configure it
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            // Parse the XML file and build the Document object
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // Print root element
            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

            // Retrieve the DocumentID element
            NodeList documentIDList = doc.getElementsByTagName("rsm:DocumentID");
            String documentID = documentIDList.item(0).getTextContent();

            // Retrieve the Interval element for timestamps
            NodeList intervalList = doc.getElementsByTagName("rsm:Interval");
            String startDateTime = "";
            String endDateTime = "";

            if (intervalList.getLength() > 0) {
                Node intervalNode = intervalList.item(0);
                if (intervalNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element intervalElement = (Element) intervalNode;
                    startDateTime = intervalElement.getElementsByTagName("rsm:StartDateTime").item(0).getTextContent();
                    endDateTime = intervalElement.getElementsByTagName("rsm:EndDateTime").item(0).getTextContent();
                }
            }

            // Retrieve all Observation elements
            NodeList observationList = doc.getElementsByTagName("rsm:Observation");

            // Create a list to store the extracted EslRecord data
            List<SdatRecord> records = new ArrayList<>();

            // Loop through each Observation element
            for (int i = 0; i < observationList.getLength(); i++) {
                Node observationNode = observationList.item(i);

                if (observationNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element observationElement = (Element) observationNode;

                    // Extract the Volume value
                    String volume = observationElement.getElementsByTagName("rsm:Volume").item(0).getTextContent();
                    String sequence = observationElement.getElementsByTagName("rsm:Sequence").item(0).getTextContent();

                    // Create a new EslRecord object and add it to the list
                    SdatRecord sdatRecord = new SdatRecord(documentID, startDateTime, volume, sequence);
                    records.add(sdatRecord);
                }
            }

            // Print all extracted data
            for (SdatRecord record : records) {
                System.out.println(record);
            }
            // Remove duplicates based on timestamp
            /*records = records.stream()
                    .collect(Collectors.toMap(SdatRecord::getTimestamp, r -> r, (r1, r2) -> r1))
                    .values()
                    .stream()
                    .sorted((r1, r2) -> r1.getTimestamp().compareTo(r2.getTimestamp()))
                    .collect(Collectors.toList());*/
            return records;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
