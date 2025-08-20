/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.venta.model;

public class Producto {
    private String codigo;        // autoincremental "001", "002", ...
    private String nombre;
    private String marca;
    private String presentacion;  // ej: "500g", "1L"
    private double precio;
    private int stock;

    public Producto(String codigo, String nombre, String marca, String presentacion, double precio, int stock) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.marca = marca;
        this.presentacion = presentacion;
        this.precio = precio;
        this.stock = stock;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getMarca() { return marca; }
    public String getPresentacion() { return presentacion; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setMarca(String marca) { this.marca = marca; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setStock(int stock) { this.stock = stock; }
}
