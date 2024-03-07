package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Solicitude;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface solicitudeRepository extends JpaRepository<Solicitude, Integer> {
    void findAllById(int id);
    List<Solicitude> findEmpleosByCategoriaLike(String descripcion);

    List<Solicitude> findEmpleosByEmpresaLike(String empresa);
    List<Solicitude> findEmpleosByActivo(String estado);

}
