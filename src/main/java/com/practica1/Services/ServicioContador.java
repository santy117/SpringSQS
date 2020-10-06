package com.practica1.Services;

import org.springframework.stereotype.Service;

@Service
public class ServicioContador {
    int contador=0;

    public void incContador() {
        this.contador = this.contador+1;
    }

    public int getContador(){
        return contador;
    }
}
