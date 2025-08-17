package com.duediligence.documentanalyzer.model;

/**
 * Enumeration of supported analysis domains for document due diligence
 */
public enum Domain {
    FINANCE("finance"),
    HEALTHCARE("healthcare"),
    LEGAL("legal");

    private final String value;

    Domain(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Convert string value to Domain enum
     * @param value String representation of domain
     * @return Domain enum or null if not found
     */
    public static Domain fromValue(String value) {
        for (Domain domain : Domain.values()) {
            if (domain.value.equalsIgnoreCase(value)) {
                return domain;
            }
        }
        return null;
    }
}