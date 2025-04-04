package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.service.AdminSolicitudeService;
import com.example.registrationlogindemo.service.FileStorageService;
import com.example.registrationlogindemo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para operaciones administrativas relacionadas con solicitudes
 */
@Service
public class AdminSolicitudeServiceImpl implements AdminSolicitudeService {

    @Autowired
    private SolicitudeRepository solicitudeRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Override
    public List<Solicitude> getAllSolicitudes() {
        return solicitudeRepository.findAll();
    }
    
    @Override
    public Optional<Solicitude> getSolicitudeById(int id) {
        return solicitudeRepository.findById(id);
    }
    
    @Override
    public boolean updateSolicitude(Solicitude solicitude, MultipartFile imagem, RedirectAttributes msg) throws IOException {
        try {
            // Verificar si la solicitud existe
            Optional<Solicitude> solicitudeExistente = solicitudeRepository.findById(solicitude.getId());
            if (!solicitudeExistente.isPresent()) {
                notificationService.addErrorMessage(msg, "Solicitud no encontrada con ID: " + solicitude.getId());
                return false;
            }
            
            Solicitude solicitudeActual = solicitudeExistente.get();
            
            // Actualizar atributos básicos
            solicitudeActual.setCategoria(solicitude.getCategoria());
            solicitudeActual.setDescripcion(solicitude.getDescripcion());
            solicitudeActual.setEstado(solicitude.getEstado());
            
            // Manejar la imagen (si se proporciona una nueva)
            if (imagem != null && !imagem.isEmpty()) {
                // Utilizar el servicio de almacenamiento de archivos para manejar la imagen
                String imageName = fileStorageService.storeImage(imagem, solicitudeActual.getImagen());
                solicitudeActual.setImagen(imageName);
            }
            
            // Actualizar fecha de modificación
            solicitudeActual.setFecha(LocalDateTime.now());
            
            // Guardar la solicitud actualizada
            solicitudeRepository.save(solicitudeActual);
            
            notificationService.addSuccessMessage(msg, "Solicitud actualizada exitosamente");
            return true;
            
        } catch (Exception e) {
            notificationService.addErrorMessage(msg, "Error al actualizar la solicitud: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean createSolicitude(Solicitude solicitude, MultipartFile imagem, RedirectAttributes msg) throws IOException {
        try {
            // Establecer la fecha de creación
            solicitude.setFecha(LocalDateTime.now());
            
            // Manejar la imagen (si se proporciona)
            if (imagem != null && !imagem.isEmpty()) {
                String imageName = fileStorageService.storeImage(imagem, null);
                solicitude.setImagen(imageName);
            }
            
            // Guardar la solicitud
            Solicitude savedSolicitude = solicitudeRepository.save(solicitude);
            
            notificationService.addSuccessMessage(msg, "Solicitud creada exitosamente: " + savedSolicitude.getCategoria());
            return true;
            
        } catch (Exception e) {
            notificationService.addErrorMessage(msg, "Error al crear la solicitud: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteSolicitude(int id) {
        try {
            // Verificar si la solicitud existe
            Optional<Solicitude> solicitude = solicitudeRepository.findById(id);
            if (!solicitude.isPresent()) {
                return false;
            }
            
            // Eliminar la imagen asociada (si existe)
            String imageName = solicitude.get().getImagen();
            if (imageName != null && !imageName.isEmpty()) {
                fileStorageService.deleteImage(imageName);
            }
            
            // Eliminar la solicitud
            solicitudeRepository.deleteById(id);
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
}
