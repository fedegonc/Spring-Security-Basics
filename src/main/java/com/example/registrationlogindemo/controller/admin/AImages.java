package com.example.registrationlogindemo.controller.admin;

import com.example.registrationlogindemo.entity.Image;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ImageRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.ImageService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import javax.imageio.ImageIO;

@Controller
@RequestMapping("/admin")
public class AImages {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageRepository;

    private UserService userService;
    private ImageService imageService;

    @Autowired
    public AImages(UserService userService, ImageService imageService) {
        this.userService = userService;
        this.imageService = imageService;
    }

    @GetMapping("/images")
    public ModelAndView adminImages() {
        ModelAndView mv = new ModelAndView("admin/images");
        List<Image> images = imageRepository.findAll();
        mv.addObject("images", images);
        return mv;
    }

    @GetMapping("/newimage")
    public ModelAndView adminNewImage() {
        ModelAndView mv = new ModelAndView("image/newimage");
        return mv;
    }

    @PostMapping("/newimage")
    public String adminUploadImage(@Valid Image image,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes,
                                   @RequestParam("file") MultipartFile file,
                                   @AuthenticationPrincipal UserDetails currentUser) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Error al iniciar solicitud. Por favor, llenar todos los campos.");
            return "redirect:/admin/dashboard"; // Cambia esta URL según la estructura de tu aplicación
        }

        if (!file.isEmpty()) {
            try {
                // Redimensionar la imagen a 500x500 píxeles manteniendo el formato original
                BufferedImage bufferedImage = Thumbnails.of(file.getInputStream())
                        .size(500, 500)
                        .outputQuality(0.8f)
                        .asBufferedImage();

                // Convertir la BufferedImage a un arreglo de bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos); // Usa el formato de la imagen original
                baos.flush();
                byte[] resizedImageBytes = baos.toByteArray();
                baos.close();

                image.setData(resizedImageBytes);
                image.setType(file.getContentType());
                image.setNombre(file.getOriginalFilename());
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al guardar la imagen. Inténtalo de nuevo más tarde.");
                return "redirect:/admin/images"; // Cambia esta URL según la estructura de tu aplicación
            }
        } else {
            image.setData(null); // O establece un valor por defecto
        }

        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user != null) {
            image.setUser(user);
            image.setFecha(LocalDateTime.now()); // Establece la fecha actual
            imageRepository.save(image);
            redirectAttributes.addFlashAttribute("exito", "Imagen subida con éxito.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
        }

        return "redirect:/admin/images"; // Cambia esta URL según la estructura de tu aplicación
    }

    @GetMapping("/editimage/{id}")
    public ModelAndView adminEditImage(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("image/editimage");
        Optional<Image> image = imageRepository.findById(id);
        if (image.isPresent()) {
            mv.addObject("image", image.get());
        } else {
            mv.setViewName("redirect:/user/welcome");
        }
        return mv;
    }

    @PostMapping("/editimage/{id}")
    public ModelAndView adminEditImage(@PathVariable("id") long id,
                                       @ModelAttribute("image") @Valid Image image,
                                       BindingResult result, RedirectAttributes msg,
                                       @RequestParam("file") MultipartFile imagen) {
        ModelAndView mv = new ModelAndView();

        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al editar. Por favor, complete todos los campos correctamente.");
            mv.setViewName("redirect:/admin/editimage/" + id);
            return mv;
        }

        Optional<Image> imageOptional = imageRepository.findById(id);
        if (imageOptional.isPresent()) {
            Image imageEdit = imageOptional.get();

            try {
                if (!imagen.isEmpty()) {
                    imageEdit.setData(imagen.getBytes());
                    imageEdit.setType(imagen.getContentType());
                    imageEdit.setNombre(imagen.getOriginalFilename());
                }
            } catch (IOException e) {
                e.printStackTrace();
                msg.addFlashAttribute("error", "Error al cargar el archivo de imagen.");
                mv.setViewName("redirect:/admin/editimage/" + id);
                return mv;
            }

            imageRepository.save(imageEdit);
            msg.addFlashAttribute("exito", "Imagen editada con éxito.");
            mv.setViewName("redirect:/admin/images");
        } else {
            msg.addFlashAttribute("error", "No se encontró la imagen a editar.");
            mv.setViewName("redirect:/admin/images");
        }

        return mv;
    }

    @GetMapping("/deletimage/{id}")
    public String deleteImage(@PathVariable("id") long id) {
        imageService.eliminarEntidad(id);
        return "redirect:/admin/images";
    }
}
