// src/main/java/com/monitoralgo/core/model/LogPattern.java
package com.monitoralgo.core.model;

/**
 * POJO représentant la table log_pattern.
 */
public class LogPattern {
    private Long id;
    private String pattern;
    private String message;
    private boolean active;

    // Constructeurs
    public LogPattern() {
    }

    public LogPattern(Long id, String pattern, String message, boolean active) {
        this.id = id;
        this.pattern = pattern;
        this.message = message;
        this.active = active;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
