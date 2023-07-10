package modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;

public final class APD {

    private char[] ConjQ;
    private char[] ConjSigma;
    private char[] ConjAlfPila;
    private char[] ConjEstadosFinales;
    private List<String> delta = new ArrayList<>();
    private String estado_inicial;
    private char SimboloFondoPila;
    private Stack<String> pila = new Stack<>();
    private String textofichero;
    private String[][] automata = null;
    private File ruta;
    private String estados;
    private String impresor;

    public APD(File r) {
        try {
            leerTupla(r);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getImpresor() {
        return this.impresor;
    }

    public void leerTupla(File r) throws FileNotFoundException, IOException {
        ruta = new File(r.getAbsolutePath());
        FileReader leerFichero = new FileReader(ruta);
        try (BufferedReader bufferLectura = new BufferedReader(leerFichero)) {
            String cadenaElement;
            int linea = 0;
            while ((textofichero = bufferLectura.readLine()) != null) {
                if (textofichero.matches("#.*")) {
                } else if (textofichero.matches("\b*")) {
                } else {
                    if (linea >= 2) { //
                        String[] separarEspaciosDelta = textofichero.split(" ");
                        for (String espacio : separarEspaciosDelta) {
                            delta.add(espacio);
                        }
                    } else {
                        String[] separarEspacios = textofichero.split(" ");
                        for (String espacio : separarEspacios) {
                            if (espacio.matches("#.*")) {
                                break;
                            }
                        }
                        if (linea == 0) {
                            for (int i = 0; i < separarEspacios.length; i++) {
                                cadenaElement = separarEspacios[i];
                                cadenaElement = ajustarCaracteres(cadenaElement, i);
                                switch (i) {
                                    case 0: //
                                        ConjQ = cadenaElement.toCharArray();
                                        automata = new String[ConjQ.length][ConjQ.length];
                                        estados = ConjQ.toString();
                                        break;
                                    case 1:
                                        ConjSigma = cadenaElement.toCharArray();
                                        break;
                                    case 2:
                                        ConjAlfPila = cadenaElement.toCharArray();
                                        break;
                                    case 3:
                                        estado_inicial = cadenaElement.substring(0, 1);
                                        break;
                                    case 4:
                                        SimboloFondoPila = cadenaElement.charAt(0);
                                        break;
                                    case 5:
                                        ConjEstadosFinales = cadenaElement.toCharArray();
                                        break;
                                }
                            }
                        }

                    }
                    linea++;
                }

            }
        }
        asignarTransiciones();
    }

    private String ajustarCaracteres(String cadena, int indice) {
        cadena = cadena.replace("{", "");
        cadena = cadena.replace("}", "");
        cadena = cadena.replace(",", "");
        switch (indice) {
            case 0:
                cadena = cadena.replace("(", "");
                break;
            case 3:
                cadena = cadena.replace(")", "");
                break;
        }
        return cadena;
    }

    private boolean validarNumero(String cadena) {
        boolean resultado;
        try {
            Integer.parseInt(cadena);
            resultado = true;
        } catch (NumberFormatException e) {
            resultado = false;
        }
        return resultado;
    }

    private void asignarTransiciones() {
        int fila = 0, columna = 0;
        String f, c, transiciones_concatenadas = null;
        for (int i = 0; i < delta.size(); i++) {
            String cadena = delta.get(i);
            if (cadena.endsWith(":")) {
                transiciones_concatenadas = "";
                for (int j = 0; j < cadena.length() - 1; j++) {
                    if (cadena.substring(j, j + 1).equals(",")) {
                        f = cadena.substring(0, j);
                        c = cadena.substring(j + 1, cadena.length() - 1);
                        if ((validarNumero(f)) && (validarNumero(c))) {
                            fila = Integer.parseInt(f);
                            columna = Integer.parseInt(c);
                            transiciones_concatenadas += cadena + " ";
                            break;
                        }
                    }
                }
            } else if (cadena.endsWith(";")) {
                transiciones_concatenadas += cadena + " ";
                automata[fila][columna] = transiciones_concatenadas;
            }
        }
    }

    private boolean verificarEstadoFinal(int estadoActual) {
        for (int i = 0; i < ConjEstadosFinales.length; i++) {
            if (estadoActual == Character.getNumericValue(ConjEstadosFinales[i])) {
                return true;
            }
        }
        return false;
    }

    private void generarImpresion(String[] transiciones, char caracter, int estadoActual, int k, int l) {
        int cons = k + 1;
        impresor += "        |       " + cons + "       |        |       " + caracter + "       |      " + estadoActual + "      |    " + transiciones[l] + "     |\n";
        for (int i = pila.size() - 1; i > -1; i--) {
            impresor += "                         |   " + pila.elementAt(i) + "   |                \n";
        }
    }

    public boolean generarComputos(String cadena_entrada) {
        pila.clear();
        pila.push(Character.toString(SimboloFondoPila));
        impresor = "";
        int estadoActual = Integer.parseInt(estado_inicial);
        int estadoDirigido = 0;
        int nTrans;
        String transicionesNoEjecutadas;
        boolean warning = false;
        boolean exit;
        impresor += "        | Iteración | Pila | Caracter | Estado | Transiciones |\n";
        for (int k = 0; k < cadena_entrada.length(); k++) {
            if (warning == false) {
                char caracter = cadena_entrada.charAt(k);
                for (int i = estadoActual; i < automata.length; i++) {
                    exit = false;
                    transicionesNoEjecutadas = null;
                    nTrans = 0;
                    for (int j = estadoDirigido; j < automata.length; j++) {
                        if (automata[i][j] != null) {
                            String[] transiciones = automata[i][j].split(" ");
                            if ((Integer.parseInt(Character.toString(transiciones[0].charAt(0))) == estadoActual) && (i == estadoActual)) {
                                nTrans += transiciones.length - 1;
                                for (int l = 1; l < transiciones.length; l++) {
                                    if (exit) {
                                        break;
                                    } else {
                                        if ((caracter == transiciones[l].charAt(0)) && (pila.peek().equals(Character.toString(transiciones[l].charAt(2))))) {
                                            String funcion = transiciones[l];
                                            funcion = funcion.replace(funcion.substring(0, 2), "");
                                            if (funcion.length() == 5) {
                                                if (Character.toString(funcion.charAt(4)).equals(";")) {
                                                    if (Character.toString(funcion.charAt(0)).equals(Character.toString(funcion.charAt(3)))) {
                                                        pila.push(Character.toString(funcion.charAt(2)));
                                                        i = j;
                                                        j = 0;
                                                        estadoActual = i;
                                                        estadoDirigido = j;
                                                        exit = true;
                                                        generarImpresion(transiciones, caracter, estadoActual, k, l);
                                                    }
                                                }
                                            } else if (funcion.length() == 4) {
                                                if (Character.toString(funcion.charAt(3)).equals(";")) {
                                                    if (Character.toString(funcion.charAt(0)).equals(Character.toString(funcion.charAt(2)))) {
                                                        i = j;
                                                        j = 0;
                                                        estadoActual = i;
                                                        estadoDirigido = j;
                                                        exit = true;
                                                    } else if (Character.toString(funcion.charAt(2)).equals("Ɛ")) {
                                                        if (!pila.peek().equals(Character.toString(SimboloFondoPila))) {
                                                            pila.pop();
                                                        }
                                                        i = j;
                                                        j = 0;
                                                        estadoActual = i;
                                                        estadoDirigido = j;
                                                        exit = true;
                                                    }
                                                    generarImpresion(transiciones, caracter, estadoActual, k, l);
                                                }
                                            }
                                        } else {
                                            transicionesNoEjecutadas += transiciones[l] + " ";
                                        }
                                    }
                                }
                            } else {
                                exit = true;
                            }
                        }
                        if (exit) {
                            break;
                        }
                    }
                    if (transicionesNoEjecutadas != null) {   //Aplica esta sentencia cuando ninguna transición desde el estadoActual hacia el estadoDirigido contiene el caracter en lectura, por lo cual de forma inmediata se terminarán los cómputos y la cadena será rechazada
                        String[] fallidas = transicionesNoEjecutadas.split(" ");
                        if (fallidas.length == nTrans) {
                            exit = true;
                            warning = true;
                        }
                    }
                    if (exit) {
                        break;
                    }
                }
            }
            if (k == cadena_entrada.length() - 1) {
                if (warning == true) {
                    return false;
                } else if ((pila.peek().equals(Character.toString(SimboloFondoPila))) && (verificarEstadoFinal(estadoActual))) {
                    return true;
                }
            }
        }
        return false;
    }
}
