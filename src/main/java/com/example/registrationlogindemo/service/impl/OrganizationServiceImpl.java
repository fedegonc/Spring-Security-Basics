package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Organization;
import com.example.registrationlogindemo.repository.OrganizationRepository;
import com.example.registrationlogindemo.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Override
    public Organization saveOrganization(Organization organization) {
        return organizationRepository.save(organization);
    }
    
    @Override
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }
    
    @Override
    public List<Organization> getOrganizationsByStatus(String status) {
        return organizationRepository.findByStatus(status);
    }
    
    @Override
    public List<Organization> getOrganizationsByOwner(Long ownerId) {
        return organizationRepository.findByOwnerId(ownerId);
    }
    
    @Override
    public Optional<Organization> getOrganizationById(Long id) {
        return organizationRepository.findById(id);
    }
    
    @Override
    public void updateOrganizationStatus(Long id, String status) {
        Optional<Organization> org = organizationRepository.findById(id);
        if (org.isPresent()) {
            Organization organization = org.get();
            organization.setStatus(status);
            organizationRepository.save(organization);
        }
    }
}
