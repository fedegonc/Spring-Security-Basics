package com.example.registrationlogindemo.controller;
import com.example.registrationlogindemo.entity.Image;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@RequestMapping("/images")
public class ImageController {

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";
    @GetMapping("/newimage")
    public String uploadImageForm(Model model) {
        model.addAttribute("image", new Image());
        return "image/newimage"; // Vista para cargar una nueva imagen
    }

    @PostMapping("/newimage")
    public String uploadImage(@Valid Image image,
                              BindingResult result,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes msg) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al cargar la imagen. Por favor, complete todos los campos correctamente.");
            return "redirect:/image/upload";
        }

        try {
            if (!file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String modifiedFilename = file.getOriginalFilename().replace(" ", "_"); // Reemplazar espacios en el nombre del archivo
                Path path = Paths.get(UPLOAD_DIR + modifiedFilename);
                Files.write(path, bytes);
                image.setImagen(modifiedFilename);
            } else {
                image.setImagen(null); // Opcional: Establecer un valor por defecto si no se carga ninguna imagen
            }
        } catch (IOException e) {
            msg.addFlashAttribute("error", "Error al guardar la imagen. Inténtalo de nuevo más tarde.");
            return "redirect:/image/upload";
        }

        image.setFecha(LocalDateTime.now()); // Establecer la fecha actual
        // Aquí deberías obtener el usuario actual y asignarlo a la imagen
        // Ejemplo: User currentUser = userRepository.findByUsername(authentication.getName());
        // image.setUser(currentUser);

        // Guardar la imagen en la base de datos o realizar otras operaciones necesarias
        // imageRepository.save(image);

        msg.addFlashAttribute("success", "Imagen cargada exitosamente.");
        return "redirect:/image/upload";
    }
}
