package modelo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class personaDAO {

    private File archivo;
    private persona persona;

    public personaDAO(persona persona) {
        this.persona = persona;
        archivo = new File("gestionContactos");
        prepararArchivo();
    }

    private void prepararArchivo() {
        if (!archivo.exists()) {
            archivo.mkdir();
        }

        archivo = new File(archivo.getAbsolutePath(), "datosContactos.csv");
        if (!archivo.exists()) {
            try {
                archivo.createNewFile();
                String encabezado = String.format("%s;%s;%s;%s;%s", "NOMBRE", "TELEFONO", "EMAIL", "CATEGORIA", "FAVORITO");
                escribir(encabezado);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void escribir(String texto) {
        try (FileWriter escribir = new FileWriter(archivo.getAbsolutePath(), true)) {
            escribir.write(texto + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribirArchivo() {
        try {
            escribir(persona.datosContacto());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<persona> leerArchivo() throws IOException {
        StringBuilder sb = new StringBuilder();
        try (FileReader leer = new FileReader(archivo.getAbsolutePath())) {
            int c;
            while ((c = leer.read()) != -1) {
                sb.append((char) c);
            }
        }
        String contactos = sb.toString();
        String[] datos = contactos.split("\n");
        List<persona> personas = new ArrayList<>();
        for (int i = 1; i < datos.length; i++) {
            String contacto = datos[i].trim();
            if (!contacto.isEmpty()) {
                String[] partes = contacto.split(";");
                if (partes.length == 5) {
                    try {
                        persona p = new persona();
                        p.setNombre(partes[0]);
                        p.setTelefono(partes[1]);
                        p.setEmail(partes[2]);
                        p.setCategoria(partes[3]);
                        p.setFavorito(Boolean.parseBoolean(partes[4]));
                        personas.add(p);
                    } catch (Exception e) {
                        System.err.println("Error parseando línea: " + contacto);
                    }
                }
            }
        }
        return personas;
    }

    public void actualizarContactos(List<persona> personas) throws IOException {
        archivo.delete();
        archivo.createNewFile();
        try (FileWriter escribir = new FileWriter(archivo, true)) {
            escribir.write("NOMBRE;TELEFONO;EMAIL;CATEGORIA;FAVORITO\n");
            for (persona p : personas) {
                escribir.write(p.datosContacto() + "\n");
            }
        }
    }
}