/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.venta.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VentaTotal {
    private final List<VentaItem> items = new ArrayList<>();
    private String id;
    private String fecha;
    private String hora;
    private double pagoRecibido;
    private double vuelto;
    
    public VentaTotal() {
        this.id = "V" + System.currentTimeMillis();
        this.fecha = LocalDate.now().toString();
        this.hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public void agregarItem(VentaItem item) { items.add(item); }
    public List<VentaItem> getItems() { return items; }

    public double getTotal() {
        return items.stream().mapToDouble(VentaItem::getSubtotal).sum();
    }

    public void limpiar() { items.clear(); }
    
    public String getId() { return id; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public double getPagoRecibido() { return pagoRecibido; }
    public double getVuelto() { return vuelto; }
    
    public void setPagoRecibido(double pagoRecibido) { this.pagoRecibido = pagoRecibido; }
    public void setVuelto(double vuelto) { this.vuelto = vuelto; }
    public void setId(String id) { this.id = id; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setHora(String hora) { this.hora = hora; }
}