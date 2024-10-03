package com.apirest.apirestfull.controller;

import java.util.List;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apirest.apirestfull.dto.Mensaje;
import com.apirest.apirestfull.dto.ProductoDto;
import com.apirest.apirestfull.entity.Producto;
import com.apirest.apirestfull.service.ProductoService;

import io.micrometer.common.util.StringUtils;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/pruducts")
@CrossOrigin(origins = "*")
public class ProductoController {
    @Autowired
    ProductoService productoService;

    @GetMapping("")
    public ResponseEntity<List<Producto>> findAll(){
        List<Producto> list = productoService.list();
        return new ResponseEntity<List<Producto>>(list, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") int id){
        if(!productoService.existsById(id))
            return new ResponseEntity<>(new Mensaje("El producto solicitado no existe"), HttpStatus.NOT_FOUND);
        Producto producto = productoService.getOne(id).get();
        return new ResponseEntity<>(producto, HttpStatus.OK);
    }

    @GetMapping("/detail-name/{name}")
    public ResponseEntity<?> getByNombre(@PathVariable("nombre") String nombre){
        if (!productoService.existsByNombre(nombre))
            return new ResponseEntity<>(new Mensaje("El producto con el nombre" + nombre + " no existe"), HttpStatus.NOT_FOUND);
        Producto producto = productoService.getByNombre(nombre).get();
        return new ResponseEntity<Producto>(producto, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Mensaje> create(@RequestBody ProductoDto productoDto){
        if(StringUtils.isBlank(productoDto.getNombre())){
            return new ResponseEntity<Mensaje>(new Mensaje("El nombre del producto es obligatorio"), HttpStatus.BAD_REQUEST);
        }else if(productoDto.getPrecio()==null || productoDto.getPrecio() < 0){ 
            return new ResponseEntity<Mensaje>(new Mensaje("El precio del producto debe ser mayor que 0.0"), HttpStatus.BAD_REQUEST);
        }else if(productoService.existsByNombre(productoDto.getNombre())){ 
            return new ResponseEntity<Mensaje>(new Mensaje("El nombre " + productoDto.getNombre() + " ya se encuentra registrado"), HttpStatus.BAD_REQUEST);
        }else{
            Producto producto = new Producto(0, productoDto.getNombre(), productoDto.getPrecio());
            productoService.create(producto);
            return new ResponseEntity<>(new Mensaje("Producto creado exitosamente"), HttpStatus.OK);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mensaje> update(@PathVariable("id") int id, @RequestBody ProductoDto productoDto){
        if(!productoService.existsById(id))
            return new ResponseEntity<Mensaje>(new Mensaje("El producto no existe"), HttpStatus.NOT_FOUND);
            
        if(productoService.existsByNombre(productoDto.getNombre()) && productoService.getByNombre(productoDto.getNombre()).get().getId() != id)
            return new ResponseEntity<Mensaje>(new Mensaje("El nombre " + productoDto.getNombre() + " ya se encuentra registrado"), HttpStatus.BAD_REQUEST);
            
        if(StringUtils.isBlank(productoDto.getNombre()))
            return new ResponseEntity<Mensaje>(new Mensaje("El nombre del producto es obligatorio"), HttpStatus.BAD_REQUEST);
            
        if(productoDto.getPrecio()==null || productoDto.getPrecio() < 0)
            return new ResponseEntity<Mensaje>(new Mensaje("El precio del producto debe ser mayor que 0.0"), HttpStatus.BAD_REQUEST);

        Producto producto = productoService.getOne(id).get();
        producto.setNombre(productoDto.getNombre());
        producto.setPrecio(productoDto.getPrecio());
        productoService.create(producto);
        return new ResponseEntity<Mensaje>(new Mensaje("Producto actulizado correctamente"), HttpStatus.OK);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Mensaje> delete(@PathVariable("id") int id){
        if(!productoService.existsById(id))
            return new ResponseEntity<Mensaje>(new Mensaje("El productoa eliminar no existe"), HttpStatus.NOT_FOUND);
        productoService.delete(id);
        return new ResponseEntity<Mensaje>(new Mensaje("Producto eliminado correctamente"), HttpStatus.OK);
        
    }            
}
