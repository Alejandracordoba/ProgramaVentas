/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.venta.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.venta.model.VentaTotal;
import com.venta.service.VentaService;
import com.venta.service.ReporteService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CierreCajaForm extends JFrame {

    private final VentaService ventaService = new VentaService();
    private final ReporteService reporteService = new ReporteService();

    private JTable tablaVentas;
    private DefaultTableModel modelo;
    private JLabel lblTotalVentas, lblCantidadVentas, lblPromedio;

    public CierreCajaForm() {
        try { FlatLightLaf.setup(); } catch (Exception ignored) {}
        setTitle("Cierre de Caja - Ventas Realizadas");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        initUI();
        cargarVentas();
    }

    private void initUI() {
        // Panel superior con botones
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel titulo = new JLabel("CIERRE DE CAJA - VENTAS REALIZADAS");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(titulo);

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.setBackground(new Color(0, 120, 215));
        btnRefrescar.setForeground(Color.WHITE);
        btnRefrescar.setFocusPainted(false);
        btnRefrescar.addActionListener(e -> cargarVentas());
        topPanel.add(btnRefrescar);

        JButton btnReporte = new JButton("Ver Reporte");
        btnReporte.setBackground(new Color(60, 180, 75));
        btnReporte.setForeground(Color.WHITE);
        btnReporte.setFocusPainted(false);
        btnReporte.addActionListener(e -> generarReporte());
        topPanel.add(btnReporte);

        JButton btnDescargar = new JButton("Descargar Reporte");
        btnDescargar.setBackground(new Color(120, 100, 200));
        btnDescargar.setForeground(Color.WHITE);
        btnDescargar.setFocusPainted(false);
        btnDescargar.addActionListener(e -> descargarReporte());
        topPanel.add(btnDescargar);

        add(topPanel, BorderLayout.NORTH);

        // Panel central con tabla
        modelo = new DefaultTableModel(new String[]{
            "ID Venta", "Fecha", "Hora", "Total", "Pago", "Vuelto", "Productos"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaVentas = new JTable(modelo);
        tablaVentas.setRowHeight(28);
        JScrollPane scrollPane = new JScrollPane(tablaVentas);
        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior con resumen de cierre
        JPanel bottomPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblCantidadVentas = new JLabel("Ventas: 0", SwingConstants.CENTER);
        lblCantidadVentas.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCantidadVentas.setOpaque(true);
        lblCantidadVentas.setBackground(new Color(220, 220, 255));
        lblCantidadVentas.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(lblCantidadVentas);

        lblTotalVentas = new JLabel("Total: $0.00", SwingConstants.CENTER);
        lblTotalVentas.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalVentas.setOpaque(true);
        lblTotalVentas.setBackground(new Color(220, 255, 220));
        lblTotalVentas.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(lblTotalVentas);

        lblPromedio = new JLabel("Promedio: $0.00", SwingConstants.CENTER);
        lblPromedio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPromedio.setOpaque(true);
        lblPromedio.setBackground(new Color(255, 220, 220));
        lblPromedio.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(lblPromedio);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void cargarVentas() {
        modelo.setRowCount(0);
        List<VentaTotal> ventas = ventaService.getVentasRealizadas();
        double totalGeneral = 0;

        for (VentaTotal venta : ventas) {
            String productos = "";
            for (int i = 0; i < Math.min(venta.getItems().size(), 2); i++) {
                if (i > 0) productos += ", ";
                productos += venta.getItems().get(i).getProducto().getNombre();
            }
            if (venta.getItems().size() > 2) {
                productos += " y " + (venta.getItems().size() - 2) + " más";
            }

            modelo.addRow(new Object[]{
                venta.getId(),
                venta.getFecha(),
                venta.getHora(),
                String.format("$%.2f", venta.getTotal()),
                String.format("$%.2f", venta.getPagoRecibido()),
                String.format("$%.2f", venta.getVuelto()),
                productos
            });

            totalGeneral += venta.getTotal();
        }

        // Actualizar resumen de cierre
        lblCantidadVentas.setText("Ventas: " + ventas.size());
        lblTotalVentas.setText("Total: $" + String.format("%.2f", totalGeneral));
        lblPromedio.setText("Promedio: $" + 
            String.format("%.2f", ventas.isEmpty() ? 0 : totalGeneral / ventas.size()));
    }

    private void generarReporte() {
        List<VentaTotal> ventas = ventaService.getVentasRealizadas();
        if (ventas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay ventas para generar reporte");
            return;
        }

        String reporte = reporteService.generarReporteVentas(ventas);
        JTextArea textArea = new JTextArea(reporte);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Reporte de Cierre de Caja", JOptionPane.INFORMATION_MESSAGE);
    }

    private void descargarReporte() {
        List<VentaTotal> ventas = ventaService.getVentasRealizadas();
        if (ventas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay ventas para descargar");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Cierre de Caja");
        fileChooser.setSelectedFile(new File("cierre_caja_" + java.time.LocalDate.now() + ".txt"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                String reporte = reporteService.generarReporteVentas(ventas);
                writer.write(reporte);
                JOptionPane.showMessageDialog(this, 
                    "Reporte de cierre descargado exitosamente:\n" + fileToSave.getAbsolutePath(),
                    "Descarga Exitosa", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al descargar el reporte: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}