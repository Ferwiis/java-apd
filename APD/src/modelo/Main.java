package modelo;

import vista.*;
import controlador.*;

public class Main {

    public static void main(String args[]) {
        VAPD vista = new VAPD();
        VFichero lectura = new VFichero();
        Controlador_APD control = new Controlador_APD(vista, lectura);
        control.iniciarVentanaPrincipal();
    }

}
