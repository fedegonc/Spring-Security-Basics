package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Solicitude;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Servicio para gestionar las operaciones de solicitudes desde el panel de administración
 */
public interface AdminSolicitudeService {

    /**
     * Prepara el ModelAndView para la vista de solicitudes
     */
    ModelAndView prepareSolicitudesView(String viewName);

    /**
     * Prepara el ModelAndView para la vista de edición de solicitud
     */
    ModelAndView prepareSolicitudeEditView(Long solicitudeId, String viewName);

    /**
     * Prepara el ModelAndView para la vista de creación de solicitud
     */
    ModelAndView prepareNewSolicitudeView(String viewName);

    /**
     * Actualiza una solicitud existente
     */
    String updateSolicitude(Solicitude solicitude, BindingResult result, 
                           MultipartFile imagen, RedirectAttributes msg);

    /**
     * Crea una nueva solicitud
     */
    String createSolicitude(Solicitude solicitude, BindingResult result, 
                           MultipartFile imagen, RedirectAttributes msg);

    /**
     * Elimina una solicitud por su ID
     */
    String deleteSolicitude(Long id, RedirectAttributes msg);
}
