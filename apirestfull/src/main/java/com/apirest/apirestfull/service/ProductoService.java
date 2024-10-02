package com.apirest.apirestfull.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apirest.apirestfull.entity.Producto;
import com.apirest.apirestfull.repository.ProductoRepository;

@Service
@Transactional
public class ProductoService {
    @Autowired
    ProductoRepository productoRepository;

    public List<Producto> list(){
        return productoRepository.findAll();
    } 

    public Optional<Producto> getOne(int id){
        return productoRepository.findById(id);
    }

    public boolean existsById(int id){
        return productoRepository.existsById(id);
    }
    
    public Optional<Producto> getByNombre(String nombre){
        return productoRepository.findByNombre(nombre);
    }
    
    public boolean existsByNombre(String nombre){
        return productoRepository.existsByNombre(nombre);
    }

    public void create(Producto producto){
        productoRepository.save(producto);
    }

    public void delete(int id) {
        productoRepository.deleteById(id);
    }

}
