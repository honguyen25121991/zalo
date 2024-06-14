package com.zalo.Spring_Zalo.Controller;

import com.zalo.Spring_Zalo.Entities.Company;
import com.zalo.Spring_Zalo.Entities.Event;
import com.zalo.Spring_Zalo.Entities.Product;
import com.zalo.Spring_Zalo.Entities.ProductEvent;
import com.zalo.Spring_Zalo.Repo.CompanyRepo;
import com.zalo.Spring_Zalo.Repo.EventRepo;
import com.zalo.Spring_Zalo.Repo.ProductEventRepo;
import com.zalo.Spring_Zalo.Repo.ProductRepo;
import com.zalo.Spring_Zalo.Response.ApiResponse;
import com.zalo.Spring_Zalo.ServiceImpl.ProductEventServiceImpl;
import com.zalo.Spring_Zalo.request.ProductEventRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/admin/productEvent")
public class ProductEventController {
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private EventRepo eventRepo;
    @Autowired
    private ProductEventRepo productEventRepo;
    @Autowired
    private CompanyRepo companyRepo;
    @Autowired
    private ProductEventServiceImpl service;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Product>> getListProductEvent(@PathVariable("eventId") Integer eventId) {
        List<ProductEvent> eventList = productEventRepo.findAllByEventId(eventId);
        if (eventList == null || eventList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Product> productList = new ArrayList<>();
        for (ProductEvent productEvent : eventList) {
            Optional<Product> productTemp = productRepo.findById(productEvent.getProduct().getId());
            if (productTemp.isPresent()) { // Kiểm tra xem Optional có giá trị không
                productList.add(productTemp.get()); // Lấy giá trị từ Optional bằng get()
            }
        }

        return ResponseEntity.ok().body(productList);
    }

    // @PostMapping("/company/{companyId}")
    // public ResponseEntity<ProductEvent> createEvent(@RequestBody ProductEvent
    // eventDetails) {
    // System.out.println(eventDetails);
    // if (eventDetails.getEvent() == null) {
    // System.out.println("Events info is empty");
    // return ResponseEntity.badRequest().build();
    // }
    // if (eventDetails.getProduct() == null) {
    // System.out.println("product info is empty");
    // return ResponseEntity.badRequest().build();
    // }
    // Event event = new Event();
    // Product product = new Product();
    // event = eventDetails.getEvent();
    // product = eventDetails.getProduct();

    // // Save the Event and Product entities
    // eventRepo.save(event);
    // productRepo.save(product);

    // // Set the Event and Product in the ProductEvent
    // eventDetails.setEvent(event);
    // eventDetails.setProduct(product);

    // // Save the ProductEvent
    // ProductEvent savedProductEvent = productEventRepo.save(eventDetails);

    // if (savedProductEvent != null) {
    // return ResponseEntity.ok(savedProductEvent);
    // } else {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    // }
    // }

    @PostMapping("/")
    public ResponseEntity<Object> createProductEvent(@RequestBody ProductEventRequest productEventRequest) {
        Event event = productEventRequest.getEvent();
        List<Product> productList = productEventRequest.getProductList();

        if (event == null) {
            ApiResponse response = new ApiResponse("Không có thông tin Sự kiện", false, 400);
            return ResponseEntity.badRequest().body(response);
        }

        Company company = event.getCompany();
        if (company == null) {
            ApiResponse response = new ApiResponse("Không có thông tin công ty", false, 400);
            return ResponseEntity.badRequest().body(response);
        }

        Optional<Company> existingCompany = companyRepo.findById(company.getId());
        if (existingCompany.isEmpty()) {
            companyRepo.saveAndFlush(company);
        }

        Optional<Event> existingEvent = eventRepo.findById(event.getId());
        if (existingEvent.isEmpty()) {
            eventRepo.saveAndFlush(event);
        }

        if (productList.isEmpty()) {
            ApiResponse response = new ApiResponse("Không có thông tin Sản phẩm", false, 400);
            return ResponseEntity.badRequest().body(response);
        }

        for (Product product : productList) {
            Optional<Product> existingProduct = productRepo.findById(product.getId());
            if (existingProduct.isEmpty()) {
                productRepo.saveAndFlush(product);
            }

            ProductEvent productEvent = new ProductEvent();
            productEvent.setEvent(event);
            productEvent.setProduct(product);
            productEventRepo.save(productEvent);
        }

        return ResponseEntity.ok("ProductEvent created successfully.");
    }

}
