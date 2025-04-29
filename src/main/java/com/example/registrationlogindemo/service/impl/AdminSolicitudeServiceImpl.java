package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.service.AdminSolicitudeService;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

/**
 * Implementación del servicio para gestionar las operaciones de solicitudes desde el panel de administración
 */
@Service
public class AdminSolicitudeServiceImpl implements AdminSolicitudeService {

    @Autowired
    private SolicitudeService solicitudeService;
    
    @Autowired
    private ValidationAndNotificationService validationService;
    
    private static final String ERROR_VALIDACION = "Por favor, corrija los errores en el formulario";
    
    /**
     * Prepara el ModelAndView para la vista de solicitudes
     */
    @Override
    public ModelAndView prepareSolicitudesView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            List<Solicitude> solicitudes = solicitudeService.findAll();
            mv.addObject("solicitudes", solicitudes);
            mv.addObject("currentPage", "Solicitudes");
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar solicitudes: " + e.getMessage());
        }
        return mv;
    }
    
    /**
     * Prepara el ModelAndView para la vista de edición de solicitud
     */
    @Override
    public ModelAndView prepareSolicitudeEditView(Long solicitudeId, String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            Solicitude solicitude = solicitudeService.findById(solicitudeId);
            if (solicitude == null) {
                mv.setViewName("redirect:/admin/solicitudes");
                return mv;
            }
            
            mv.addObject("solicitude", solicitude);
            mv.addObject("currentPage", "Editar Solicitud");
            
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la solicitud: " + e.getMessage());
        }
        return mv;
    }
    
    /**
     * Prepara el ModelAndView para la vista de creación de solicitud
     */
    @Override
    public ModelAndView prepareNewSolicitudeView(String viewName) {
        ModelAndView mv = new ModelAndView(viewName);
        try {
            mv.addObject("solicitude", new Solicitude());
            mv.addObject("currentPage", "Nueva Solicitud");
        } catch (Exception e) {
            mv.addObject("error", "Error al preparar el formulario: " + e.getMessage());
        }
        return mv;
    }
    
    /**
     * Actualiza una solicitud existente
     */
    @Override
    public String updateSolicitude(Solicitude solicitude, BindingResult result, 
                                  MultipartFile imagen, RedirectAttributes msg) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return "redirect:/admin/solicitudes/edit/" + solicitude.getId();
        }
        
        try {
            solicitudeService.updateSolicitudeByAdmin(solicitude, imagen, msg);
            validationService.addSuccessMessage(msg, "Solicitud actualizada exitosamente");
        } catch (IOException e) {
            validationService.addErrorMessage(msg, "Error al procesar la imagen: " + e.getMessage());
            return "redirect:/admin/solicitudes/edit/" + solicitude.getId();
        }
        return "redirect:/admin/solicitudes";
    }
    
    /**
     * Crea una nueva solicitud
     */
    @Override
    public String createSolicitude(Solicitude solicitude, BindingResult result, 
                                  MultipartFile imagen, RedirectAttributes msg) {
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, ERROR_VALIDACION);
            return "redirect:/admin/newsolicitude";
        }
        
        try {
            solicitudeService.updateSolicitudeByAdmin(solicitude, imagen, msg);
            validationService.addSuccessMessage(msg, "Solicitud creada exitosamente");
        } catch (IOException e) {
            validationService.addErrorMessage(msg, "Error al procesar la imagen: " + e.getMessage());
            return "redirect:/admin/newsolicitude";
        }
        return "redirect:/admin/solicitudes";
    }
    
    /**
     * Elimina una solicitud por su ID
     */
    @Override
    public String deleteSolicitude(Long id, RedirectAttributes msg) {
        try {
            boolean success = solicitudeService.deleteSolicitudeByAdmin(id);
            if (success) {
                validationService.addSuccessMessage(msg, "Solicitud eliminada correctamente");
            } else {
                validationService.addErrorMessage(msg, "No se pudo eliminar la solicitud");
            }
        } catch (Exception e) {
            validationService.addErrorMessage(msg, "Error al eliminar solicitud: " + e.getMessage());
        }
        return "redirect:/admin/solicitudes";
    }
}
