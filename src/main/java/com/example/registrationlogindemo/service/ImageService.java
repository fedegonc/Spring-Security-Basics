package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Image;
import jakarta.transaction.Transactional;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

public interface ImageService {
    @Transactional
    void eliminarEntidad(Long id);


    void addLanguageImages(ModelAndView mv, Optional<Image> image, String imageName);

}
