package com.cms.repository;

import com.cms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used for login and profile lookup
    Optional<User> findByEmail(String email);

    // Used during registration to block duplicate emails
    boolean existsByEmail(String email);
}
