package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.repository.ArticleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
public class GuestController {
    @Autowired
    ArticleRepository articleRepository;

    // Redirecciona a la página de inicio
    @GetMapping({"", "/"})
    public String redirectToIndex() {
        return "redirect:/index";
    }

    // Obtiene la página de inicio y muestra las solicitudes activas
    @GetMapping("/index")
    public ModelAndView getIndex() {
        ModelAndView mv = new ModelAndView("/index");

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Article> articles = articleRepository.findAll();
        mv.addObject("articles", articles);

        if (principal instanceof UserDetails) {
            // El usuario ya está autenticado, redirigir a la página de inicio correspondiente
            return new ModelAndView("redirect:/init");
        }

        return mv;
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // Obtiene el contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            // Invalida la sesión actual
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        // Redirige a la página de inicio después de cerrar sesión
        return "redirect:/";
    }

    @RequestMapping(value = "/img/{img}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] getImagens(@PathVariable("img") String img) throws IOException {
        File caminho = new File("./src/main/resources/static/img/" + img);
        if (img != null || img.trim().length() > 0) {
            return Files.readAllBytes(caminho.toPath());
        }
        return null;
    }

}

