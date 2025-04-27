package com.example.registrationlogindemo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Utilidad para manejo de archivos e imágenes
 */
public class FileUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";
    
    /**
     * Guarda un archivo de imagen y devuelve el nombre del archivo generado
     */
    public static String saveImage(MultipartFile imagen) throws IOException {
        if (imagen == null || imagen.isEmpty()) {
            return null;
        }
        
        // Generar nombre único para el archivo
        String originalFilename = StringUtils.cleanPath(imagen.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        // Guardar el archivo
        saveImageFile(imagen, fileName);
        
        return fileName;
    }
    
    /**
     * Obtiene la extensión de un archivo
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    /**
     * Guarda un archivo de imagen en el sistema de archivos
     */
    public static void saveImageFile(MultipartFile image, String fileName) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        
        // Si el archivo ya existe, eliminarlo primero
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        Files.copy(image.getInputStream(), filePath);
        logger.info("Archivo guardado: {}", fileName);
    }
    
    /**
     * Elimina un archivo del sistema de archivos
     */
    public static boolean deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("Archivo eliminado: {}", fileName);
                return true;
            }
            return false;
        } catch (IOException e) {
            logger.error("Error al eliminar archivo {}: {}", fileName, e.getMessage());
            return false;
        }
    }
}
