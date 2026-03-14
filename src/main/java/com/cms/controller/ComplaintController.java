package com.cms.controller;

import com.cms.model.Complaint;
import com.cms.model.Complaint.Category;
import com.cms.model.Complaint.Priority;
import com.cms.model.Complaint.Status;
import com.cms.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "http://localhost:5173")
public class ComplaintController {

    private final ComplaintService complaintService;

    @Autowired
    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    // ─── POST /api/complaints ──────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createComplaint(@Valid @RequestBody Complaint complaint) {
        try {
            Complaint created = complaintService.createComplaint(complaint);
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse("Complaint created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    // ─── GET /api/complaints ───────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAllComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<Complaint> complaints = complaintService.getAllComplaints(page, size, sortBy, sortDir);
            return ResponseEntity.ok(pagedResponse(complaints));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorResponse(e.getMessage()));
        }
    }

    // ─── GET /api/complaints/{id} ──────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getComplaintById(@PathVariable Long id) {
        return complaintService.getComplaintById(id)
                .map(c -> ResponseEntity.ok(successResponse("Complaint found", c)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(errorResponse("Complaint not found with id: " + id)));
    }

    // ─── GET /api/complaints/status/{status} ───────────────────────────────────
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(
            @PathVariable Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Complaint> result = complaintService.getComplaintsByStatus(status, page, size);
            return ResponseEntity.ok(pagedResponse(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    // ─── GET /api/complaints/category/{category} ───────────────────────────────
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getByCategory(
            @PathVariable Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Complaint> result = complaintService.getComplaintsByCategory(category, page, size);
            return ResponseEntity.ok(pagedResponse(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    // ─── GET /api/complaints/priority/{priority} ───────────────────────────────
    @GetMapping("/priority/{priority}")
    public ResponseEntity<?> getByPriority(
            @PathVariable Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Complaint> result = complaintService.getComplaintsByPriority(priority, page, size);
            return ResponseEntity.ok(pagedResponse(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    // ─── GET /api/complaints/email/{email} ─────────────────────────────────────
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getByEmail(@PathVariable String email) {
        try {
            List<Complaint> result = complaintService.getComplaintsByEmail(email);
            return ResponseEntity.ok(successResponse("Complaints for " + email, result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    // ─── GET /api/complaints/search?keyword=xxx ────────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Complaint> result = complaintService.searchComplaints(keyword, page, size);
            return ResponseEntity.ok(pagedResponse(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    // ─── GET /api/complaints/urgent ────────────────────────────────────────────
    @GetMapping("/urgent")
    public ResponseEntity<?> getUrgent() {
        try {
            List<Complaint> result = complaintService.getUrgentComplaints();
            return ResponseEntity.ok(successResponse("Urgent complaints", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorResponse(e.getMessage()));
        }
    }

    // ─── GET /api/complaints/recent?limit=5 ───────────────────────────────────
    @GetMapping("/recent")
    public ResponseEntity<?> getRecent(@RequestParam(defaultValue = "5") int limit) {
        try {
            List<Complaint> result = complaintService.getRecentComplaints(limit);
            return ResponseEntity.ok(successResponse("Recent complaints", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorResponse(e.getMessage()));
        }
    }

    // ─── GET /api/complaints/stats ─────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Map<String, Object> stats = complaintService.getDashboardStats();
            return ResponseEntity.ok(successResponse("Dashboard statistics", stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorResponse(e.getMessage()));
        }
    }

    // ─── GET /api/complaints/date-range ────────────────────────────────────────
    @GetMapping("/date-range")
    public ResponseEntity<?> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<Complaint> result = complaintService.getComplaintsByDateRange(start, end);
            return ResponseEntity.ok(successResponse("Complaints in date range", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    // ─── PUT /api/complaints/{id} ──────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateComplaint(
            @PathVariable Long id,
            @Valid @RequestBody Complaint complaint) {
        try {
            Complaint updated = complaintService.updateComplaint(id, complaint);
            return ResponseEntity.ok(successResponse("Complaint updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorResponse(e.getMessage()));
        }
    }

    // ─── PATCH /api/complaints/{id}/status ────────────────────────────────────
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam Status status,
            @RequestParam(required = false) String resolutionNote) {
        try {
            Complaint updated = complaintService.updateStatus(id, status, resolutionNote);
            return ResponseEntity.ok(successResponse("Status updated to " + status, updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorResponse(e.getMessage()));
        }
    }

    // ─── PATCH /api/complaints/{id}/priority ──────────────────────────────────
    @PatchMapping("/{id}/priority")
    public ResponseEntity<?> updatePriority(
            @PathVariable Long id,
            @RequestParam Priority priority) {
        try {
            Complaint updated = complaintService.updatePriority(id, priority);
            return ResponseEntity.ok(successResponse("Priority updated to " + priority, updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorResponse(e.getMessage()));
        }
    }

    // ─── DELETE /api/complaints/{id} ──────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComplaint(@PathVariable Long id) {
        try {
            complaintService.deleteComplaint(id);
            return ResponseEntity.ok(successResponse("Complaint deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(errorResponse(e.getMessage()));
        }
    }

    // ─── Helper methods ────────────────────────────────────────────────────────

    private Map<String, Object> successResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    private Map<String, Object> pagedResponse(Page<?> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", page.getContent());
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("pageSize", page.getSize());
        response.put("isFirst", page.isFirst());
        response.put("isLast", page.isLast());
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
