package com.practica1.Models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Concierto {

    private int entradas;
    private String nombre;


    public Concierto() {

    }

    public int getEntradas() {
        return this.entradas;
    }

    public String getNombre() {
        return this.nombre;
    }


    public void setEntradas(int entradas) {
        this.entradas = entradas;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


}