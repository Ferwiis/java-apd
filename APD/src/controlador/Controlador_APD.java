package controlador;

import static javax.swing.WindowConstants.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import modelo.*;
import vista.*;

public final class Controlador_APD implements ActionListener {

    private final VAPD vista;
    private final VFichero lectura;
    private APD tupla;
    private String cadena_entrada;
    private boolean post_precargado = false;
    private int cont = 0;

    public Controlador_APD(VAPD vista, VFichero lectura) {
        this.vista = vista;
        this.lectura = lectura;
        registrarOyentes();
    }

    public void registrarOyentes() {
        this.vista.jbCargar.addActionListener(this);
        this.vista.jbComputar.addActionListener(this);
        this.lectura.jbBuscar.addActionListener(this);
        this.lectura.jbContinuar.addActionListener(this);
        this.lectura.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                post_precargado = true;
                cont = 1;
            }
        });
    }

    public void iniciarVentanaPrincipal() {
        vista.setDefaultCloseOperation(EXIT_ON_CLOSE);
        vista.setLocationRelativeTo(null);
        vista.txTupla.setEditable(false);
        vista.txComputos.setEditable(false);
        vista.setResizable(false);
        vista.setVisible(true);
    }

    public void iniciarVentanaLecturaArchivo() {
        lectura.setLocationRelativeTo(null);
        lectura.txTupla.setEditable(false);
        lectura.txDirArchivo.setEditable(false);
        lectura.setResizable(false);
        lectura.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        if (post_precargado == false) {
            lectura.txTupla.setText("({0,1,2}, {a,b,c}, {X,Y,$}, {0}, {$}, {2})\n"
                    + "delta:\n"
                    + "0,0: b,X/YX; a,Y/XY; b,Y/YY; a,X/XX; b,$/Y$; a,$/X$;\n"
                    + "0,1: c,Y/Y; c,X/X; c,$/$;\n"
                    + "1,1: b,Y/Ɛ; a,X/Ɛ;\n"
                    + "1,2: Ɛ,$/$;");
            lectura.jbBuscar.setEnabled(false);
            lectura.jTitulo.setText("Autómata precargado \n- (Palíndromo)");
            lectura.jbContinuar.setEnabled(true);
            post_precargado = true;
        } else {
            lectura.txTupla.setText("");
            lectura.jbContinuar.setEnabled(false);
            lectura.jbBuscar.setEnabled(true);
            lectura.jTitulo.setText("Creación del APD");
        }
        lectura.setVisible(true);
    }

    public void generarTupla() {
        String contenido = lectura.txTupla.getText();
        if (cont == 0) {
            try {
                lectura.seleccionar.setFileFilter(lectura.filtro);
                if (lectura.seleccionar.showDialog(null, "Guardar") == JFileChooser.APPROVE_OPTION) {
                    vista.txTupla.setText(contenido);
                    File precargado = lectura.seleccionar.getSelectedFile();
                    if (!precargado.exists()) {
                        precargado.createNewFile();
                    } else {
                        String nr = precargado.getAbsolutePath();
                        for (int i = 1; i < Long.MAX_VALUE; i++) {
                            precargado = new File(nr + " (" + i + ")");
                            if (!precargado.exists()) {
                                precargado.createNewFile();
                                break;
                            }
                        }
                    }
                    FileWriter fw = new FileWriter(precargado);
                    try (BufferedWriter bw = new BufferedWriter(fw)) {
                        bw.write(contenido);
                    }
                    tupla = new APD(precargado);
                    String cadenaPrueba = "abcba";
                    vista.txCadenaEntrada.setText(cadenaPrueba);
                    //Validacion con if-else del formato del .txt
                    cadena_entrada = vista.txCadenaEntrada.getText();
                    lectura.dispose();
                    generarComputos();
                    cont = 1;
                } else {
                    for (Window window : Window.getWindows()) {
                        if (vista != window) {
                            window.toFront();
                            return;
                        }
                    }
                }
            } catch (HeadlessException | IOException e) {
                e.printStackTrace();
            }
        } else {
            vista.txTupla.setText(contenido);
            vista.txComputos.setText("");
            tupla = new APD(lectura.archivo);
            lectura.dispose();
        }
        vista.setDefaultCloseOperation(EXIT_ON_CLOSE);
        vista.setLocationRelativeTo(null);
        vista.txTupla.setEditable(false);
        vista.txComputos.setEditable(false);
        vista.setResizable(false);
        vista.setVisible(true);
    }

    public void generarComputos() {
        String info = vista.txTupla.getText();
        if (!info.equals("")) {
            cadena_entrada = vista.txCadenaEntrada.getText();
            if (tupla.generarComputos(cadena_entrada.concat("E")) == false) {
                JOptionPane.showMessageDialog(vista, "¡Cadena rechazada!");
            } else {
                JOptionPane.showMessageDialog(vista, "¡Cadena aceptada!");
            }
            vista.txComputos.setText(tupla.getImpresor());
        } else {
            JOptionPane.showMessageDialog(vista, "¡No hay un APD para ejecutar computaciones! Por favor cárguelo.");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(lectura.jbContinuar)) {
            generarTupla();
        }
        if (e.getSource().equals(vista.jbCargar)) {
            iniciarVentanaLecturaArchivo();
        }
        if (e.getSource().equals(vista.jbComputar)) {
            generarComputos();
        }
    }
}
