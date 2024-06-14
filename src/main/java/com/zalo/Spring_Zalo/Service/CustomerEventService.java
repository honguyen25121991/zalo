package com.zalo.Spring_Zalo.Service;

import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.zalo.Spring_Zalo.Entities.ItemPointReturn;
import com.zalo.Spring_Zalo.Entities.Line;
import com.zalo.Spring_Zalo.Response.ReplacementResult;

import jakarta.servlet.http.HttpSession;

public interface CustomerEventService {
    Object ScanResult(String result, Locale currentLocale, Integer eventId, Integer customerId);

    Object ScanResultOCR(String result, Locale currentLocale, Integer eventId, Integer customerId);

    Object ScanResultOCRMapping(List<Line> linesList, Locale currentLocale, Integer eventId, Integer customerId);

    Object ProcessingLine(List<Line> lines, Locale currentLocale, Integer customerId, Integer eventId);

    ReplacementResult keyexchange(String text, Locale currentLocale);

    ResponseEntity<ItemPointReturn> processFile(MultipartFile file, String json, Locale currentLocale,
            Integer customerId, Integer eventId);
}
