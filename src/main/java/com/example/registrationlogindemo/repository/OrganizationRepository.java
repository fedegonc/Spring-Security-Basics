package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findByOwnerId(Long ownerId);
    List<Organization> findByStatus(String status);
    Optional<Organization> findByNameAndType(String name, String type);
}
