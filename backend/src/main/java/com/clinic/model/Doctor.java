package com.clinic.model;

import java.util.List;

/** Clinic doctor with daily office hours (HH:mm format). */
public record Doctor(
        Long id,
        String name,
        String specialty,
        List<String> schedule) {
}
