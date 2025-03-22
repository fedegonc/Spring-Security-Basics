package com.example.registrationlogindemo.service.impl;
import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ArticleRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    SolicitudeRepository solicitudeRepository;

    @Autowired
    ImageService imageService;

    public ModelAndView populateDashboard(String viewName, String destino) {
        ModelAndView mv = new ModelAndView(viewName);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            User usuario = userRepository.findByUsername(username);
            mv.addObject("user", usuario);
            mv.addObject("username", username);
            mv.addObject("principal", authentication.getPrincipal().toString());
        }

        List<Solicitude> solicitudes = solicitudeRepository.findByDestinoContaining(destino);
        mv.addObject("solicitudes", solicitudes);

        List<Article> articles = articleRepository.findAll();
        mv.addObject("articles", articles);

        List<User> users = userRepository.findAll();
        mv.addObject("users", users);


        return mv;
    }
}
