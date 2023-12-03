package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Empleos;
import com.example.registrationlogindemo.repository.EmpleosRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
public class EmpleosController {

    @Autowired
    EmpleosRepository empleosRepository;


    @RequestMapping(value = "/inicioempleos", method = RequestMethod.GET)
    public String inicioEmpleo() {
        return "empleos/inicioempleo";
    }

    @RequestMapping(value = "/imagen/{imagem}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] getImagens(@PathVariable("imagem") String imagem) throws IOException {
        File caminho = new File("./src/main/resources/static/img/" + imagem);
        if (imagem != null || imagem.trim().length() > 0) {
            return Files.readAllBytes(caminho.toPath());
        }
        return null;
    }


    @RequestMapping(value = "/nuevoempleo", method = RequestMethod.GET)
    public String novoEmpleo() {
        return "empleos/nuevoempleo";
    }

    @RequestMapping(value = "/nuevoempleo", method = RequestMethod.POST)
    public String novoEmpleo(@Valid Empleos empleos,
                                         BindingResult result, RedirectAttributes msg,
                                         @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro",
                    "Erro ao cadastrar empleos. Por favor, preencha todos os campos");
            return "redirect:/users/nuevoempleo";
        }
        try {
            if (!imagem.isEmpty()) {
                byte[] bytes = imagem.getBytes();
                Path caminho = Paths.get("./src/main/resources/static/img/" + imagem.getOriginalFilename());
                Files.write(caminho, bytes);
                empleos.setImagen(imagem.getOriginalFilename());
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar imagem");
        }

        empleosRepository.save(empleos);
        msg.addFlashAttribute("sucesso",
                "empleos cadastrado.");
        return "redirect:/dashboard";
    }

    @RequestMapping(value = "/listarempleos", method = RequestMethod.GET)
    public ModelAndView getListarEmpleos() {
        ModelAndView mv = new ModelAndView("/empleos/listarempleos");
        List<Empleos> empleos = empleosRepository.findAll();
        mv.addObject("empleos", empleos);
        return mv;
    }


    @RequestMapping(value = "/editarempleo/{id}", method = RequestMethod.GET)
    public ModelAndView editarEmpleo(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("empleos/editarempleo");
        Optional<Empleos> empleosOptional = empleosRepository.findById(id);
        if (empleosOptional.isPresent()) {
            Empleos empleos = empleosOptional.get();
            mv.addObject("empleos", empleos);
        } else {
            mv.setViewName("redirect:/users/error"); // Redirigir a la página de error
        }
        return mv;
    }

    @RequestMapping(value = "/editarempleo/{id}", method = RequestMethod.POST)
    public String editarEmpleoBanco(@ModelAttribute("empleo") @Valid Empleos empleos,
                                      BindingResult result, RedirectAttributes msg,
                                      @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Erro ao editar." +
                    " Por favor, preencha todos os campos");
            return "redirect:/users/editar/" + empleos.getId();
        }
        Empleos empleoExistente = empleosRepository.findById(Math.toIntExact(empleos.getId())).orElse(null);
        if (empleoExistente != null) {
            empleoExistente.setId(empleos.getId());
            empleoExistente.setEmpresa(empleos.getEmpresa());
            empleoExistente.setDescripcion(empleos.getDescripcion());
            empleoExistente.setCategoria(empleos.getCategoria());
            empleoExistente.setActivo(empleos.getActivo());
            empleoExistente.setContacto(empleos.getContacto());
            empleoExistente.setPuesto(empleos.getPuesto());
            empleoExistente.setUbicacion(empleos.getUbicacion());

            try {
                if (!imagem.isEmpty()) {
                    byte[] bytes = imagem.getBytes();
                    Path caminho = Paths.get("./src/main/resources/static/img/" + imagem.getOriginalFilename());
                    Files.write(caminho, bytes);
                    empleoExistente.setImagen(imagem.getOriginalFilename());
                }
            } catch (IOException e) {
                System.out.println("Error de imagen");
            }

            empleosRepository.save(empleoExistente);
            msg.addFlashAttribute("sucesso", "Empleo editado com sucesso.");
        }

        return "redirect:/users/listarempleos";
    }

    @RequestMapping(value = "/deletarempleo/{id}", method = RequestMethod.GET)
    public String excluirEmpleo(@PathVariable("id") int id) {
        empleosRepository.deleteById(id);
        return "redirect:/users/listarempleos";
    }

}
