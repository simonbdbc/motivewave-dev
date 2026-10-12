package com.monitoralgo.admin.controller;

import org.springframework.web.bind.annotation.*;
import com.monitoralgo.core.service.DatabaseAdminService;
import com.monitoralgo.api.model.AdminActionResponse;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DatabaseAdminService adminService;

    public AdminController(DatabaseAdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/init-db")
    public AdminActionResponse initDb() {
        try {
            adminService.initializeDatabase();
            return new AdminActionResponse(true, "DB initialized");
        } catch (Exception e) {
            e.printStackTrace();
            return new AdminActionResponse(false, "ERROR initializing DB: " + e.getMessage());
        }
    }

    @PostMapping("/reset-db")
    public AdminActionResponse resetDb() {
        try {
            adminService.resetDatabaseAndSeed();
            return new AdminActionResponse(true, "DB reset and seeded");
        } catch (Exception e) {
            e.printStackTrace();
            return new AdminActionResponse(false, "ERROR resetting DB: " + e.getMessage());
        }
    }

    @PostMapping("/seed-data")
    public AdminActionResponse seedData() {
        try {
            adminService.seedData();
            return new AdminActionResponse(true, "Seeded data");
        } catch (Exception e) {
            e.printStackTrace();
            return new AdminActionResponse(false, "ERROR seeding data: " + e.getMessage());
        }
    }

    @PostMapping("/seed-patterns")
    public AdminActionResponse seedPatterns() {
        try {
            adminService.seedPatterns();
            return new AdminActionResponse(true, "Seeded patterns");
        } catch (Exception e) {
            e.printStackTrace();
            return new AdminActionResponse(false, "ERROR seeding patterns: " + e.getMessage());
        }
    }
}
