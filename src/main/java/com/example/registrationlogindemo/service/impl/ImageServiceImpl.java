package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Image;
import com.example.registrationlogindemo.repository.ImageRepository;
import com.example.registrationlogindemo.service.ImageService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Service
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {

        this.imageRepository = imageRepository;
    }

    public void addLanguageImages(ModelAndView mv, Optional<Image> image, String imageName) {
        if (image.isPresent()) {
            mv.addObject(imageName, image.get().getNombre());
        }
    }

    @Override
    @Transactional
    public void eliminarEntidad(Long id) {
        imageRepository.deleteById(id);
    }
}
