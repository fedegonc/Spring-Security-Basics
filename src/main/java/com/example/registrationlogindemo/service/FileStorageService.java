package com.example.registrationlogindemo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Servicio para manejar el almacenamiento de archivos
 * Centraliza toda la lógica relacionada con subida, almacenamiento y recuperación de archivos
 */
public interface FileStorageService {
    
    /**
     * Maneja la subida de imágenes y devuelve el nombre del archivo
     * @param file Archivo a subir
     * @param currentImageName Nombre de imagen actual (si existe)
     * @return Nombre del archivo subido o el nombre actual si no se sube ninguno
     * @throws IOException Si ocurre un error al procesar el archivo
     */
    String storeImage(MultipartFile file, String currentImageName) throws IOException;
    
    /**
     * Genera un nombre único para un archivo basado en UUID
     * @param originalFilename Nombre original del archivo
     * @return Nombre de archivo único
     */
    String generateUniqueFileName(String originalFilename);
    
    /**
     * Elimina una imagen del sistema de archivos
     * @param imageName Nombre de la imagen a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean deleteImage(String imageName);
    
    /**
     * Obtiene la ruta completa a una imagen
     * @param imageName Nombre de la imagen
     * @return Ruta completa a la imagen
     */
    String getImagePath(String imageName);
    
    /**
     * Verifica si un archivo es una imagen válida
     * @param file Archivo a verificar
     * @return true si es una imagen válida, false en caso contrario
     */
    boolean isValidImageFile(MultipartFile file);
    
    /**
     * Obtiene el mensaje de error específico si la validación de la imagen falla
     * @param file La imagen que falló la validación
     * @return El mensaje de error específico
     */
    String getValidationErrorMessage(MultipartFile file);
    
    /**
     * Maneja la subida de imágenes y devuelve el nombre del archivo
     * Este método es un alias para storeImage para mantener compatibilidad con el código existente
     * @param file Archivo a subir
     * @param currentImageName Nombre de imagen actual (si existe)
     * @return Nombre del archivo subido o el nombre actual si no se sube ninguno
     * @throws IOException Si ocurre un error al procesar el archivo
     */
    String handleImageUpload(MultipartFile file, String currentImageName) throws IOException;
}
