package com.frauddetector.repository;

import com.frauddetector.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    java.util.List<User> findByRoleIn(java.util.List<User.Role> roles);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR CAST(u.role AS string) = :role) AND " +
           "(:status IS NULL OR CAST(u.status AS string) = :status)")
    org.springframework.data.domain.Page<User> getFilteredUsers(
            @org.springframework.data.repository.query.Param("role") String role,
            @org.springframework.data.repository.query.Param("status") String status,
            org.springframework.data.domain.Pageable pageable);
}