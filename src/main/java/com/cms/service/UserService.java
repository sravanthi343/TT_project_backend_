package com.cms.service;

import com.cms.model.User;
import com.cms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    
    

    // ─── REGISTER ─────────────────────────────────────────────────────────────

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("An account with this email already exists: " + user.getEmail());
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        return userRepository.save(user);
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with email: " + email));
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Incorrect password");
        }
        
        return user;
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    public User updateUser(Long id, User updatedUser) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        if (updatedUser.getName() != null && !updatedUser.getName().isBlank()) {
            existing.setName(updatedUser.getName());
        }
        if (updatedUser.getPhone() != null) {
            existing.setPhone(updatedUser.getPhone());
        }
        // Email and role are intentionally not updatable via this method
        return userRepository.save(existing);
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
