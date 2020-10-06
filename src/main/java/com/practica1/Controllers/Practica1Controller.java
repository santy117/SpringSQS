package com.practica1.Controllers;

import com.practica1.Services.ServicioContador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Practica1Controller {
    @Autowired
    ServicioContador servicioContador;
    @GetMapping("/hello")
    public String sayHello(@RequestParam(value = "myName", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    @GetMapping("/contador")
    public String contador() {
        servicioContador.incContador();
        String cont= String.valueOf(servicioContador.getContador());
        return "El contador es"+cont;
    }
}
