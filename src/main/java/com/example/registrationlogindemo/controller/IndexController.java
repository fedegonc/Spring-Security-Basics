package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Livros;
import com.example.registrationlogindemo.entity.Empleos;
import com.example.registrationlogindemo.repository.EmpleosRepository;
import com.example.registrationlogindemo.repository.LivrosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
public class IndexController {
    @Autowired
    LivrosRepository livrosRepository;

    @Autowired
    EmpleosRepository empleosRepository;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView getLivroUsers() {
        ModelAndView mv = new ModelAndView("/index");
        List<Livros> livros = livrosRepository.findAll();
        mv.addObject("livros", livros);

        List<Empleos> empleos = empleosRepository.findAll();
        mv.addObject("empleos", empleos);
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
