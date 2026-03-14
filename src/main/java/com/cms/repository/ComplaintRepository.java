package com.cms.repository;

import com.cms.model.Complaint;
import com.cms.model.Complaint.Category;
import com.cms.model.Complaint.Priority;
import com.cms.model.Complaint.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Find by status
    Page<Complaint> findByStatus(Status status, Pageable pageable);

    // Find by category
    Page<Complaint> findByCategory(Category category, Pageable pageable);

    // Find by priority
    Page<Complaint> findByPriority(Priority priority, Pageable pageable);

    // Find by email (all complaints from a specific user)
    List<Complaint> findByEmailOrderByCreatedAtDesc(String email);

    // Find by complainant name (case-insensitive)
    List<Complaint> findByComplainantNameContainingIgnoreCase(String name);

    // Combined filter: status + category
    Page<Complaint> findByStatusAndCategory(Status status, Category category, Pageable pageable);

    // Combined filter: status + priority
    Page<Complaint> findByStatusAndPriority(Status status, Priority priority, Pageable pageable);

    // Full-text search across title, description, and complainant name
    @Query("SELECT c FROM Complaint c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.complainantName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Complaint> searchComplaints(@Param("keyword") String keyword, Pageable pageable);

    // Count by status
    long countByStatus(Status status);

    // Count by category
    long countByCategory(Category category);

    // Count by priority
    long countByPriority(Priority priority);

    // Find complaints created between two dates
    List<Complaint> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Stats: count per status
    @Query("SELECT c.status AS status, COUNT(c) AS count FROM Complaint c GROUP BY c.status")
    List<Map<String, Object>> countGroupByStatus();

    // Stats: count per category
    @Query("SELECT c.category AS category, COUNT(c) AS count FROM Complaint c GROUP BY c.category")
    List<Map<String, Object>> countGroupByCategory();

    /*
     * FIX: Original query used string literals ('HIGH', 'CRITICAL', 'OPEN') in JPQL.
     * JPQL does not accept string literals for enum comparisons — it requires
     * the enum type prefix: com.cms.model.Complaint.Priority.HIGH
     * Using named parameters bound to actual Java enum values is the correct approach.
     */
    @Query("SELECT c FROM Complaint c WHERE c.priority IN :priorities AND c.status = :status ORDER BY c.createdAt ASC")
    List<Complaint> findUrgentComplaints(
            @Param("priorities") List<Priority> priorities,
            @Param("status") Status status);

    // Unassigned complaints — no assignedTo field yet, so we count all OPEN ones
    long countByStatusNot(Status status);

    // Recent complaints ordered by creation date
    @Query("SELECT c FROM Complaint c ORDER BY c.createdAt DESC")
    List<Complaint> findRecentComplaints(Pageable pageable);
}
