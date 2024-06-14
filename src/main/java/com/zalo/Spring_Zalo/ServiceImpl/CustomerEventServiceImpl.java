package com.zalo.Spring_Zalo.ServiceImpl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zalo.Spring_Zalo.Controller.CustomerController;
import com.zalo.Spring_Zalo.DTO.DateConverterDTO;
import com.zalo.Spring_Zalo.Entities.*;
import com.zalo.Spring_Zalo.Exception.ApiNotFoundException;
import com.zalo.Spring_Zalo.Exception.ResourceNotFoundException;
import com.zalo.Spring_Zalo.Repo.*;
import com.zalo.Spring_Zalo.Response.ReplacementResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.zalo.Spring_Zalo.Service.CustomerEventService;
import com.zalo.Spring_Zalo.request.FileStorageManager;

@Service
public class CustomerEventServiceImpl implements CustomerEventService {
    @Autowired
    private ProductEventRepo productEventRepo;

    @Autowired
    private CustomerRewardRepo customerRewardRepo;
    @Autowired
    private CustomerPointRepo customerPointRepo;

    @Autowired
    private BillServiceImpl billServiceImpl;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private EventRepo eventRepo;

    @Autowired
    private BillRepo billRepo;

    private final String storageDirectory = FileStorageManager.getStorageDirectory();
    private static final Logger logger = LoggerFactory.getLogger(CustomerEventServiceImpl.class);

    /**
     * @param result
     * @param currentLocale
     * @return
     */
    @Override
    public Object ScanResult(String result, Locale currentLocale, Integer eventId, Integer customerId) {
        logger.info("ScanResult :>>>");
        System.out.println("ScanResult :>>>");
        Bill bill = new Bill();
        String billsaveCode = "";
        LocalDate billsaveDate = LocalDate.now();
        ResourceBundle keyWord = ResourceBundle.getBundle("keyWord", currentLocale);
        String billingDate = keyWord.getString("keyBillingDate");
        String startBillNo = keyWord.getString("keyStartBillNo");
        String startBillNo2 = keyWord.getString("keyStartBillNo2");
        String endListItem = keyWord.getString("keyEndListItem");// ="Cong tién hang"
        String startListItem = keyWord.getString("keyStartListItem");
        String endListItem2 = keyWord.getString("keyEndListItem2");
        List<String> lines = Arrays.asList(result.split("\n"));
        List<Item> items = new ArrayList<>();
        ItemReturn itemReturn = new ItemReturn();
        boolean isListItem = false;
        boolean isBillNo = false;

        int totalPoint = 0;

        // create a loop to Approve each line
        for (String line : lines) {
            if (line.equals(billingDate)) {
                System.out.println(billingDate);
                continue;
            }
            if (line.equals(startBillNo) || line.equals(startBillNo2)) {
                isBillNo = true;
                continue;
            }
            if (isBillNo) {
                bill = billServiceImpl.findBillByCode(line);
                logger.info("bill->>" + bill);
                System.out.println("bill->>" + bill);
                if (bill != null) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("This bill already Used! ");
                }
                billsaveDate = LocalDate.now();
                System.out.println("code Line =>> " + line);
                billsaveCode = line;

                isBillNo = false;
                continue;
            }
            if (line.contains(startListItem)) {
                isListItem = true;
                continue;
            }
            if (isListItem) {
                System.out.println("Process bill 1 ");
                // found list product in bill and begin to read infomation each product to get
                // list item
                processListItemOCR(lines, line, items, itemReturn, endListItem, endListItem2, isListItem, eventId,
                        totalPoint, currentLocale);
                break;
            }

        }
        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Item List is empty , please check your recive! ");

        }
        System.out.println("->>DAte " + billsaveDate);
        System.out.println("->> Code " + billsaveCode);
        Bill billSave = new Bill();
        billSave.setBillCode(billsaveCode);
        // System.out.println("->> Code "+ bill.getBillCode());
        billSave.setScanDate(billsaveDate);
        // System.out.println("->> Date "+ bill.getScanDate());
        billServiceImpl.savebill(billSave);

        Optional<CustomerPoint> customerPointOptional = customerPointRepo.findByCustomerAndEvent(customerId, eventId);
        CustomerPoint customerPoint;
        if (customerPointOptional.isPresent()) {
            customerPoint = customerPointOptional.get();
            int currentTotalPoint = customerPoint.getPoint() + itemReturn.getTotalPoint();
            customerPoint.setPoint(currentTotalPoint);
        } else {
            Customer customer = customerRepo.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "CustomerId", customerId));
            Event event = eventRepo.findById(3).orElseThrow(() -> new ResourceNotFoundException("Event", "EventId", 3));
            // Nếu không tìm thấy đối tượng CustomerPoint, bạn có thể tạo mới và cộng điểm
            // cho nó
            customerPoint = new CustomerPoint();
            customerPoint.setCustomer(customer);
            customerPoint.setEvent(event);
            customerPoint.setPoint(itemReturn.getTotalPoint());
        }

        customerPointRepo.save(customerPoint);
        return itemReturn;
    }

    private void processListItemOCR(List<String> lines, String line, List<Item> items, ItemReturn itemReturn,
            String endListItem, String endListItem2, Boolean isListItem, Integer eventId, int totalPoint,
            Locale currentLocale) {
        List<String> subLines = lines.subList(lines.indexOf(line), lines.size());

        System.out.println(subLines);
        ResourceBundle keyWord = ResourceBundle.getBundle("keyWord", currentLocale);
        String cafeSua_Fake = keyWord.getString("keyCafeSua_fake");
        String cafeSua = keyWord.getString("keyCafeSua");
        String cafeDen_fake = keyWord.getString("keyCafeDen_fake");
        String cafeDen = keyWord.getString("keyCafeDen");
        String BeafSteak_fake = keyWord.getString("keyBeefStake_fake");
        String BeafSteak = keyWord.getString("keyBeefStake");
        String VietVangPure_fake = keyWord.getString("keyVietVangPure_fake");
        String VietVangPure = keyWord.getString("keyVietVangPure");
        String GoldenVietVang_fake = keyWord.getString("keyGoldenVietVang_fake");
        String GoldenVietVang = keyWord.getString("keyGoldenVietVang");
        String VietCoffee_fake = keyWord.getString("keyVietCoffee_fake");
        String VietCoffee = keyWord.getString("keyVietCoffee");
        String Soul_fake = keyWord.getString("keySoul_fake");
        String Soul = keyWord.getString("keySoul");
        String ComboPremium_fake = keyWord.getString("keyComboPremium_fake");
        String ComboPremium = keyWord.getString("keyComboPremium");
        // Store in hashmap
        Map<String, String> replacements = new HashMap<>();
        replacements.put(cafeSua_Fake, cafeSua);
        replacements.put(cafeDen_fake, cafeDen);
        replacements.put(ComboPremium_fake, ComboPremium);
        replacements.put(BeafSteak_fake, BeafSteak);
        replacements.put(GoldenVietVang_fake, GoldenVietVang);
        replacements.put(Soul_fake, Soul);
        replacements.put(VietCoffee_fake, VietCoffee);
        replacements.put(VietVangPure_fake, VietVangPure);
        for (int i = subLines.indexOf(line); i < subLines.size(); i += 2) {
            String currentLine = subLines.get(i);

            if (currentLine.contains(endListItem) || currentLine.contains(endListItem2)) {
                isListItem = false;// change flag
                System.out.println(currentLine);
                break;
            }
            if (isListItem) {
                String name = subLines.get(i);
                System.out.println("name1:  " + name);
                int quantity = Integer.parseInt(subLines.get(i + 1).split(" ")[0]);

                if (replacements.containsKey(name)) {
                    name = replacements.get(name);
                }
                System.out.println("name2:  " + name);
                Item item = new Item(name.trim(), quantity);
                System.out.println("item: " + item);
                String productName = item.getName();
                System.out.println("product " + productName);
                Optional<Integer> point = productEventRepo.getPointByProductName(productName, eventId);

                if (point.isPresent()) {
                    System.out.println("Point " + point.get());
                    totalPoint += point.get(); // Thêm điểm vào totalPoint nếu point có giá trị
                    item = new Item(name.trim(), quantity);
                    items.add(item);
                    itemReturn.setItems(items);
                    itemReturn.setTotalPoint(totalPoint);

                }
            }
        }
    }

    @Override
    public Object ScanResultOCR(String result, Locale currentLocale, Integer eventId, Integer customerId) {
        Bill bill = new Bill();
        String billsaveCode = "";
        LocalDate billsaveDate = LocalDate.now();
        ResourceBundle keyWord = ResourceBundle.getBundle("keyWord", currentLocale);
        // String biling date
        String billingDate = keyWord.getString("keyBillingDate");
        // String billing No
        String startBillNo = keyWord.getString("keyStartBillNo");
        String startBillNo2 = keyWord.getString("keyStartBillNo2");
        String startBillNo3 = keyWord.getString("keyStartBillNo3");
        // String start List Item
        String startListItem = keyWord.getString("keyStartListItem");
        String startListItem1 = keyWord.getString("keyStartListItem1");
        // String end List Item
        String endListItem = keyWord.getString("keyEndListItem");// ="Cong tién hang"
        String endListItem2 = keyWord.getString("keyEndListItem2");
        String endListItem3 = keyWord.getString("keyEndListItem3");// Tổng tiền đơn hàng

        // Device into Lines (list)
        List<String> lines = Arrays.asList(result.split("\n"));
        List<Item> items = new ArrayList<>();
        ItemReturn itemReturn = new ItemReturn();
        boolean isListItem = false;
        boolean isBillNo = false;

        int totalPoint = 0;

        System.out.println(startListItem);
        // System.out.println("Scanning");
        // create a loop to Approve each line
        for (String line : lines) {
            if (line.equals(billingDate)) {
                System.out.println(billingDate);
                continue;
            }
            if (line.equals(startBillNo) || line.equals(startBillNo2) || line.equals(startBillNo3)) {
                isBillNo = true;
                continue;
            }
            if (isBillNo) {
                bill = billServiceImpl.findBillByCode(line);
                System.out.println("bill->>" + bill);
                if (bill != null) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("This bill already Used! ");
                }
                billsaveDate = LocalDate.now();
                System.out.println("code Line =>> " + line);
                billsaveCode = line;

                isBillNo = false;
                continue;
            }
            if (line.contains(startListItem) || line.contains(startListItem1)) {
                isListItem = true;
                continue;
            }
            if (isListItem) {
                System.out.println("Process bill 2 ");
                // found list product in bill and begin to read infomation each product to get
                // list item
                processListItem(lines, line, items, itemReturn, endListItem, endListItem2, endListItem3, isListItem,
                        eventId, totalPoint, currentLocale);
                break;
            }

        }
        if (items.isEmpty()) {

            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Item List is empty , please check your recive! ");

        }
        System.out.println("->>DAte " + billsaveDate);
        System.out.println("->> Code " + billsaveCode);
        Bill billSave = new Bill();
        billSave.setBillCode(billsaveCode);
        // System.out.println("->> Code "+ bill.getBillCode());
        billSave.setScanDate(billsaveDate);
        // System.out.println("->> Date "+ bill.getScanDate());
        billServiceImpl.savebill(billSave);

        Optional<CustomerPoint> customerPointOptional = customerPointRepo.findByCustomerAndEvent(customerId, eventId);
        CustomerPoint customerPoint;
        if (customerPointOptional.isPresent()) {
            customerPoint = customerPointOptional.get();
            int currentTotalPoint = customerPoint.getPoint() + itemReturn.getTotalPoint();
            customerPoint.setPoint(currentTotalPoint);
        } else {
            Customer customer = customerRepo.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "CustomerId", customerId));
            Event event = eventRepo.findById(3).orElseThrow(() -> new ResourceNotFoundException("Event", "EventId", 3));
            // Nếu không tìm thấy đối tượng CustomerPoint, bạn có thể tạo mới và cộng điểm
            // cho nó
            customerPoint = new CustomerPoint();
            customerPoint.setCustomer(customer);
            customerPoint.setEvent(event);
            customerPoint.setPoint(itemReturn.getTotalPoint());
        }

        customerPointRepo.save(customerPoint);
        return itemReturn;
    }

    private void processListItem(List<String> lines, String line, List<Item> items, ItemReturn itemReturn,
            String endListItem, String endListItem2, String endListItem3, Boolean isListItem, Integer eventId,
            int totalPoint, Locale currentLocale) {
        List<String> subLines = lines.subList(lines.indexOf(line), lines.size());
        System.out.println(subLines);
        ResourceBundle keyWord = ResourceBundle.getBundle("keyWordOg", currentLocale);
        // ResourceBundle keyWord_JP = ResourceBundle.getBundle("keyWord",
        // currentLocale);
        // key change if wrong text value
        String cafeSua_Fake = keyWord.getString("keyCafeSua_fake");
        String cafeSua = keyWord.getString("keyCafeSua");
        String cafeDen_fake = keyWord.getString("keyCafeDen_fake");
        String cafeDen = keyWord.getString("keyCafeDen");
        String BeafSteak_fake = keyWord.getString("keyBeefStake_fake");
        String BeafSteak = keyWord.getString("keyBeefStake");
        String VietVangPure_fake = keyWord.getString("keyVietVangPure_fake");
        String VietVangPure = keyWord.getString("keyVietVangPure");
        String GoldenVietVang_fake = keyWord.getString("keyGoldenVietVang_fake");
        String GoldenVietVang = keyWord.getString("keyGoldenVietVang");
        String VietCoffee_fake = keyWord.getString("keyVietCoffee_fake");
        String VietCoffee = keyWord.getString("keyVietCoffee");
        String Soul_fake = keyWord.getString("keySoul_fake");
        String Soul = keyWord.getString("keySoul");
        String ComboPremium_fake = keyWord.getString("keyComboPremium_fake");
        String ComboPremium = keyWord.getString("keyComboPremium");
        String VietVangShare_fake = keyWord.getString("keyVietVangShare_fake");
        String VietVangShare = keyWord.getString("keyVietVangShare");
        String VietVangTalentshow_fake = keyWord.getString("keyVietVangTalentshow_fake");
        String VietVangTalentshow = keyWord.getString("keyVietVangTalentshow");
        // currency
        String currency = keyWord.getString("keyVietNamCurrency");

        // Store in hashmap
        Map<String, String> replacements = new HashMap<>();
        replacements.put(cafeSua_Fake, cafeSua);
        replacements.put(cafeDen_fake, cafeDen);
        replacements.put(ComboPremium_fake, ComboPremium);
        replacements.put(BeafSteak_fake, BeafSteak);
        replacements.put(GoldenVietVang_fake, GoldenVietVang);
        replacements.put(Soul_fake, Soul);
        replacements.put(VietCoffee_fake, VietCoffee);
        replacements.put(VietVangPure_fake, VietVangPure);
        replacements.put(VietVangShare_fake, VietVangShare);
        replacements.put(VietVangTalentshow_fake, VietVangTalentshow);

        for (int i = subLines.indexOf(line); i < subLines.size(); i += 2) {
            String currentLine = subLines.get(i);
            String nextLine = subLines.get(i + 1);
            Item item = new Item("", 1);
            String beforeLine = currency;
            if (i != 0) {
                beforeLine = subLines.get(i - 1);
            }
            // boolean IsMenu = false;
            if (currentLine.contains(endListItem) || currentLine.contains(endListItem2)
                    || currentLine.contains(endListItem3) || beforeLine.contains(endListItem3)
                    || nextLine.contains(endListItem3)) {
                isListItem = false;// change flag
                System.out.println(currentLine);
                break;
            }
            if (isListItem) {
                item.setQuantity(1);
                if (isInteger(nextLine)) {
                    item.setName(currentLine);
                    item.setQuantity(Integer.parseInt(nextLine));
                    System.out.println("item: " + item);
                    String productName = item.getName();
                    System.out.println("product " + productName);
                    Optional<Integer> point = productEventRepo.getPointByProductName(productName, eventId);
                    System.out.println(">>Point: " + point);
                    if (point.isPresent()) {
                        System.out.println("Point " + point.get());
                        totalPoint = totalPoint + point.get() * item.getQuantity();
                        items.add(item);
                        itemReturn.setItems(items);
                        itemReturn.setTotalPoint(totalPoint);

                    }
                }
                // continue;

                if (isInteger(currentLine)) {
                    item.setName(beforeLine);
                    item.setQuantity(Integer.parseInt(currentLine));
                    System.out.println("item: " + item);
                    String productName = item.getName();
                    System.out.println("product " + productName);
                    Optional<Integer> point = productEventRepo.getPointByProductName(productName, eventId);
                    System.out.println(">>Point: " + point);
                    if (point.isPresent()) {
                        System.out.println("Point " + point.get());
                        totalPoint = totalPoint + point.get() * item.getQuantity(); // Thêm điểm vào totalPoint nếu
                                                                                    // point có giá trị
                        items.add(item);
                        itemReturn.setItems(items);
                        itemReturn.setTotalPoint(totalPoint);

                    }
                }
            }

        }
    }

    @Override
    public Object ScanResultOCRMapping(List<Line> linesList, Locale currentLocale, Integer eventId,
            Integer customerId) {
        ResourceBundle keyWord = ResourceBundle.getBundle("keyWord", currentLocale);
        String billsaveCode = "";
        LocalDate billsaveDate = LocalDate.now();
        // String biling date
        String billingDate = keyWord.getString("keyBillingDate");
        // String billing No
        String startBillNo = keyWord.getString("keyStartBillNo");
        String startBillNo2 = keyWord.getString("keyStartBillNo2");
        String startBillNo3 = keyWord.getString("keyStartBillNo3");
        // // String start List Item
        // String startListItem = keyWord.getString("keyStartListItem");
        // String startListItem1 = keyWord.getString("keyStartListItem1");
        // //String end List Item
        // String endListItem = keyWord.getString("keyEndListItem");//="Cong tién hang"
        // String endListItem2= keyWord.getString("keyEndListItem2");
        // String endListItem3= keyWord.getString("keyEndListItem3");//Tổng tiền đơn
        // hàng
        // Bill
        Bill bill = new Bill();
        int totalPoint = 0;
        List<ItemPoint> items = new ArrayList<>();
        ItemPointReturn itemReturn = new ItemPointReturn();
        if (linesList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Item List is empty , please check your recive! ");
        }
        DateConverterDTO convert = new DateConverterDTO();

        for (int i = 0; i < linesList.size(); i += 2) {
            String currentWord = linesList.get(i).getLineText();

            System.out.println("CurrentText: " + currentWord);
            String nextWord = "";
            while (i < linesList.size()) {
                nextWord = linesList.get(i + 1).getLineText();
            }

            if (currentWord.contains(billingDate)) {
                billsaveDate = convert.convertToDate(currentWord);

                if (billsaveDate != null) {
                    bill.setScanDate(billsaveDate);

                } else if (nextWord.contains(billingDate)) {
                    billsaveDate = convert.convertToDate(nextWord);

                    if (billsaveDate != null) {
                        bill.setScanDate(billsaveDate);
                    }
                }
            }

            if (currentWord.equals(startBillNo) || currentWord.equals(startBillNo2)
                    || currentWord.equals(startBillNo3)) {
                billNoProcess(currentWord, bill);
                continue;
            }
            if (nextWord.equals(startBillNo) || nextWord.equals(startBillNo2) || nextWord.equals(startBillNo3)) {
                billNoProcess(currentWord, bill);
                continue;
            }

            if (isInteger(currentWord)) {
                String beforeWord = linesList.get(i - 1).getLineText();
                ItemPoint item = CheckIfProductName(beforeWord, currentWord, eventId);
                if (item != null) {
                    items.add(item);
                    totalPoint += item.getPoint().orElse(1) * item.getQuantity();
                }
                continue;
            } else {
                ItemPoint item = CheckIfProductName(currentWord, nextWord, eventId);
                if (item != null) {
                    items.add(item);
                    totalPoint += item.getPoint().orElse(1) * item.getQuantity();
                }
            }
            System.out.println("->>DAte " + billsaveDate);
            System.out.println("->> Code " + billsaveCode);
            Bill billSave = new Bill();
            billSave.setBillCode(billsaveCode);
            // System.out.println("->> Code "+ bill.getBillCode());
            billSave.setScanDate(billsaveDate);
            // System.out.println("->> Date "+ bill.getScanDate());
            billServiceImpl.savebill(billSave);
        }

        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Item List is empty , please check your recive! ");
        } else {
            itemReturn.setItems(items);
            itemReturn.setTotalPoint(totalPoint);

            return ResponseEntity.ok(itemReturn);
        }

    }

    public Object billNoProcess(String codebill, Bill bill) {
        if (bill.getBillCode() != null) {
            bill = billServiceImpl.findBillByCode(codebill);
            logger.info("bill->>" + bill);
            System.out.println("bill->>" + bill);
            if (bill != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("This bill already Used! ");
            }
        }
        return bill;
    }

    public ItemPoint CheckIfProductName(String currentWord, String nextWord, Integer eventId) {
        if (!isInteger(currentWord)) {
            System.out.println("EVENT >> " + eventId);
            Optional<Integer> point = productEventRepo.getPointByProductName(currentWord, eventId);
            // Optional<Integer> point = productEventRepo.getPointByProductName(productName,
            // eventId);
            System.out.println("Point >> " + point);
            ItemPoint item = new ItemPoint();
            if (point.isPresent()) {
                System.out.println("Point " + point.get());
                item.setName(currentWord);
                item.setQuantity(1);
                item.setPoint(point);
                return item;
            }
        }
        return null;
    }

    /**
     * methob use to check if input is an integer or not
     * 
     * @param String
     * @return true if it's an Integer || false if It's not
     */
    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * methob use to check if input is a double or not
     * 
     * @param String
     * @return true if it's a Double || false if It's not
     */
    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str.replace(",", ""));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public ReplacementResult keyexchange(String text, Locale currentLocale) {
        ResourceBundle keyWord = ResourceBundle.getBundle("keyWordOg", currentLocale);
        // ResourceBundle keyWord_JP = ResourceBundle.getBundle("keyWord",
        // currentLocale);
        // key change if wrong text value
        String cafeSua_Fake = keyWord.getString("keyCafeSua_fake");
        String cafeSua = keyWord.getString("keyCafeSua");
        String cafeDen_fake = keyWord.getString("keyCafeDen_fake");
        String cafeDen = keyWord.getString("keyCafeDen");
        String BeafSteak_fake = keyWord.getString("keyBeefStake_fake");
        String BeafSteak = keyWord.getString("keyBeefStake");
        String VietVangPure_fake = keyWord.getString("keyVietVangPure_fake");
        String VietVangPure = keyWord.getString("keyVietVangPure");
        String GoldenVietVang_fake = keyWord.getString("keyGoldenVietVang_fake");
        String GoldenVietVang = keyWord.getString("keyGoldenVietVang");
        String VietCoffee_fake = keyWord.getString("keyVietCoffee_fake");
        String VietCoffee = keyWord.getString("keyVietCoffee");
        String Soul_fake = keyWord.getString("keySoul_fake");
        String Soul = keyWord.getString("keySoul");
        String ComboPremium_fake = keyWord.getString("keyComboPremium_fake");
        String ComboPremium = keyWord.getString("keyComboPremium");
        String VietVangShare_fake = keyWord.getString("keyVietVangShare_fake");
        String VietVangShare = keyWord.getString("keyVietVangShare");
        String VietVangTalentshow_fake = keyWord.getString("keyVietVangTalentshow_fake");
        String VietVangTalentshow = keyWord.getString("keyVietVangTalentshow");
        // currency
        String currency = keyWord.getString("keyVietNamCurrency");

        Map<String, String> replacements = new HashMap<>();
        replacements.put(cafeSua_Fake, cafeSua);
        replacements.put(cafeDen_fake, cafeDen);
        replacements.put(ComboPremium_fake, ComboPremium);
        replacements.put(BeafSteak_fake, BeafSteak);
        replacements.put(GoldenVietVang_fake, GoldenVietVang);
        replacements.put(Soul_fake, Soul);
        replacements.put(VietCoffee_fake, VietCoffee);
        replacements.put(VietVangPure_fake, VietVangPure);
        replacements.put(VietVangShare_fake, VietVangShare);
        replacements.put(VietVangTalentshow_fake, VietVangTalentshow);
        boolean replacementOccurred = false;

        // Iterate through the map and replace keys in the text
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            if (text.contains(entry.getKey())) {
                text = text.replace(entry.getKey(), entry.getValue());
                replacementOccurred = true;
            }
        }

        return new ReplacementResult(text, replacementOccurred);
    }

    public ProcessingState whatToDo(String text, Locale currentLocale) {
        ResourceBundle keyWord = ResourceBundle.getBundle("keyWord", currentLocale);

        logger.info("keyWord: >>>" + keyWord);
        // Check for billing date
        if (text.contains(keyWord.getString("keyBillingDate"))) {
            return ProcessingState.BILLING_DATE;
        }

        // Check for start bill numbers
        if (text.contains(keyWord.getString("keyStartBillNo")) ||
                text.contains(keyWord.getString("keyStartBillNo2")) ||
                text.contains(keyWord.getString("keyStartBillNo3"))) {
            return ProcessingState.BILLING_NO;
        }

        // Check for start list items
        if (text.contains(keyWord.getString("keyStartListItem")) ||
                text.contains(keyWord.getString("keyStartListItem1"))) {
            return ProcessingState.LIST_ITEM;
        }

        if (text.contains(keyWord.getString("keyEndListItem")) ||
                text.contains(keyWord.getString("keyEndListItem2")) ||
                text.contains(keyWord.getString("keyEndListItem3"))) {
            return ProcessingState.LIST_END;
        }

        return ProcessingState.NONE;

    }

    public enum ProcessingState {
        NONE, BILLING_DATE, BILLING_NO, LIST_ITEM, LIST_END
    }

    @Override
    public Object ProcessingLine(List<Line> lines, Locale currentLocale, Integer customerId, Integer eventId) {
        Bill bill = new Bill();
        LocalDate scannedDate = LocalDate.now();
        DateConverterDTO dateConverter = new DateConverterDTO();
        Boolean isListItem = false;
        int totalPoint = 0;
        List<ItemPoint> items = new ArrayList<>();
        ItemPointReturn itemReturn = new ItemPointReturn();
        ItemPoint item = new ItemPoint();
        ProcessingState currentState = ProcessingState.NONE;
        for (Line line : lines) {
            currentState = whatToDo(line.getLineText(), currentLocale);
            switch (currentState) {
                case BILLING_DATE:
                    scannedDate = dateConverter.convertToDate(line.getLineText());
                    if (scannedDate != null) {
                        bill.setScanDate(scannedDate);
                    } else {
                        System.err.println("Error Cover Scandate !");
                    }
                    break;
                case BILLING_NO:
                    bill.setBillCode(line.getLineText());
                    billNoProcess(bill.getBillCode(), bill);
                    break;
                case LIST_ITEM:
                    isListItem = true;
                    break;
                case LIST_END:
                    isListItem = false;
                case NONE:
                    break;
            }
            if (isListItem) {
                ReplacementResult result = keyexchange(line.getLineText(), currentLocale);
                if (result.isReplacementOccurred()) {
                    item.setName(line.getLineText());
                    item = CheckIfProductName(line.getLineText(), line.getLineText(), eventId);
                    if (item != null) {
                        items.add(item);
                        totalPoint += item.getPoint().orElse(1) * item.getQuantity();
                    }
                    items.add(item);
                }
            }
            if (items.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("Item List is empty , please check your recive! ");
            } else {

                itemReturn.setItems(items);
                itemReturn.setTotalPoint(totalPoint);

                return ResponseEntity.ok(itemReturn);
            }

        }
        billServiceImpl.savebill(bill);
        throw new UnsupportedOperationException("Unimplemented method 'ProcessingLine'");
    }

    public ResponseEntity<ItemPointReturn> processFile(MultipartFile file, String json, Locale currentLocale,
            Integer customerId, Integer eventId) {
        System.out.println("Processing data !");
        Bill bill = new Bill();
        LocalDate scannedDate = LocalDate.now();
        Boolean isListItem = false;
        int totalPoint = 0;
        List<ItemPoint> items = new ArrayList<>();
        ItemPointReturn itemReturn = new ItemPointReturn();
        DateConverterDTO dateConverter = new DateConverterDTO();

        if (json == null || json.isEmpty()) {
            System.out.println("Invalid JSON!");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ParsedResult parsedResult = objectMapper.readValue(json, ParsedResult.class);
            List<Line> lineTextList = parsedResult.getParsedResults().get(0).getTextOverlay().getLines();
            // for (Line line : lineTextList) {
            for (int i = 0; i < lineTextList.size(); i++) {
                Line line = lineTextList.get(i);
                // List<Word> words = line.getWords();
                ProcessingState currentState = ProcessingState.NONE;
                currentState = whatToDo(line.getLineText(), currentLocale);
                switch (currentState) {

                    case BILLING_DATE:
                        scannedDate = dateConverter.convertToDate(line.getLineText());
                        if (scannedDate != null) {
                            bill.setScanDate(scannedDate);
                        } else {
                            System.err.println("Error Cover Scandate!");
                        }
                        break;
                    case BILLING_NO:
                        billNoProcess(line.getLineText(), bill);
                        bill.setBillCode(line.getLineText());
                        bill.setScanDate(scannedDate);
                        bill.setStatus(EnumManager.Billtatus.APPROVE);

                        break;
                    case LIST_ITEM:
                        isListItem = true;
                        break;
                    case NONE:
                        break;
                }
                if (isListItem) {
                    System.out.println(line.getLineText().trim());
                    ItemPoint item = CheckIfProductName(line.getLineText().trim(), line.getLineText(), eventId);
                    if (item != null) {
                        items.add(item);
                        totalPoint += item.getPoint().orElse(1) * item.getQuantity();
                    }
                }
            }

            if (items.isEmpty()) {
                saveBillImage(file, eventId, customerId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            } else {
                itemReturn.setItems(items);
                itemReturn.setTotalPoint(totalPoint);
                Optional<CustomerPoint> customerPointOptional = customerPointRepo.findByCustomerAndEvent(customerId,
                        eventId);
                CustomerPoint customerPoint;
                if (customerPointOptional.isPresent()) {
                    customerPoint = customerPointOptional.get();
                    int currentTotalPoint = customerPoint.getPoint() + itemReturn.getTotalPoint();
                    customerPoint.setPoint(currentTotalPoint);
                } else {
                    Customer customer = customerRepo.findById(customerId)
                            .orElseThrow(() -> new ResourceNotFoundException("Customer", "CustomerId", customerId));
                    Event event = eventRepo.findById(3)
                            .orElseThrow(() -> new ResourceNotFoundException("Event", "EventId", 3));
                    // Nếu không tìm thấy đối tượng CustomerPoint, bạn có thể tạo mới và cộng điểm
                    // cho nó
                    customerPoint = new CustomerPoint();
                    customerPoint.setCustomer(customer);
                    customerPoint.setEvent(event);
                    customerPoint.setPoint(itemReturn.getTotalPoint());
                }

                customerPointRepo.save(customerPoint);
                bill.setPoint(itemReturn.getTotalPoint());
                bill.setCustomerId(customerId);
                bill.setEventId(eventId);
                bill.setEventName(eventRepo.findById(eventId).get().getName());
                billServiceImpl.savebill(bill);

                return ResponseEntity.ok(itemReturn);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // for (Word word : words) {}
    }

    public void saveBillImage(MultipartFile file, int eventId, int customerId) {
        try {
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
            bill.setEventName(eventRepo.findById(eventId).get().getName());
            billRepo.save(bill);
            Optional<CustomerPoint> customerPointOptional = customerPointRepo.findByCustomerAndEvent(customerId,
                    eventId);
            Customer customer = customerRepo.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "CustomerId", customerId));
            Event event = eventRepo.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event", "EventId", eventId));
            if (customerPointOptional.isEmpty()) {
                CustomerPoint cus = new CustomerPoint();
                cus.setEvent(event);
                cus.setCustomer(customer);
                cus.setPoint(0);
                customerPointRepo.save(cus);
            }

        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception as needed
        }
    }

}