package com.cms.controller;

import com.cms.model.User;
import com.cms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ─── POST /api/users/register ─────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            User created = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(successResponse("Account created successfully", sanitize(created)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse(e.getMessage()));
        }
    }

    // ─── POST /api/users/login ────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email    = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(errorResponse("Email and password are required"));
        }
        try {
            User user = userService.loginUser(email, password);
            return ResponseEntity.ok(successResponse("Login successful", sanitize(user)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse(e.getMessage()));
        }
    }

    // ─── GET /api/users ───────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<Map<String, Object>> sanitized = users.stream()
                    .map(this::sanitize)
                    .toList();
            return ResponseEntity.ok(successResponse("All users", sanitized));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse(e.getMessage()));
        }
    }

    // ─── GET /api/users/{id} ──────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(u -> ResponseEntity.ok(successResponse("User found", sanitize(u))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(errorResponse("User not found with id: " + id)));
    }

    // ─── GET /api/users/email/{email} ─────────────────────────────────────────
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(u -> ResponseEntity.ok(successResponse("User found", sanitize(u))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(errorResponse("No user found with email: " + email)));
    }

    // ─── PUT /api/users/{id} ──────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser) {
        try {
            User updated = userService.updateUser(id, updatedUser);
            return ResponseEntity.ok(successResponse("Profile updated successfully", sanitize(updated)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse(e.getMessage()));
        }
    }

    // ─── DELETE /api/users/{id} ───────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(successResponse("User deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(errorResponse(e.getMessage()));
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    // Never expose the password field in any response
    private Map<String, Object> sanitize(User user) {
        Map<String, Object> safe = new HashMap<>();
        safe.put("id",        user.getId());
        safe.put("name",      user.getName());
        safe.put("email",     user.getEmail());
        safe.put("role",      user.getRole());
        safe.put("phone",     user.getPhone());
        safe.put("createdAt", user.getCreatedAt());
        safe.put("updatedAt", user.getUpdatedAt());
        return safe;
    }

    private Map<String, Object> successResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success",   true);
        response.put("message",   message);
        response.put("data",      data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success",   false);
        response.put("message",   message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
