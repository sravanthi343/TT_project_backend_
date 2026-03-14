package com.cms.service;

import com.cms.model.Complaint;
import com.cms.model.Complaint.Category;
import com.cms.model.Complaint.Priority;
import com.cms.model.Complaint.Status;
import com.cms.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    @Autowired
    public ComplaintService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    // ─── CREATE ────────────────────────────────────────────────────────────────

    public Complaint createComplaint(Complaint complaint) {
        if (complaint.getStatus() == null) {
            complaint.setStatus(Status.OPEN);
        }
        if (complaint.getPriority() == null) {
            complaint.setPriority(Priority.MEDIUM);
        }
        return complaintRepository.save(complaint);
    }

    // ─── READ ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Optional<Complaint> getComplaintById(Long id) {
        return complaintRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Complaint> getAllComplaints(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return complaintRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Complaint> getComplaintsByStatus(Status status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return complaintRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Complaint> getComplaintsByCategory(Category category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return complaintRepository.findByCategory(category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Complaint> getComplaintsByPriority(Priority priority, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return complaintRepository.findByPriority(priority, pageable);
    }

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByEmail(String email) {
        return complaintRepository.findByEmailOrderByCreatedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public Page<Complaint> searchComplaints(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return complaintRepository.searchComplaints(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public List<Complaint> getUrgentComplaints() {
        // FIX: Pass actual enum values as parameters instead of string literals in JPQL
        List<Priority> urgentPriorities = Arrays.asList(Priority.HIGH, Priority.CRITICAL);
        return complaintRepository.findUrgentComplaints(urgentPriorities, Status.OPEN);
    }

    @Transactional(readOnly = true)
    public List<Complaint> getRecentComplaints(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return complaintRepository.findRecentComplaints(pageable);
    }

    // ─── UPDATE ────────────────────────────────────────────────────────────────

    public Complaint updateComplaint(Long id, Complaint updatedComplaint) {
        Complaint existing = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + id));

        if (updatedComplaint.getTitle() != null)
            existing.setTitle(updatedComplaint.getTitle());
        if (updatedComplaint.getDescription() != null)
            existing.setDescription(updatedComplaint.getDescription());
        if (updatedComplaint.getComplainantName() != null)
            existing.setComplainantName(updatedComplaint.getComplainantName());
        if (updatedComplaint.getEmail() != null)
            existing.setEmail(updatedComplaint.getEmail());
        if (updatedComplaint.getPhone() != null)
            existing.setPhone(updatedComplaint.getPhone());
        if (updatedComplaint.getCategory() != null)
            existing.setCategory(updatedComplaint.getCategory());
        if (updatedComplaint.getPriority() != null)
            existing.setPriority(updatedComplaint.getPriority());

        return complaintRepository.save(existing);
    }

    public Complaint updateStatus(Long id, Status newStatus, String resolutionNote) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + id));

        complaint.setStatus(newStatus);
        if (resolutionNote != null && !resolutionNote.isBlank()) {
            complaint.setResolutionNote(resolutionNote);
        }
        return complaintRepository.save(complaint);
    }

    public Complaint updatePriority(Long id, Priority newPriority) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + id));

        complaint.setPriority(newPriority);
        return complaintRepository.save(complaint);
    }

    // ─── DELETE ────────────────────────────────────────────────────────────────

    public void deleteComplaint(Long id) {
        if (!complaintRepository.existsById(id)) {
            throw new RuntimeException("Complaint not found with id: " + id);
        }
        complaintRepository.deleteById(id);
    }

    // ─── STATS ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total",      complaintRepository.count());
        stats.put("open",       complaintRepository.countByStatus(Status.OPEN));
        stats.put("inProgress", complaintRepository.countByStatus(Status.IN_PROGRESS));
        stats.put("resolved",   complaintRepository.countByStatus(Status.RESOLVED));
        stats.put("closed",     complaintRepository.countByStatus(Status.CLOSED));
        stats.put("rejected",   complaintRepository.countByStatus(Status.REJECTED));

        // Per category
        Map<String, Long> byCategory = new HashMap<>();
        for (Category cat : Category.values()) {
            byCategory.put(cat.name(), complaintRepository.countByCategory(cat));
        }
        stats.put("byCategory", byCategory);

        // Per priority
        Map<String, Long> byPriority = new HashMap<>();
        for (Priority p : Priority.values()) {
            byPriority.put(p.name(), complaintRepository.countByPriority(p));
        }
        stats.put("byPriority", byPriority);

        // Urgent count (HIGH + CRITICAL open)
        List<Priority> urgentPriorities = Arrays.asList(Priority.HIGH, Priority.CRITICAL);
        stats.put("urgentCount", complaintRepository.findUrgentComplaints(urgentPriorities, Status.OPEN).size());

        // FIX: was storing a raw Page object — must call .getContent() to get a serializable List
        stats.put("recentComplaints", complaintRepository.findRecentComplaints(PageRequest.of(0, 5)));

        // unassignedCount — expected by the frontend dashboard stat card
        stats.put("unassignedCount", complaintRepository.countByStatus(Status.OPEN));

        return stats;
    }

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByDateRange(LocalDateTime start, LocalDateTime end) {
        return complaintRepository.findByCreatedAtBetween(start, end);
    }
}
