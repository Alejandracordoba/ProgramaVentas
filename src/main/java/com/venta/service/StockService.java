/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * Autor: Alejandra Córdoba

 * Proyecto: GestorTurnos
 
 */

package com.venta.service;

import com.venta.model.Producto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StockService {

    private static StockService INSTANCE;

    private final List<Producto> productos = new ArrayList<>();
    private final File baseDir;
    private final File excelFile;

    private static final String FILE_NAME = "Datos.xlsx";
    private static final String HOJA_STOCK = "Stock";
    private static final String HOJA_VENTAS = "Ventas";

    private StockService() {
        baseDir = new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "AlmacenVenta");
        if (!baseDir.exists()) baseDir.mkdirs();
        excelFile = new File(baseDir, FILE_NAME);
        inicializarArchivo();
        cargarDesdeExcel();
    }

    public static synchronized StockService getInstance() {
        if (INSTANCE == null) INSTANCE = new StockService();
        return INSTANCE;
    }

    /* ===================== PÚBLICO ====================== */

    public synchronized List<Producto> getProductos() {
        return new ArrayList<>(productos);
    }

    public synchronized void agregarProducto(Producto p) {
        productos.add(p);
        guardarStock();
    }

    public synchronized void actualizarProducto(Producto p) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getCodigo().equalsIgnoreCase(p.getCodigo())) {
                productos.set(i, p);
                break;
            }
        }
        guardarStock();
    }

    public synchronized void eliminarProducto(String codigo) {
        productos.removeIf(x -> x.getCodigo().equalsIgnoreCase(codigo));
        guardarStock();
    }

    public synchronized Producto buscarPorCodigo(String codigo) {
        return productos.stream()
                .filter(p -> p.getCodigo().equalsIgnoreCase(codigo))
                .findFirst().orElse(null);
    }

    public synchronized Producto buscarPorNombre(String nombre) {
        return productos.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(nombre))
                .findFirst().orElse(null);
    }

    public synchronized String getProximoCodigo() {
        int max = 0;
        for (Producto p : productos) {
            try {
                int n = Integer.parseInt(p.getCodigo());
                if (n > max) max = n;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("%03d", max + 1);
    }

    public File getExcelFile() { return excelFile; }

    public boolean isExcelAbierto() {
        try (RandomAccessFile raf = new RandomAccessFile(excelFile, "rw")) {
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /* ===================== EXCEL ====================== */

    private void inicializarArchivo() {
        if (excelFile.exists()) return;

        try (Workbook wb = new XSSFWorkbook()) {
            // Hoja Stock
            Sheet stock = wb.createSheet(HOJA_STOCK);
            Row h1 = stock.createRow(0);
            String[] colsStock = {"Código", "Nombre", "Marca", "Presentación", "Precio", "Stock"};
            for (int i = 0; i < colsStock.length; i++) h1.createCell(i).setCellValue(colsStock[i]);

            // Hoja Ventas
            Sheet ventas = wb.createSheet(HOJA_VENTAS);
            Row h2 = ventas.createRow(0);
            String[] colsVentas = {"FechaHora", "Ticket", "Código", "Nombre", "Marca", "Presentación",
                    "PrecioUnit", "Cantidad", "Subtotal", "TotalVenta", "Pago", "Vuelto"};
            for (int i = 0; i < colsVentas.length; i++) h2.createCell(i).setCellValue(colsVentas[i]);

            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                wb.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void cargarDesdeExcel() {
        if (!excelFile.exists()) return;
        productos.clear();

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(HOJA_STOCK);
            if (sheet == null) return;

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String codigo = getString(row.getCell(0));
                String nombre = getString(row.getCell(1));
                String marca = getString(row.getCell(2));
                String presentacion = getString(row.getCell(3));
                double precio = getDouble(row.getCell(4));
                int stock = (int) getDouble(row.getCell(5));

                if (codigo == null || codigo.isBlank()) continue;
                productos.add(new Producto(codigo, nombre, marca, presentacion, precio, stock));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void guardarStock() {
        // Avisar si Excel está abierto pero no bloquear
        if (isExcelAbierto()) {
            JOptionPane.showMessageDialog(null,
                    "El archivo Excel está abierto.\nCierre Excel para actualizar el stock.",
                    "Archivo bloqueado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Workbook wb = excelFile.exists() ? new XSSFWorkbook(new FileInputStream(excelFile)) : new XSSFWorkbook()) {

            // Asegurar hojas
            Sheet stock = wb.getSheet(HOJA_STOCK);
            if (stock == null) stock = wb.createSheet(HOJA_STOCK);
            Sheet ventas = wb.getSheet(HOJA_VENTAS);
            if (ventas == null) ventas = wb.createSheet(HOJA_VENTAS);

            // Limpiar hoja stock
            for (int i = stock.getLastRowNum(); i >= 0; i--) {
                Row row = stock.getRow(i);
                if (row != null) stock.removeRow(row);
            }

            // Cabecera
            Row h = stock.createRow(0);
            String[] cols = {"Código", "Nombre", "Marca", "Presentación", "Precio", "Stock"};
            for (int i = 0; i < cols.length; i++) h.createCell(i).setCellValue(cols[i]);

            // Data
            int r = 1;
            for (Producto p : productos) {
                Row row = stock.createRow(r++);
                row.createCell(0).setCellValue(p.getCodigo());
                row.createCell(1).setCellValue(p.getNombre());
                row.createCell(2).setCellValue(p.getMarca());
                row.createCell(3).setCellValue(p.getPresentacion());
                row.createCell(4).setCellValue(p.getPrecio());
                row.createCell(5).setCellValue(p.getStock());
            }

            for (int i = 0; i < cols.length; i++) stock.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                wb.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ===================== Utils ====================== */
    private static String getString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private static double getDouble(Cell cell) {
        if (cell == null) return 0d;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try { yield Double.parseDouble(cell.getStringCellValue()); } catch (Exception e) { yield 0d; }
            }
            default -> 0d;
        };
    }
}
