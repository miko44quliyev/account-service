package com.example.accountservice.account.repository;

import com.example.accountservice.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByOwnerId(UUID ownerId);

    boolean existsByAccountNumber(String accountNumber);

    @Query("""
           SELECT a
           FROM Account a
           JOIN FETCH a.owner
           WHERE a.id = :id
           """)
    Optional<Account> findByIdWithUser(@Param("id") UUID id);
    @Query("""
           SELECT a
           FROM Account a
           JOIN FETCH a.owner
           WHERE a.owner.id = :ownerId
           """)
    List<Account> findAllByOwnerIdWithUser(@Param("ownerId") UUID ownerId);
}
