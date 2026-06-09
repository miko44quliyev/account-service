package com.example.accountservice.user.repository;

import com.example.accountservice.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
           SELECT u
           FROM User u
           LEFT JOIN FETCH u.accounts
           WHERE u.id = :id
           """)
    Optional<User> findByIdWithAccounts(@Param("id") UUID id);
}