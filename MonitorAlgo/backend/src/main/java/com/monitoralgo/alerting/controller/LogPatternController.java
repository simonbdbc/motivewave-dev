package com.monitoralgo.alerting.controller;

import com.monitoralgo.core.dao.LogPatternDao;
import com.monitoralgo.core.model.LogPattern;
import com.monitoralgo.api.model.LogPatternApiModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/alert-patterns")
public class LogPatternController {

    private final LogPatternDao dao = new LogPatternDao();

    @GetMapping
    public ResponseEntity<List<LogPatternApiModel>> getAll() {
        try {
            List<LogPatternApiModel> result = dao.findAll().stream()
                    .map(LogPatternApiModel::fromEntity)
                    .toList();
            return ResponseEntity.ok(result);
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<LogPatternApiModel> create(@RequestBody LogPatternApiModel model) {
        try {
            LogPattern entity = new LogPattern();
            entity.setPattern(model.pattern());
            entity.setMessage(model.message());
            entity.setActive(model.active());
            LogPattern saved = dao.save(entity);
            return ResponseEntity.ok(LogPatternApiModel.fromEntity(saved));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<LogPatternApiModel> update(@PathVariable Long id, @RequestBody LogPatternApiModel model) {
        try {
            LogPattern existing = dao.findById(id);
            if (existing == null)
                return ResponseEntity.notFound().build();
            existing.setPattern(model.pattern());
            existing.setMessage(model.message());
            existing.setActive(model.active());
            dao.save(existing);
            return ResponseEntity.ok(LogPatternApiModel.fromEntity(existing));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            if (!dao.existsById(id))
                return ResponseEntity.notFound().build();
            dao.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
