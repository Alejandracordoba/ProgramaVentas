/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.venta.service;

import com.venta.model.VentaItem;
import com.venta.model.VentaTotal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteService {
    public String generarTicketTexto(VentaTotal venta, double pago, double vuelto) {
        StringBuilder sb = new StringBuilder();
        sb.append("====== TICKET ======\n");
        sb.append("Fecha: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        sb.append(String.format("%-6s %-18s %5s %8s %10s\n", "COD", "DESCRIPCIÓN", "CANT", "P.UNIT", "SUBTOT"));
        sb.append("------------------------------------------------------\n");
        venta.getItems().forEach(it -> {
            sb.append(String.format("%-6s %-18s %5d %8.2f %10.2f\n",
                    it.getProducto().getCodigo(),
                    it.getProducto().getNombre(),
                    it.getCantidad(),
                    it.getProducto().getPrecio(),
                    it.getSubtotal()));
        });
        sb.append("------------------------------------------------------\n");
        sb.append(String.format("TOTAL: $%.2f\n", venta.getTotal()));
        sb.append(String.format("PAGO : $%.2f\n", pago));
        sb.append(String.format("VUELTO: $%.2f\n", vuelto));
        sb.append("====================\n");
        return sb.toString();
    }

    public String generarReporteVentas(List<VentaTotal> ventas) {
        StringBuilder reporte = new StringBuilder();
        reporte.append("REPORTE DE VENTAS\n");
        reporte.append("================\n");
        reporte.append("Generado: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        
        double totalGeneral = 0;
        for (VentaTotal venta : ventas) {
            reporte.append(String.format("VENTA ID: %s\n", venta.getId()));
            reporte.append(String.format("Fecha: %s - Hora: %s\n", venta.getFecha(), venta.getHora()));
            reporte.append(String.format("Total: $%.2f\n", venta.getTotal()));
            reporte.append(String.format("Pago: $%.2f - Vuelto: $%.2f\n", venta.getPagoRecibido(), venta.getVuelto()));
            reporte.append("Productos:\n");
            
            for (VentaItem item : venta.getItems()) {
                reporte.append(String.format("  - %s (x%d): $%.2f\n", 
                    item.getProducto().getNombre(), 
                    item.getCantidad(), 
                    item.getSubtotal()));
            }
            reporte.append("----------------\n");
            totalGeneral += venta.getTotal();
        }
        
        reporte.append("\nRESUMEN GENERAL\n");
        reporte.append("===============\n");
        reporte.append(String.format("Total de ventas: %d\n", ventas.size()));
        reporte.append(String.format("Ingreso total: $%.2f\n", totalGeneral));
        reporte.append(String.format("Promedio por venta: $%.2f\n", ventas.isEmpty() ? 0 : totalGeneral / ventas.size()));
        
        return reporte.toString();
    }
}
