package com.zalo.Spring_Zalo.Controller;

import com.zalo.Spring_Zalo.Entities.Company;
import com.zalo.Spring_Zalo.Repo.CompanyRepo;
import com.zalo.Spring_Zalo.Service.CompanyService;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/companys")
public class CompanyController {
    @Autowired
    private CompanyService companyService;
    @Autowired
    private CompanyRepo companyRepo;

    @PostMapping("/")
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        Company createCompay = companyService.creeateCompany(company);
        return new ResponseEntity<>(createCompay, HttpStatus.CREATED);
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<Company> updateCompany(@RequestBody Company company, @PathVariable Integer companyId) {
        Company updateCompany = companyService.updateCompany(company, companyId);
        return new ResponseEntity<>(updateCompany, HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<Page<Company>> getListcompanyWithPagination(@RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Page<Company> companiesPage = companyRepo.findAll(pageable);

        return ResponseEntity.ok(companiesPage);
    }

}
