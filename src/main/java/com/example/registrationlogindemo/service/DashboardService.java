package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

public interface DashboardService {

    // Método para obtener información del dashboard, incluidos los usuarios y artículos
    ModelAndView populateDashboard(String viewName, String destino);

    // Otros métodos que puedan ser útiles, como buscar solicitudes o artículos específicos
    List<Solicitude> getSolicitudesByDestino(String destino);
    List<Article> getAllArticles();
    User getAuthenticatedUser();
}
