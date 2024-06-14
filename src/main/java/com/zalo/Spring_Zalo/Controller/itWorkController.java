package com.zalo.Spring_Zalo.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@CrossOrigin("*")
@RequestMapping("/api")
public class itWorkController {
    Logger logger = LoggerFactory.getLogger(itWorkController.class);

    @GetMapping("/work")
    public String getResponse() {
        System.out.println("ping success");
        return "It works!";
    }

    @GetMapping("/work2")
    public String getMethodName(@RequestParam String param) {
        System.out.println("param: " + param);
        System.out.println("ping success");
        return "It works! " + param;
    }

    // body
    @GetMapping("/work3")
    public String getMethodBody(@RequestBody String param) {
        System.out.println("param: " + param);
        System.out.println("ping success");
        return "It works! " + param;
    }

}
