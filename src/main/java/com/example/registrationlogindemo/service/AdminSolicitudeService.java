package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Solicitude;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para manejar las operaciones administrativas relacionadas con solicitudes.
 * Centraliza la lógica de creación, edición y gestión de solicitudes desde la perspectiva del administrador.
 */
public interface AdminSolicitudeService {

    /**
     * Obtiene todas las solicitudes en el sistema
     * @return Lista de todas las solicitudes
     */
    List<Solicitude> getAllSolicitudes();
    
    /**
     * Obtiene una solicitud por su ID
     * @param id ID de la solicitud
     * @return Solicitud encontrada o vacío si no existe
     */
    Optional<Solicitude> getSolicitudeById(int id);
    
    /**
     * Actualiza una solicitud existente
     * @param solicitude Solicitud con los datos actualizados
     * @param result Resultado de la validación
     * @param imagem Archivo de imagen (opcional)
     * @param msg Para mensajes de retroalimentación
     * @return true si se actualizó correctamente, false en caso contrario
     */
    boolean updateSolicitude(Solicitude solicitude, MultipartFile imagem, RedirectAttributes msg) throws IOException;
    
    /**
     * Crea una nueva solicitud
     * @param solicitude Datos de la solicitud a crear
     * @param imagem Archivo de imagen (opcional)
     * @param msg Para mensajes de retroalimentación
     * @return true si se creó correctamente, false en caso contrario
     */
    boolean createSolicitude(Solicitude solicitude, MultipartFile imagem, RedirectAttributes msg) throws IOException;
    
    /**
     * Elimina una solicitud por su ID
     * @param id ID de la solicitud a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean deleteSolicitude(int id);
}
