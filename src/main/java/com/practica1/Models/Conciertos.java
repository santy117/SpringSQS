package com.practica1.Models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Conciertos {
    ArrayList< Concierto > conciertos = new ArrayList < Concierto> ();
    public Conciertos(){

    }
    public String getNombreConcierto(int i){
        return this.conciertos.get(i).getNombre();
    }
    public int getTicketsConcierto(int i){
        return this.conciertos.get(i).getEntradas();
    }
    public void setTicketsConcierto(int i,int nuevoValor){
        this.conciertos.get(i).setEntradas(nuevoValor);
    }
    public int getLength(){
        return conciertos.size();
    }

}
