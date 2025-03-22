package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Organization;

import java.util.List;
import java.util.Optional;

public interface OrganizationService {
    Organization saveOrganization(Organization organization);
    List<Organization> getAllOrganizations();
    List<Organization> getOrganizationsByStatus(String status);
    List<Organization> getOrganizationsByOwner(Long ownerId);
    Optional<Organization> getOrganizationById(Long id);
    void updateOrganizationStatus(Long id, String status);
}
