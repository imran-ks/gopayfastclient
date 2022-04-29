package com.kalsym.gopayfast;

import com.kalsym.gopayfast.services.GoPayFastClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GopayfastApplication {

    @Autowired
    static GoPayFastClient gpfClient;

    public static void main(String[] args) {
        SpringApplication.run(GopayfastApplication.class, args);
    }

}
