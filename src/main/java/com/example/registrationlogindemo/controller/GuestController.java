package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ArticleRepository;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
public class GuestController {
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReportRepository reportRepository;


    // Obtiene la página de inicio y muestra las solicitudes activas
    @GetMapping({"", "/", "/index"})
    public ModelAndView getIndex() {
        ModelAndView mv = new ModelAndView("index");

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

    @GetMapping("/img/{img}")
    @ResponseBody
    public byte[] getImagens(@PathVariable("img") String img) throws IOException {
        File caminho = new File("./src/main/resources/static/img/" + img);
        if (img != null || img.trim().length() > 0) {
            return Files.readAllBytes(caminho.toPath());
        }
        return null;
    }

    @GetMapping("/report")
    public ModelAndView newReport() {
        ModelAndView mv = new ModelAndView("report-problem");
        return mv;
    }
    @PostMapping("/report")
    public String newReportPost(@Valid Report report,
                                BindingResult result, RedirectAttributes msg,
                                @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user != null) {
            report.setUser(user);
            reportRepository.save(report);
            msg.addFlashAttribute("rexito", "report realizada con éxito.");
        } else {
            msg.addFlashAttribute("rerror", "No se pudo encontrar el usuario actual.");
        }

        return "redirect:/init";
    }

}

