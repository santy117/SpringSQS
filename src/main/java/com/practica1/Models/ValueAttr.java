package com.practica1.Models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValueAttr {

    private long idCompra;
    private String concierto;
    private String tickets;

    public ValueAttr() {

    }

    public long getIdCompra() {
        return this.idCompra;
    }//getIdDispositivo

    public String getConcierto() {
        return this.concierto;
    }//getFecha

    public String getTickets() {
        return this.tickets;
    }//getHora

    public void setIdCompra(long id) {
        this.idCompra = id;
    }//setIdDispositivo

    public void setConcierto(String concierto) {
        this.concierto = concierto;
    }//setFecha

    public void setTickets(String tickets) {
        this.tickets = tickets;
    }//setHora

    @Override
    public String toString() {
        return "Value{" +
                "idCompra=" + idCompra +
                ", concierto='" + concierto + '\'' +
                ", tickets='" + tickets + '\'' +
                '}';
    }//toString

}