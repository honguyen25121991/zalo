package com.zalo.Spring_Zalo.Controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zalo.Spring_Zalo.Entities.Bill;
import com.zalo.Spring_Zalo.Entities.EnumManager;
import com.zalo.Spring_Zalo.Entities.Line;
import com.zalo.Spring_Zalo.Entities.Word;
import com.zalo.Spring_Zalo.Repo.BillRepo;
import com.zalo.Spring_Zalo.Repo.EventRepo;
import com.zalo.Spring_Zalo.Response.ApiResponse;
import com.zalo.Spring_Zalo.ServiceImpl.CustomerEventServiceImpl;
import com.zalo.Spring_Zalo.request.FileStorageManager;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/customer_event")
public class CustomerEventController {
    @Autowired
    private CustomerEventServiceImpl service;
    private EventRepo eventRepo;
    private BillRepo billRepo;
    private final String storageDirectory = FileStorageManager.getStorageDirectory();
    private static final Logger logger = LoggerFactory.getLogger(CustomerEventController.class);

    @PostMapping("/event/{eventId}/customer/{customerId}/upload")
    public Object handleFileUpload(@RequestPart("file") MultipartFile file,
            @PathVariable("customerId") Integer customerId, @PathVariable("eventId") Integer eventId) {
        try {
            logger.info(("save bill image >>>"));
            // Convert MultipartFile to BufferedImage
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

            // Create a temporary PNG file
            File tempFile = File.createTempFile("temp", ".png");

            // Use try-with-resources to ensure the file is closed
            try (OutputStream os = new FileOutputStream(tempFile)) {
                // Write BufferedImage to PNG file
                ImageIO.write(bufferedImage, "png", os);
            }
            Locale currentLocale = LocaleContextHolder.getLocale();
            // Use try-with-resources to ensure the HttpClient is closed
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                // Use try-with-resources to ensure the response entity is closed
                try (CloseableHttpResponse response = httpClient.execute(createOcrSpaceRequest(tempFile))) {
                    HttpEntity responseEntity = response.getEntity();
                    logger.info("Start sent request !");
                    if (responseEntity != null) {
                        // Read the JSON response from OCR Space API
                        logger.info("Response haven't data !");
                        String jsonResponse = EntityUtils.toString(responseEntity);
                        // JSONObject jsonResult = new JSONObject(jsonResponse);
                        return service.processFile(file, jsonResponse, currentLocale, customerId, eventId);
                    }
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            service.saveBillImage(file, eventId, customerId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the image.");
        } catch (IOException e) {
            e.printStackTrace();
            service.saveBillImage(file, eventId, customerId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing the image.");
        } catch (Exception e) {
            e.printStackTrace();
            service.saveBillImage(file, eventId, customerId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the image.");
        }
        service.saveBillImage(file, eventId, customerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Your image didn't have what we need");
    }

    public void saveBillImage(MultipartFile file, int eventId, int customerId) {
        try {
            String eventName = eventRepo.findById(eventId).get().getName();

            FileStorageManager fileStorageManager = new FileStorageManager(storageDirectory);
            String fileName = fileStorageManager.storeFile(file, eventId, customerId, LocalDateTime.now());
            String codeName = fileStorageManager.generateCodeName(eventId, customerId);
            Bill bill = new Bill();
            bill.setBillCode(codeName);
            bill.setImage(fileName);
            bill.setCreateDate(LocalDateTime.now());
            bill.setStatus(EnumManager.Billtatus.ERROR);
            bill.setDeleteFlag(false);
            bill.setCustomerId(customerId);
            bill.setEventId(eventId);
            bill.setEventName(eventName);
            billRepo.save(bill);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/scan-bill")
    public String test(@RequestPart("file") MultipartFile file) {
        try {

            // Convert MultipartFile to BufferedImage
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

            // Create a temporary PNG file
            File tempFile = File.createTempFile("temp", ".png");

            // Write BufferedImage to PNG file
            ImageIO.write(bufferedImage, "png", tempFile);

            // Use OCR Space API to process the image
            String apiKey = "K86621687288957"; // Replace with your OCR Space API key
            String ocrSpaceApiUrl = "https://api.ocr.space/parse/image";

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(ocrSpaceApiUrl);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("apikey", apiKey);
            builder.addTextBody("OCREngine", "2");
            builder.addTextBody("language", "eng");
            builder.addTextBody("isOverlayRequired", "true");

            builder.addBinaryBody("file", tempFile, ContentType.MULTIPART_FORM_DATA, tempFile.getName());

            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String result = EntityUtils.toString(responseEntity);
                String parsedText = extractParsedTextOB(result);
                Locale currentLocale = LocaleContextHolder.getLocale();
                // return service.ScanResultOCR(parsedText, currentLocale, eventId,customerId);
                return parsedText;
            }
            httpClient.close();

        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return "Error processing the image. Client Protocol Exception.";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error processing the image. IO Exception.";
        }

        return "Error processing the image. An unexpected exception occurred.";
    }

    private String extractParsedTextOB(String ocrResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(ocrResponse);

            JsonNode parsedResults = jsonNode.path("ParsedResults");

            if (parsedResults.isArray() && parsedResults.size() > 0) {
                JsonNode firstResult = parsedResults.get(0);
                JsonNode parsedText = firstResult.path("ParsedText");

                if (parsedText.isTextual()) {
                    return parsedText.asText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception as needed
        }

        return null; // Return null if ParsedText is not found or an error occurs
    }

    /**
     * 
     * @param file
     * @return
     */
    @PostMapping("/event/{eventId}/customer/{customerId}/scan-bill-OCR")
    public Object testOCRSpace(@RequestPart("file") MultipartFile file, @PathVariable("customerId") Integer customerId,
            @PathVariable("eventId") Integer eventId) {
        try {
            // int eventId= 3;
            // int customerId =1;
            // Convert MultipartFile to BufferedImage
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

            // Create a temporary PNG file
            File tempFile = File.createTempFile("temp", ".png");

            // Write BufferedImage to PNG file
            ImageIO.write(bufferedImage, "png", tempFile);

            // Use OCR Space API to process the image
            String apiKey = "K86621687288957"; // Replace with your OCR Space API key
            String ocrSpaceApiUrl = "https://api.ocr.space/parse/image";

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(ocrSpaceApiUrl);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("apikey", apiKey);
            builder.addTextBody("OCREngine", "2");
            builder.addTextBody("language", "eng");
            builder.addTextBody("isOverlayRequired", "true");

            builder.addBinaryBody("file", tempFile, ContentType.MULTIPART_FORM_DATA, tempFile.getName());

            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String result = EntityUtils.toString(responseEntity);
                String parsedText = extractParsedText(result);
                Locale currentLocale = LocaleContextHolder.getLocale();
                return service.ScanResultOCR(parsedText, currentLocale, eventId, customerId);
                // return parsedText;
            }

            httpClient.close();

        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the image.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the image.");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the image.");
    }

    /**
     * This is the function take out String parsedText from OCR result , return
     * String with long text to handle
     * 
     * @param ocrResponse
     * @return
     */
    private String extractParsedText(String ocrResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(ocrResponse);

            JsonNode parsedResults = jsonNode.path("ParsedResults");
            // handle Array of Object
            if (parsedResults.isArray() && parsedResults.size() > 0) {
                JsonNode firstResult = parsedResults.get(0);
                JsonNode parsedText = firstResult.path("ParsedText");

                return parsedText.isTextual() ? parsedText.asText() : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log or handle the exception as needed
        }

        return null; // Return null if ParsedText is not found or an error occurs
    }

    /**
     * 
     * @param file
     * @return
     */
    @PostMapping("/event/{eventId}/customer/{customerId}/scan-bill-OCR-mapping")
    public Object testOCRSpaceMapping(@RequestPart("file") MultipartFile file,
            @PathVariable("customerId") Integer customerId, @PathVariable("eventId") Integer eventId) {
        try {
            // Convert MultipartFile to BufferedImage
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

            // Create a temporary PNG file
            File tempFile = File.createTempFile("temp", ".png");

            // Use try-with-resources to ensure the file is closed
            try (OutputStream os = new FileOutputStream(tempFile)) {
                // Write BufferedImage to PNG file
                ImageIO.write(bufferedImage, "png", os);
            }
            Locale currentLocale = LocaleContextHolder.getLocale();
            // Use try-with-resources to ensure the HttpClient is closed
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                // Use try-with-resources to ensure the response entity is closed
                try (CloseableHttpResponse response = httpClient.execute(createOcrSpaceRequest(tempFile))) {
                    HttpEntity responseEntity = response.getEntity();

                    if (responseEntity != null) {
                        // Read the JSON response from OCR Space API
                        String jsonResponse = EntityUtils.toString(responseEntity);
                        JSONObject jsonResult = new JSONObject(jsonResponse);

                        // Get the ParsedResults array
                        JSONArray parsedResults = jsonResult.getJSONArray("ParsedResults");

                        if (parsedResults.length() > 0) {
                            // Get the first item from the ParsedResults array
                            JSONObject firstResult = parsedResults.getJSONObject(0);

                            // Check if it contains the TextOverlay object
                            if (firstResult.has("TextOverlay")) {
                                // Get the TextOverlay object
                                JSONObject textOverlay = firstResult.getJSONObject("TextOverlay");

                                // Check if it contains the Lines array
                                if (textOverlay.has("Lines")) {
                                    // Get the Lines array
                                    JSONArray linesArray = textOverlay.getJSONArray("Lines");
                                    // System.out.println(">> Line Array :"+linesArray.length());
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    List<Line> linesList = new ArrayList<>(
                                            Arrays.asList(objectMapper.readValue(linesArray.toString(), Line[].class)));
                                    try {
                                        service.ProcessingLine(linesList, currentLocale, customerId, eventId);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body("Error processing the image.");
                                    }
                                    for (int i = 0; i < linesArray.length(); i++) {
                                        // Get the Line JSON object
                                        JSONObject lineObject = linesArray.getJSONObject(i);
                                        // Map Line object
                                        Line line = Line.lineFromJson(lineObject);

                                        // Map Words array inside the Line
                                        if (lineObject.has("Words")) {
                                            JSONArray wordsArray = lineObject.getJSONArray("Words");
                                            Word[] words = Word.wordFromJson(wordsArray);
                                            line.setWords(Arrays.asList(words));
                                            // Add the Line object to the list
                                            linesList.add(line);
                                        }
                                    }

                                    // System.out.println(">>lineList: " + linesList);

                                    // return linesList;
                                    return service.ProcessingLine(linesList, currentLocale, customerId, eventId);
                                    // return service.ScanResultOCRMapping(linesList,currentLocale, customerId,
                                    // eventId);
                                }
                            }
                        }
                    }
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the image.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing the image.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the image.");
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Your image didn't have what we need");
    }

    private HttpPost createOcrSpaceRequest(File tempFile) {
        ResourceBundle infomation = ResourceBundle.getBundle("application");
        String apiKey = infomation.getString("OCRKey"); // Replace with your OCR Space API key
        String ocrSpaceApiUrl = infomation.getString("OcrURL");
        String ocrEngine = infomation.getString("OCREngine");
        String ocrLanguage = infomation.getString("OCRlanguage");
        HttpPost httpPost = new HttpPost(ocrSpaceApiUrl);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        builder.addTextBody("apikey", apiKey);
        builder.addTextBody("OCREngine", ocrEngine);
        builder.addTextBody("language", ocrLanguage);
        builder.addTextBody("isOverlayRequired", "true");
        builder.addBinaryBody("file", tempFile, ContentType.MULTIPART_FORM_DATA, tempFile.getName());

        httpPost.setEntity(builder.build());
        return httpPost;
    }

}
