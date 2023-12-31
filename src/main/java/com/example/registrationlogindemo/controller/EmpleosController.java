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

    @RequestMapping(value = "/img/{imagen}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] getImagens(@PathVariable("imagen") String imagen) throws IOException {
        File caminho = new File("./src/main/resources/static/img/" + imagen);
        if (imagen != null || imagen.trim().length() > 0) {
            return Files.readAllBytes(caminho.toPath());
        }
        return null;
    }





    @RequestMapping(value = "/editarempleo/{id}", method = RequestMethod.GET)
    public ModelAndView editarEmpleo(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("empleos/editarempleo");
        Optional<Empleos> empleosOptional = empleosRepository.findById(id);
        if (empleosOptional.isPresent()) {
            Empleos empleo = empleosOptional.get();
            mv.addObject("empleo", empleo);
        } else {
            mv.setViewName("redirect:/users/error"); // Redirigir a la página de error
        }
        return mv;
    }

    @RequestMapping(value = "/editarempleo/{id}", method = RequestMethod.POST)
    public String editarEmpleoBanco(@ModelAttribute("empleo") @Valid Empleos empleo,
                                      BindingResult result, RedirectAttributes msg,
                                      @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Erro ao editar." +
                    " Por favor, preencha todos os campos");
            return "redirect:/editarempleo/" + empleo.getId();
        }
        Empleos empleoExistente = empleosRepository.findById(Math.toIntExact(empleo.getId())).orElse(null);
        if (empleoExistente != null) {
            empleoExistente.setId(empleo.getId());
            empleoExistente.setEmpresa(empleo.getEmpresa());
            empleoExistente.setDescripcion(empleo.getDescripcion());
            empleoExistente.setCategoria(empleo.getCategoria());
            empleoExistente.setActivo(empleo.getActivo());
            empleoExistente.setPuesto(empleo.getPuesto());
            empleoExistente.setUbicacion(empleo.getUbicacion());
            empleoExistente.setJornada(empleo.getJornada());

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

        return "redirect:/dashboard";
    }



    @RequestMapping(value = {"/buscarPorEmpresa"}, method=RequestMethod.POST)
    public ModelAndView pesquisar(@RequestParam("buscarPorEmpresa") String buscarPorEmpresa) {
        ModelAndView mv = new ModelAndView("/index");
        List<Empleos> empleosPorEmpresa = empleosRepository.findEmpleosByEmpresaLike("%"+buscarPorEmpresa+"%");
        mv.addObject("empleos", empleosPorEmpresa);
        return mv;
    }
    @PostMapping("/buscarPorCategoria")
    public ModelAndView buscar(@RequestParam("buscarPorCategoria") String buscarPorCategoria) {
        ModelAndView mv = new ModelAndView("/index");
        List<Empleos> empleosPorCategoria = empleosRepository.findEmpleosByCategoriaLike("%"+buscarPorCategoria+"%");
        mv.addObject("empleos", empleosPorCategoria);
        return mv;
    }
}
