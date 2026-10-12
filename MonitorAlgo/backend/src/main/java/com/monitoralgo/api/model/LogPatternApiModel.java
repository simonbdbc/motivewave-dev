package com.monitoralgo.api.model;

import com.monitoralgo.core.model.LogPattern;

/**
 * API model exposé pour la ressource LogPattern.
 * Découplage avec la structure interne.
 */
public record LogPatternApiModel(
        Long id,
        String pattern,
        String message,
        boolean active) {
    public static LogPatternApiModel fromEntity(LogPattern entity) {
        if (entity == null)
            return null;
        return new LogPatternApiModel(
                entity.getId(),
                entity.getPattern(),
                entity.getMessage(),
                entity.isActive());
    }
}
