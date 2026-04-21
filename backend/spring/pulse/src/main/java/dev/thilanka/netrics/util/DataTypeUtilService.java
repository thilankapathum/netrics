package dev.thilanka.netrics.util;

import dev.thilanka.netrics.common.exception.BusinessValidationException;
import org.springframework.stereotype.Service;

@Service
public class DataTypeUtilService {
    public Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new BusinessValidationException("Invalid number format: " + value);
        }
    }

    public Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BusinessValidationException("Invalid number format: " + value);
        }
    }

    public boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            throw new BusinessValidationException("Invalid boolean value: " + value);
        }
    }

    public String normalizeString(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    public String[] splitSectorName(String sectorName) {
        String[] parts = sectorName.split("__");

        if (parts.length == 2) {
            return parts;
        } else throw new BusinessValidationException("Invalid sector name: " + sectorName);
    }
}
