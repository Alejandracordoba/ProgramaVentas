/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.venta.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.venta.model.Producto;
import com.venta.model.VentaItem;
import com.venta.model.VentaTotal;
import com.venta.service.StockService;
import com.venta.service.VentaService;
import com.venta.service.ReporteService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class GestionVentaForm extends JFrame {

    private final StockService stockService = StockService.getInstance();
    private final VentaService ventaService = new VentaService();
    private final ReporteService reporteService = new ReporteService();

    private JTextField txtCodigo, txtNombre, txtCantidad, txtPago;
    private JLabel lblTotal, lblVuelto;
    private JTable tabla;
    private DefaultTableModel modelo;

    public GestionVentaForm() {
        try { FlatLightLaf.setup(); } catch (Exception ignored) {}
        setTitle("Caja Registradora");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));
        initUI();
    }

    private void initUI() {
        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; top.add(new JLabel("Código"), g);
        g.gridx=1; txtCodigo = new JTextField(); top.add(txtCodigo, g);

        g.gridx=2; top.add(new JLabel("Nombre"), g);
        g.gridx=3; txtNombre = new JTextField(); top.add(txtNombre, g);

        g.gridx=0; g.gridy=1; top.add(new JLabel("Cantidad"), g);
        g.gridx=1; txtCantidad = new JTextField("1"); top.add(txtCantidad, g);

        g.gridx=2;
        JButton btnAgregar = new JButton("Agregar Producto");
        btnAgregar.setBackground(new Color(0,120,215));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> onAgregar());
        top.add(btnAgregar, g);

        g.gridx=3;
        JButton btnStock = new JButton("Gestionar Stock");
        btnStock.setBackground(new Color(0,120,215));
        btnStock.setForeground(Color.WHITE);
        btnStock.setFocusPainted(false);
        btnStock.addActionListener(e -> new GestionStockForm().setVisible(true));
        top.add(btnStock, g);

        g.gridx=4;
        JButton btnVentas = new JButton("Ver Ventas");
        btnVentas.setBackground(new Color(60, 180, 75));
        btnVentas.setForeground(Color.WHITE);
        btnVentas.setFocusPainted(false);
        btnVentas.addActionListener(e -> new CierreCajaForm().setVisible(true));
        top.add(btnVentas, g);

        add(top, BorderLayout.NORTH);

        modelo = new DefaultTableModel(
                new String[]{"Código","Producto","Marca","Presentación","Cantidad","Precio","Subtotal"}, 0){
            @Override public boolean isCellEditable(int row, int column){ return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0;
        lblTotal = new JLabel("TOTAL: $0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bottom.add(lblTotal, g);

        g.gridx=1; bottom.add(new JLabel("Pago Cliente"), g);
        g.gridx=2; txtPago = new JTextField(); bottom.add(txtPago, g);

        g.gridx=3;
        lblVuelto = new JLabel("VUELTO: $0.00");
        lblVuelto.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bottom.add(lblVuelto, g);

        g.gridx=4;
        JButton btnConfirmar = new JButton("Confirmar Venta");
        btnConfirmar.setBackground(new Color(0,120,215));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.addActionListener(e -> onConfirmar());
        bottom.add(btnConfirmar, g);

        add(bottom, BorderLayout.SOUTH);
        
        // En el método initUI() de GestionVentaForm:
JButton btnCierre = new JButton("Cierre de Caja");
btnCierre.setBackground(new Color(200, 100, 50));
btnCierre.setForeground(Color.WHITE);
btnCierre.setFocusPainted(false);
btnCierre.addActionListener(e -> new CierreCajaForm().setVisible(true));
    }

    private void onAgregar() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if(cantidad <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Cantidad inválida");
            return;
        }

        Producto p = !codigo.isEmpty() ? stockService.buscarPorCodigo(codigo) :
                     !nombre.isEmpty() ? stockService.buscarPorNombre(nombre) : null;

        if(p==null) { JOptionPane.showMessageDialog(this,"Producto no encontrado"); return; }
        if(p.getStock() < cantidad) { JOptionPane.showMessageDialog(this,"Stock insuficiente"); return; }

        ventaService.agregarProductoAVenta(p, cantidad);

        double subtotal = p.getPrecio()*cantidad;
        modelo.addRow(new Object[]{p.getCodigo(),p.getNombre(),p.getMarca(),p.getPresentacion(),cantidad,p.getPrecio(),subtotal});
        lblTotal.setText(String.format("TOTAL: $%.2f", ventaService.getVentaActual().getTotal()));

        txtCodigo.setText(""); txtNombre.setText(""); txtCantidad.setText("1");
    }

    private void onConfirmar() {
        double pago;
        try { pago = Double.parseDouble(txtPago.getText().trim()); }
        catch(Exception e){ JOptionPane.showMessageDialog(this,"Pago inválido"); return; }

        double total = ventaService.getVentaActual().getTotal();
        if(pago < total){ JOptionPane.showMessageDialog(this,"Pago insuficiente"); return; }

        VentaTotal ventaParaTicket = snapshotVentaActual();
        double vuelto = ventaService.confirmarVentaYRegistrar(pago);
        lblVuelto.setText(String.format("VUELTO: $%.2f",vuelto));

        String ticket = reporteService.generarTicketTexto(ventaParaTicket, pago, vuelto);
        JOptionPane.showMessageDialog(this,ticket,"Ticket",JOptionPane.INFORMATION_MESSAGE);

        modelo.setRowCount(0);
        lblTotal.setText("TOTAL: $0.00");
        txtPago.setText("");
    }

    private VentaTotal snapshotVentaActual() {
        VentaTotal copia = new VentaTotal();
        for(VentaItem it : ventaService.getVentaActual().getItems())
            copia.agregarItem(new VentaItem(it.getProducto(), it.getCantidad()));
        return copia;
    }
}