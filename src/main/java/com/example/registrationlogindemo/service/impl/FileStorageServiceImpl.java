package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Implementación del servicio para manejar el almacenamiento de archivos
 * Centraliza toda la lógica relacionada con subida, almacenamiento y recuperación de archivos
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";
    
    /**
     * Maneja la subida de imágenes y devuelve el nombre del archivo
     * @param file Archivo a subir
     * @param currentImageName Nombre de imagen actual (si existe)
     * @return Nombre del archivo subido o el nombre actual si no se sube ninguno
     * @throws IOException Si ocurre un error al procesar el archivo
     */
    @Override
    public String storeImage(MultipartFile file, String currentImageName) throws IOException {
        if (file != null && !file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                // Crear directorios si no existen
                Path uploadDirectory = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadDirectory)) {
                    Files.createDirectories(uploadDirectory);
                }
                
                // Generar un nombre de archivo único para evitar colisiones
                String fileExtension = getFileExtension(originalFilename);
                String modifiedFilename = originalFilename.replace(" ", "_");
                
                // Guardar el archivo
                byte[] bytes = file.getBytes();
                Path path = Paths.get(UPLOAD_DIR + modifiedFilename);
                Files.write(path, bytes);
                return modifiedFilename;
            }
        }
        return currentImageName;
    }
    
    /**
     * Genera un nombre único para un archivo basado en UUID
     * @param originalFilename Nombre original del archivo
     * @return Nombre de archivo único
     */
    @Override
    public String generateUniqueFileName(String originalFilename) {
        String fileExtension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + fileExtension;
    }
    
    /**
     * Elimina una imagen del sistema de archivos
     * @param imageName Nombre de la imagen a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    @Override
    public boolean deleteImage(String imageName) {
        if (imageName != null && !imageName.isEmpty()) {
            try {
                Path path = Paths.get(UPLOAD_DIR + imageName);
                if (Files.exists(path)) {
                    Files.delete(path);
                    return true;
                }
            } catch (IOException e) {
                // Log error
                return false;
            }
        }
        return false;
    }
    
    /**
     * Obtiene la ruta completa a una imagen
     * @param imageName Nombre de la imagen
     * @return Ruta completa a la imagen
     */
    @Override
    public String getImagePath(String imageName) {
        return UPLOAD_DIR + imageName;
    }
    
    /**
     * Obtiene la extensión de un archivo
     * @param filename Nombre del archivo
     * @return Extensión del archivo con el punto (ej: ".jpg")
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    /**
     * Verifica si un archivo es una imagen válida
     * @param file Archivo a verificar
     * @return true si es una imagen válida, false en caso contrario
     */
    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        // Validar tipos de contenido de imágenes comunes
        return contentType.startsWith("image/");
    }
}
