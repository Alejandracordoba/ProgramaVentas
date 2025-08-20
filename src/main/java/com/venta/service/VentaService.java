/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.venta.service;

import com.venta.model.Producto;
import com.venta.model.VentaItem;
import com.venta.model.VentaTotal;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VentaService {
    private final StockService stockService = StockService.getInstance();
    private final VentaTotal ventaActual = new VentaTotal();
    private final List<VentaTotal> ventasRealizadas = new ArrayList<>();

    public VentaService() {
        cargarVentasDesdeExcel();
    }

    public VentaTotal getVentaActual() { return ventaActual; }
    public List<VentaTotal> getVentasRealizadas() { return new ArrayList<>(ventasRealizadas); }

    public void agregarProductoAVenta(Producto p, int cantidad) {
        if (p == null) throw new IllegalArgumentException("Producto inválido");
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad debe ser > 0");
        if (p.getStock() < cantidad) throw new IllegalArgumentException("Stock insuficiente de " + p.getNombre());
        ventaActual.agregarItem(new VentaItem(p, cantidad));
    }

    public double confirmarVentaYRegistrar(double pago) {
        double total = ventaActual.getTotal();
        double vuelto = pago - total;

        // Crear copia de la venta para guardar en historial
        VentaTotal ventaConfirmada = new VentaTotal();
        ventaConfirmada.setPagoRecibido(pago);
        ventaConfirmada.setVuelto(vuelto);
        for (VentaItem item : ventaActual.getItems()) {
            ventaConfirmada.agregarItem(new VentaItem(item.getProducto(), item.getCantidad()));
        }
        ventasRealizadas.add(ventaConfirmada);

        // Registrar en Excel
        File excelFile = new File("inventario.xlsx");
        try (Workbook wb = excelFile.exists() ? new XSSFWorkbook(new FileInputStream(excelFile)) : new XSSFWorkbook()) {
            Sheet ventas = wb.getSheet("Ventas");
            if (ventas == null) ventas = wb.createSheet("Ventas");

            int lastRow = ventas.getLastRowNum();
            int lastTicket = 0;
            if (lastRow > 0) {
                Row row = ventas.getRow(lastRow);
                if (row != null) {
                    Cell c = row.getCell(1);
                    if (c != null && c.getCellType() == CellType.NUMERIC) {
                        lastTicket = (int) c.getNumericCellValue();
                    } else if (c != null && c.getCellType() == CellType.STRING) {
                        try { lastTicket = Integer.parseInt(c.getStringCellValue()); } catch (Exception ignored) {}
                    }
                }
            }
            int ticket = lastTicket + 1;

            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            for (VentaItem item : ventaActual.getItems()) {
                Producto pr = item.getProducto();
                Row row = ventas.createRow(++lastRow);
                row.createCell(0).setCellValue(fecha);
                row.createCell(1).setCellValue(ticket);
                row.createCell(2).setCellValue(pr.getCodigo());
                row.createCell(3).setCellValue(pr.getNombre());
                row.createCell(4).setCellValue(pr.getMarca());
                row.createCell(5).setCellValue(pr.getPresentacion());
                row.createCell(6).setCellValue(pr.getPrecio());
                row.createCell(7).setCellValue(item.getCantidad());
                row.createCell(8).setCellValue(item.getSubtotal());
                row.createCell(9).setCellValue(total);
                row.createCell(10).setCellValue(pago);
                row.createCell(11).setCellValue(vuelto);

                pr.setStock(pr.getStock() - item.getCantidad());
                stockService.actualizarProducto(pr);
            }

            for (int i = 0; i <= 11; i++) ventas.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                wb.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ventaActual.limpiar();
        return vuelto;
    }

    public void cargarVentasDesdeExcel() {
        File excelFile = new File("inventario.xlsx");
        if (!excelFile.exists()) return;

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(excelFile))) {
            Sheet ventas = wb.getSheet("Ventas");
            if (ventas == null) return;

            ventasRealizadas.clear();
            VentaTotal ventaActual = null;
            String ultimoTicket = null;

            for (int i = 1; i <= ventas.getLastRowNum(); i++) {
                Row row = ventas.getRow(i);
                if (row == null) continue;

                String ticketActual = getCellValue(row.getCell(1));
                String fecha = getCellValue(row.getCell(0));
                String codigo = getCellValue(row.getCell(2));
                double precio = getNumericCellValue(row.getCell(6));
                int cantidad = (int) getNumericCellValue(row.getCell(7));
                double pago = getNumericCellValue(row.getCell(10));
                double vuelto = getNumericCellValue(row.getCell(11));

                if (ultimoTicket == null || !ticketActual.equals(ultimoTicket)) {
                    if (ventaActual != null) {
                        ventasRealizadas.add(ventaActual);
                    }
                    ventaActual = new VentaTotal();
                    ventaActual.setId("T" + ticketActual);
                    if (fecha != null && fecha.contains(" ")) {
                        ventaActual.setFecha(fecha.split(" ")[0]);
                        ventaActual.setHora(fecha.split(" ")[1]);
                    }
                    ventaActual.setPagoRecibido(pago);
                    ventaActual.setVuelto(vuelto);
                    ultimoTicket = ticketActual;
                }

                Producto producto = stockService.buscarPorCodigo(codigo);
                if (producto != null) {
                    ventaActual.agregarItem(new VentaItem(producto, cantidad));
                }
            }

            if (ventaActual != null) {
                ventasRealizadas.add(ventaActual);
            }

        } catch (IOException e) {
            System.out.println("Error al cargar ventas: " + e.getMessage());
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    private double getNumericCellValue(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Double.parseDouble(cell.getStringCellValue()); } catch (Exception e) { return 0; }
        }
        return 0;
    }
}