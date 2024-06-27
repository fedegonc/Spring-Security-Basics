package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.repository.ImageRepository;
import com.example.registrationlogindemo.service.ImageService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {

        this.imageRepository = imageRepository;
    }

    @Override
    @Transactional
    public void eliminarEntidad(Long id) {
        imageRepository.deleteById(id);
    }
}
