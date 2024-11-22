package com.example.registrationlogindemo.controller;


import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

public abstract class BaseController {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ImageService imageService;

    /**
     * Método para obtener el usuario autenticado actualmente.
     *
     * @return Objeto User si está autenticado, de lo contrario, retorna null.
     */
    protected User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userRepository.findByUsername(userDetails.getUsername());
        }
        return null;
    }

    /**
     * Método para añadir imágenes de banderas al ModelAndView.
     *
     * @param mv ModelAndView al que se agregan las banderas.
     */
    protected void addFlagImages(ModelAndView mv) {
        imageService.addFlagImages(mv);
    }

    /**
     * Método para inicializar el ModelAndView con información del usuario autenticado.
     *
     * @param viewName Nombre de la vista.
     * @return ModelAndView inicializado con los datos comunes.
     */
    protected ModelAndView initializeModelAndView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);

        User currentUser = getAuthenticatedUser();
        if (currentUser != null) {
            mv.addObject("user", currentUser);
            mv.addObject("username", currentUser.getUsername());
        }

        // Añadir imágenes de banderas al ModelAndView
        addFlagImages(mv);

        return mv;
    }
}
