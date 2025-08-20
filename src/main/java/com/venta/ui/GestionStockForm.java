/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.venta.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.venta.model.Producto;
import com.venta.service.StockService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class GestionStockForm extends JFrame {

    private final StockService stockService = StockService.getInstance();

    private JTextField txtNombre, txtMarca, txtPresentacion, txtPrecio, txtStock;
    private JTable tabla;
    private DefaultTableModel modelo;

    public GestionStockForm() {
        try { FlatLightLaf.setup(); } catch (Exception ignored) {}
        setTitle("Gestión de Stock");
        setSize(820, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        initUI();
        cargarTabla();
    }

    private void initUI() {
        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Nombre
        g.gridx=0; g.gridy=0; top.add(new JLabel("Nombre"), g);
        g.gridx=1; txtNombre = new JTextField(); top.add(txtNombre, g);

        // Marca
        g.gridx=0; g.gridy=1; top.add(new JLabel("Marca"), g);
        g.gridx=1; txtMarca = new JTextField(); top.add(txtMarca, g);

        // Presentación
        g.gridx=0; g.gridy=2; top.add(new JLabel("Presentación"), g);
        g.gridx=1; txtPresentacion = new JTextField(); top.add(txtPresentacion, g);

        // Precio
        g.gridx=2; g.gridy=0; top.add(new JLabel("Precio"), g);
        g.gridx=3; txtPrecio = new JTextField(); top.add(txtPrecio, g);

        // Stock
        g.gridx=2; g.gridy=1; top.add(new JLabel("Stock"), g);
        g.gridx=3; txtStock = new JTextField(); top.add(txtStock, g);

        // Botones
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnAgregar = estilizar(new JButton("Agregar"));
        JButton btnActualizar = estilizar(new JButton("Actualizar"));
        JButton btnEliminar = estilizarPeligro(new JButton("Eliminar"));
        JButton btnRefrescar = estilizar(new JButton("Refrescar"));
        JButton btnLimpiar = estilizar(new JButton("Limpiar"));

        btnAgregar.addActionListener(e -> onAgregar());
        btnActualizar.addActionListener(e -> onActualizar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnRefrescar.addActionListener(e -> cargarTabla());
        btnLimpiar.addActionListener(e -> limpiar());

        acciones.add(btnAgregar);
        acciones.add(btnActualizar);
        acciones.add(btnEliminar);
        acciones.add(btnRefrescar);
        acciones.add(btnLimpiar);

        g.gridx=0; g.gridy=3; g.gridwidth=4;
        top.add(acciones, g);

        add(top, BorderLayout.NORTH);

        // Tabla
        modelo = new DefaultTableModel(new String[]{"Código", "Nombre", "Marca", "Presentación", "Precio", "Stock"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        
        // Agregar listener para cargar datos al seleccionar una fila
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarDatosDesdeTabla();
            }
        });
        
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    // Método nuevo para cargar datos desde la tabla seleccionada
    private void cargarDatosDesdeTabla() {
        int row = tabla.getSelectedRow();
        if (row >= 0) {
            txtNombre.setText((String) modelo.getValueAt(row, 1));
            txtMarca.setText((String) modelo.getValueAt(row, 2));
            txtPresentacion.setText((String) modelo.getValueAt(row, 3));
            txtPrecio.setText(modelo.getValueAt(row, 4).toString());
            txtStock.setText(modelo.getValueAt(row, 5).toString());
        }
    }

    private JButton estilizar(JButton b) {
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(new Color(0,120,215));
        b.setForeground(Color.WHITE);
        return b;
    }
    private JButton estilizarPeligro(JButton b) {
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(new Color(200,60,60));
        b.setForeground(Color.WHITE);
        return b;
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        List<Producto> list = stockService.getProductos();
        for (Producto p : list) {
            modelo.addRow(new Object[]{
                    p.getCodigo(), p.getNombre(), p.getMarca(), p.getPresentacion(), p.getPrecio(), p.getStock()
            });
        }
    }

    private void onAgregar() {
        String nombre = txtNombre.getText().trim();
        String marca = txtMarca.getText().trim();
        String present = txtPresentacion.getText().trim();
        int stock;
        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim());
            stock  = Integer.parseInt(txtStock.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Precio/Stock inválidos");
            return;
        }
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre es requerido");
            return;
        }

        String codigo = stockService.getProximoCodigo();
        Producto p = new Producto(codigo, nombre, marca, present, precio, stock);
        stockService.agregarProducto(p);
        cargarTabla();
        limpiar();
    }

    private void onActualizar() {
        int row = tabla.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná un producto en la tabla");
            return;
        }
        String codigo = (String) modelo.getValueAt(row, 0);

        String nombre = txtNombre.getText().trim();
        String marca = txtMarca.getText().trim();
        String present = txtPresentacion.getText().trim();
        int stock;
        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim());
            stock  = Integer.parseInt(txtStock.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Precio/Stock inválidos");
            return;
        }

        Producto p = new Producto(codigo, nombre, marca, present, precio, stock);
        stockService.actualizarProducto(p);
        cargarTabla();
        limpiar();
    }

    private void onEliminar() {
        int row = tabla.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná un producto en la tabla");
            return;
        }
        String codigo = (String) modelo.getValueAt(row, 0);
        stockService.eliminarProducto(codigo);
        cargarTabla();
        limpiar(); // Limpiar campos después de eliminar
    }

    private void limpiar() {
        txtNombre.setText(""); 
        txtMarca.setText(""); 
        txtPresentacion.setText("");
        txtPrecio.setText(""); 
        txtStock.setText("");
        tabla.clearSelection(); // Deseleccionar fila
    }
}




