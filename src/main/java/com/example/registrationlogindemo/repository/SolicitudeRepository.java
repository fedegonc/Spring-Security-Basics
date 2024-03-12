package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Solicitude;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudeRepository extends JpaRepository<Solicitude, Integer> {
    void findAllById(int id);
    List<Solicitude> findSolicitudeByCategoriaLike(String descripcion);

    List<Solicitude> findSolicitudeByNombreLike(String empresa);
    List<Solicitude> findSolicitudeByActivo(String estado);

}
