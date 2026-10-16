package com.clinic;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClinicApplication {

    public static void main(String[] args) {
        // Colombia time zone, so "future date" checks behave the same locally and on Railway
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
        SpringApplication.run(ClinicApplication.class, args);
    }
}
