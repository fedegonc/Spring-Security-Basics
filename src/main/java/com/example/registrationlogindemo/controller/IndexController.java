package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
public class IndexController {
    @Autowired
    SolicitudeRepository solicitudeRepository;

    @GetMapping("")
    public String redirectToIndex() {
        return "redirect:/index";
    }

    @GetMapping("/")
    public String redirect() {
        return "redirect:/index";
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView getIndex() {
        ModelAndView mv = new ModelAndView("/index");
        List<Solicitude> solicitude = solicitudeRepository.findSolicitudeByActivo("Activo");
        mv.addObject("solicitude", solicitude);
        return mv;
    }
    @RequestMapping(value = "/imagem/{imagem}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] getImagens(@PathVariable("imagem") String imagem) throws IOException {
        File caminho = new File("./src/main/resources/static/img/" + imagem);
        if (imagem != null || imagem.trim().length() > 0) {
            return Files.readAllBytes(caminho.toPath());
        }
        return null;
    }
}
