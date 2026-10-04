package com.example.myproject.repository;

import com.example.myproject.entity.User;
import com.example.myproject.entity.User.UserStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByResetToken(String resetToken);

    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String fullName, String email);

    List<User> findByRole(User.Role role);

    // List<User> findByEnabled(boolean enabled);

    List<User> findByStatus(UserStatus status);

    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndStatus(
            String fullName, String email, UserStatus status);

    long count();

    long countByStatus(UserStatus status);

}
