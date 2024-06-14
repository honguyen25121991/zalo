package com.zalo.Spring_Zalo.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zalo.Spring_Zalo.Entities.Bill;
import com.zalo.Spring_Zalo.Entities.CustomerPoint;
import com.zalo.Spring_Zalo.Entities.EnumManager;
import com.zalo.Spring_Zalo.Entities.User;
import com.zalo.Spring_Zalo.Repo.BillRepo;
import com.zalo.Spring_Zalo.Repo.CustomerPointRepo;
import com.zalo.Spring_Zalo.Repo.CustomerRepo;
import com.zalo.Spring_Zalo.Repo.EventRepo;
import com.zalo.Spring_Zalo.request.BillRequest;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/bill")
public class BillController {
    @Autowired
    private BillRepo billRepo;
    @Autowired
    private CustomerRepo customerRepo;
    @Autowired
    private EventRepo eventRepo;
    @Autowired
    private CustomerPointRepo customerPointRepo;

    @GetMapping("/")
    public ResponseEntity<Page<Bill>> getAllBill(@RequestParam(defaultValue = "1000") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Page<Bill> bill = billRepo.findAll(pageable);

        if (bill.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(bill);
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<String> disableBill(@PathVariable("id") Long id) {
        // Gọi service để thực hiện vô hiệu hóa hóa đơn với id nhận được
        // billService.disableBill(id);
        Optional<Bill> billdata = billRepo.findById(id.intValue());
        if (billdata.isPresent()) {
            Bill bill = billdata.get();
            bill.setStatus(EnumManager.Billtatus.DISABLE);
            billRepo.save(bill);
        } else {
            return new ResponseEntity<>("Bill not found", HttpStatus.NOT_FOUND); // Trả về thông báo lỗi nếu không tìm
                                                                                 // thấy hóa đơn với id nhận được
        }

        return new ResponseEntity<>("Bill disabled successfully", HttpStatus.OK); // Trả về thông báo thành công nếu cập
                                                                                  // nhật thành công
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateBill(@PathVariable("id") int id, @RequestBody Bill updatedBill) {
        // Tìm hóa đơn trong cơ sở dữ liệu
        Bill existingBill = billRepo.findById(id).orElse(null);

        // Nếu không tìm thấy hóa đơn, trả về lỗi 404 Not Found
        if (existingBill == null) {
            return new ResponseEntity<>("Hóa đơn không tồn tại", HttpStatus.NOT_FOUND);
        }
        Optional<CustomerPoint> customerPoint = customerPointRepo.findByCustomerAndEvent(existingBill.getCustomerId(),
                existingBill.getEventId());
        if (customerPoint.isPresent()) {
            // Nếu có điểm khách hàng, thì cộng thêm vào điểm của hóa đơn
            // existingBill.setPoint( customerPoint.getPoint());
            CustomerPoint pointToUpdate = customerPoint.get();
            pointToUpdate.setPoint(pointToUpdate.getPoint() + existingBill.getPoint());
            // Lưu thông tin điểm của khách hàng vào cơ sở dữ liệu
            customerPointRepo.save(pointToUpdate);
        } else {
            CustomerPoint newPoint = new CustomerPoint();

            newPoint.setPoint(updatedBill.getPoint());
            newPoint.setCustomer(customerRepo.findById(updatedBill.getCustomerId()).orElse(null));
            newPoint.setEvent(eventRepo.findById(updatedBill.getEventId()).orElse(null));
            customerPointRepo.save(newPoint);
        }

        // Cập nhật thông tin hóa đơn với dữ liệu mới từ yêu cầu PUT
        existingBill.setStatus(updatedBill.getStatus());
        existingBill.setPoint(updatedBill.getPoint());

        // Lưu hóa đơn đã cập nhật vào cơ sở dữ liệu
        billRepo.save(existingBill);
        System.out.println("đã cập nhật hóa đơn! ");
        // Trả về phản hồi thành công
        return new ResponseEntity<>("Hóa đơn đã được cập nhật thành công", HttpStatus.OK);
    }
}