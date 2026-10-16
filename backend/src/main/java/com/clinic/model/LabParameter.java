package com.clinic.model;

/**
 * A measured value in a lab test. min or max can be null when the reference
 * range is open-ended (for example, total cholesterol below 200).
 */
public record LabParameter(
        String name,
        double value,
        String unit,
        Double min,
        Double max,
        boolean withinRange) {
}
