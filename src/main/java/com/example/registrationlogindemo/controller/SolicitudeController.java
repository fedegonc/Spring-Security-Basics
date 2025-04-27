package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.service.SolicitudeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/user")
public class SolicitudeController {

    @Autowired
    private SolicitudeService solicitudeService;

    @GetMapping("/nueva-solicitud")
    public ModelAndView newSolicitude(@AuthenticationPrincipal UserDetails userDetails) {
        // Delegamos la preparación del formulario al servicio
        ModelAndView modelAndView = solicitudeService.prepareNewSolicitudeForm(userDetails);
        // La vista ya está configurada correctamente en el servicio
        return modelAndView;
    }

    /**
     * Endpoint alternativo para nueva solicitud (usando nombre en inglés)
     */
    @GetMapping("/newsolicitude")
    public ModelAndView newSolicitudeAlt(@AuthenticationPrincipal UserDetails userDetails) {
        // Reutilizamos el método existente para mantener la consistencia
        return newSolicitude(userDetails);
    }

    /**
     * Endpoint alternativo POST para nueva solicitud (usando nombre en inglés)
     */
    @PostMapping("/newsolicitude")
    public String newSolicitudePostAlt(@Valid @ModelAttribute("solicitude") Solicitude solicitud,
                                    BindingResult result, RedirectAttributes msg,
                                    @RequestParam(value = "file", required = false) MultipartFile imagen,
                                    @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        // Reutilizamos el método existente para mantener la consistencia
        return newSolicitudePost(solicitud, result, msg, imagen, userDetails);
    }

    @PostMapping("/nueva-solicitud")
    public String newSolicitudePost(@Valid @ModelAttribute("solicitude") Solicitude solicitud,
                                    BindingResult result, RedirectAttributes msg,
                                    @RequestParam(value = "file", required = false) MultipartFile imagen,
                                    @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al iniciar solicitud. Por favor, llenar todos los campos");
            return "redirect:/user/nueva-solicitud";
        }

        // Delegamos la creación de la solicitud al servicio
        return solicitudeService.createSolicitude(solicitud, imagen, msg, userDetails);
    }

    @GetMapping("/editsolicitude/{id}")
    public ModelAndView showEditSolicitudeForm(@PathVariable("id") int id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        // Delegamos la preparación del formulario de edición al servicio
        return solicitudeService.prepareEditSolicitudeForm(id, userDetails);
    }

    @PostMapping("/editsolicitude/{id}")
    public String editSolicitude(@PathVariable("id") int id,
                                 @ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                 BindingResult result, RedirectAttributes msg,
                                 @RequestParam(value = "file", required = false) MultipartFile imagen,
                                 @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:/user/editsolicitude/" + id;
        }

        // Delegamos la actualización de la solicitud al servicio
        return solicitudeService.updateSolicitude(id, solicitude, imagen, msg, userDetails);
    }

    @PostMapping("/solicitude/{id}/messages")
    public String sendMessage(@PathVariable("id") int id,
                              @RequestParam("messageInput") String messageContent,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        // Delegamos el envío del mensaje al servicio
        return solicitudeService.sendMessage(id, messageContent, userDetails, redirectAttributes);
    }

    @GetMapping("/deletesolicitude/{id}")
    public String deleteSolicitude(@PathVariable("id") long id,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        // Delegamos la eliminación de la solicitud al servicio
        return solicitudeService.deleteSolicitude(id, userDetails);
    }
}
