package com.zalo.Spring_Zalo.Controller;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zalo.Spring_Zalo.Entities.Customer;
import com.zalo.Spring_Zalo.Entities.Event;
import com.zalo.Spring_Zalo.Entities.Product;
import com.zalo.Spring_Zalo.Entities.ProductEvent;
import com.zalo.Spring_Zalo.Repo.ProductEventRepo;
import com.zalo.Spring_Zalo.Repo.ProductRepo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin("*")
public class ProductController {
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductEventRepo productEventRepo;

    @GetMapping("/")
    public ResponseEntity<Page<Product>> getAllProducts(@RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Page<Product> productsPage = productRepo.findAll(pageable);

        // List<Product> productList = productsPage.getContent();

        return new ResponseEntity<>(productsPage, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product updatedProduct) {
        // Kiểm tra xem sản phẩm có tồn tại trong cơ sở dữ liệu không
        if (productRepo.findById(id.intValue()) == null) {
            return ResponseEntity.notFound().build(); // Trả về mã lỗi 404 nếu không tìm thấy sản phẩm
        }

        updatedProduct.setId(id.intValue()); // Đảm bảo rằng ID của sản phẩm được cập nhật đúng
        Product savedProduct = productRepo.save(updatedProduct); // Cập nhật thông tin của sản phẩm

        return ResponseEntity.ok(savedProduct); // Trả về sản phẩm đã được cập nhật
    }

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        if (product == null) {
            throw new ResponseStatusException(403, "Tạo sản phẩm không thành công do không đầy đủ thông tin", null);
        }
        Product pro = productRepo.saveAndFlush(product);
        return new ResponseEntity<>(pro, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<Event> getProductEvent(@PathVariable Long id) {
        // Tìm kiếm sản phẩm trong cơ sở dữ liệu dựa trên ID
        Optional<ProductEvent> productOptional = productEventRepo.findByProductId(id.intValue());
        if (productOptional.isPresent()) {
            Event eventInfo = productOptional.get().getEvent();
            if (eventInfo != null) {
                return ResponseEntity.ok(eventInfo); // Trả về thông tin event nếu tồn tại
            } else {
                return ResponseEntity.notFound().build(); // Trả về mã lỗi 404 nếu không tìm thấy event
            }
        } else {
            return ResponseEntity.notFound().build(); // Trả về mã lỗi 404 nếu không tìm thấy sản phẩm
        }
    }

}
