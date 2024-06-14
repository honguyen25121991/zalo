package com.zalo.Spring_Zalo.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.zalo.Spring_Zalo.Entities.EnumManager;
import com.zalo.Spring_Zalo.Entities.User;
import com.zalo.Spring_Zalo.Repo.UserRepo;
import com.zalo.Spring_Zalo.Response.UserInfoAPIRespone;
import com.zalo.Spring_Zalo.Response.UserInfoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/admin/accounts")
public class AccountController {
    @Autowired
    private UserRepo userRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    // @GetMapping("/")
    // public ResponseEntity<List<User>> getListAccount() {
    // List<User> list = userRepo.findAll();
    // if(list.isEmpty() ){
    // throw new ResponseStatusException(404, "No account found", null);
    // }
    // return ResponseEntity.ok(list);
    // }
    @GetMapping("/")
    public ResponseEntity<Page<User>> getAllAccount(@RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Page<User> usersWithRole = userRepo.findAllWithRole(pageable);
        System.out.println('>' + usersWithRole.getContent().toString());
        if (usersWithRole.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(usersWithRole);
    }

    @PutMapping("/update-account")
    public UserInfoAPIRespone postMethodName(@RequestBody User updateUser) {
        try {
            // User updateUser = mapper.readValue(entity, User.class);
            System.out.println(">> " + updateUser.isIs_active());

            if (updateUser != null && updateUser.getUsername() != null) {
                User existingUser = userRepo.findByUserName(updateUser.getUsername());

                if (existingUser != null) {
                    // Update only if the provided status is not null
                    if (updateUser.getStatus() != null) {
                        existingUser.setStatus(updateUser.getStatus());
                        userRepo.save(existingUser);
                        return new UserInfoAPIRespone("Account status updated successfully", true, 200, existingUser);
                    } else {
                        return new UserInfoAPIRespone("Invalid request body. Status cannot be null.", false, 403, null);
                    }
                } else {
                    return new UserInfoAPIRespone("User Not Found .", false, 404, null);
                }
            } else {
                return new UserInfoAPIRespone("Invalid request body. Status cannot be null.", false, 402, null);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request body", e);
        }

    }
}
