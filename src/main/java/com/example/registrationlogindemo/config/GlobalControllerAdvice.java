package com.example.registrationlogindemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {
    @Autowired
    private UserStatusService userStatusService;
    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String);


        System.out.println("Estado de autenticación del usuario: " + isAuthenticated);
        return authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String);

    }

    @ModelAttribute("userStatus")
    public UserStatusService userStatus() {
        return userStatusService;
    }
}
