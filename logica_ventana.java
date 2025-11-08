package controlador;

import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import vista.ventana;
import modelo.*;

public class logica_ventana implements ActionListener, ListSelectionListener, ItemListener {

    private ventana delegado;
    private String nombres, email, telefono, categoria = "";
    private persona persona;
    private List<persona> contactos;
    private boolean favorito = false;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    public logica_ventana(ventana delegado) {
        this.delegado = delegado;
        // Asignar referencia del controlador en la vista
        this.delegado.controlador = this;

        this.tableModel = (DefaultTableModel) delegado.tbl_contactos.getModel();
        this.sorter = new TableRowSorter<>(tableModel);
        delegado.tbl_contactos.setRowSorter(sorter);
        cargarContactosRegistrados();

        delegado.btn_add.addActionListener(this);
        delegado.btn_eliminar.addActionListener(this);
        delegado.btn_modificar.addActionListener(this);
        delegado.cmb_categoria.addItemListener(this);
        delegado.chb_favorito.addItemListener(this);
        delegado.btn_exportar.addActionListener(this);

        delegado.tbl_contactos.getSelectionModel().addListSelectionListener(this);

        delegado.tbl_contactos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { if (e.isPopupTrigger()) delegado.popupMenu.show(e.getComponent(), e.getX(), e.getY()); }
            @Override public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) delegado.popupMenu.show(e.getComponent(), e.getX(), e.getY()); }
        });

        delegado.menuItemEliminar.addActionListener(this);
        delegado.menuItemModificar.addActionListener(this);

        delegado.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_A) actionPerformed(new ActionEvent(delegado.btn_add, ActionEvent.ACTION_PERFORMED, null));
                    else if (e.getKeyCode() == KeyEvent.VK_E) actionPerformed(new ActionEvent(delegado.btn_eliminar, ActionEvent.ACTION_PERFORMED, null));
                    else if (e.getKeyCode() == KeyEvent.VK_M) actionPerformed(new ActionEvent(delegado.btn_modificar, ActionEvent.ACTION_PERFORMED, null));
                }
            }
        });
        delegado.setFocusable(true);

        delegado.txt_buscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { buscarContactos(); }
            @Override public void removeUpdate(DocumentEvent e) { buscarContactos(); }
            @Override public void changedUpdate(DocumentEvent e) { buscarContactos(); }
        });
    }

    // Método público invocado por la vista al cambiar idioma para forzar actualización de textos y gráfica
    public void onLanguageChanged() {
        // recalcula estadísticas usando las nuevas cadenas del bundle y fuerza redibujado
        actualizarEstadisticas();
    }

    private void incializacionCampos() {
        nombres = delegado.txt_nombres.getText();
        email = delegado.txt_email.getText();
        telefono = delegado.txt_telefono.getText();
        Object sel = delegado.cmb_categoria.getSelectedItem();
        categoria = sel != null ? sel.toString() : "";
        favorito = delegado.chb_favorito.isSelected();
    }

    private void cargarContactosRegistrados() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                try {
                    contactos = new personaDAO(new persona()).leerArchivo();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(delegado, getStringSafe("msg.load_failed", "No se pudieron cargar los contactos"));
                }
                return null;
            }
            @Override protected void done() {
                tableModel.setRowCount(0);
                if (contactos != null) {
                    for (persona contacto : contactos) {
                        tableModel.addRow(new Object[]{
                            contacto.getNombre(),
                            contacto.getTelefono(),
                            contacto.getEmail(),
                            contacto.getCategoria(),
                            contacto.isFavorito()
                        });
                    }
                }
                actualizarEstadisticas();
                delegado.progressBar.setValue(100);
            }
        };
        delegado.progressBar.setValue(0);
        worker.execute();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        incializacionCampos();
        if (e.getSource() == delegado.btn_add) {
            if (!nombres.isEmpty() && !telefono.isEmpty() && !email.isEmpty()) {
                if (!categoria.equals(getStringSafe("category.select", "Seleccione una Categoria")) && !categoria.isEmpty()) {
                    persona = new persona(nombres, telefono, email, categoria, favorito);
                    if (new personaDAO(persona).escribirArchivo()) {
                        limpiarCampos();
                        JOptionPane.showMessageDialog(delegado, getStringSafe("msg.contact_registered", "El Contacto ha sido Registrado!!!"));
                    } else {
                        JOptionPane.showMessageDialog(delegado, getStringSafe("msg.save_failed", "No se pudo guardar todos los archivos. Intentelo de nuevo"));
                    }
                } else {
                    JOptionPane.showMessageDialog(delegado, getStringSafe("msg.select_category", "Seleccione una Categoria!!!"));
                }
            } else {
                JOptionPane.showMessageDialog(delegado, getStringSafe("msg.fill_all_fields", "Deben estar llenados todos los campos!!!"));
            }
        } else if (e.getSource() == delegado.btn_eliminar || e.getSource() == delegado.menuItemEliminar) {
            eliminarContacto();
        } else if (e.getSource() == delegado.btn_modificar || e.getSource() == delegado.menuItemModificar) {
            modificarContacto();
        } else if (e.getSource() == delegado.btn_exportar) {
            exportarCSV();
        }
    }

    private void limpiarCampos() {
        delegado.txt_nombres.setText("");
        delegado.txt_telefono.setText("");
        delegado.txt_email.setText("");
        delegado.cmb_categoria.setSelectedIndex(0);
        delegado.chb_favorito.setSelected(false);
        incializacionCampos();
        cargarContactosRegistrados();
    }

    private void eliminarContacto() {
        int row = delegado.tbl_contactos.getSelectedRow();
        if (row != -1) {
            int modelRow = delegado.tbl_contactos.convertRowIndexToModel(row);
            int confirm = JOptionPane.showConfirmDialog(delegado, getStringSafe("msg.confirm_delete", "¿Estás seguro de eliminar este contacto?"), getStringSafe("msg.confirm_title", "Confirmar eliminación"), JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                contactos.remove(modelRow);
                try {
                    new personaDAO(new persona()).actualizarContactos(contactos);
                    cargarContactosRegistrados();
                    limpiarCampos();
                    JOptionPane.showMessageDialog(delegado, getStringSafe("msg.contact_deleted", "El Contacto ha sido eliminado"));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(delegado, getStringSafe("msg.delete_failed", "Error al eliminar el contacto"));
                }
            }
        } else {
            JOptionPane.showMessageDialog(delegado, getStringSafe("msg.select_to_delete", "Seleccione un contacto para eliminar"));
        }
    }

    private void modificarContacto() {
        int row = delegado.tbl_contactos.getSelectedRow();
        if (row != -1) {
            int modelRow = delegado.tbl_contactos.convertRowIndexToModel(row);
            incializacionCampos();
            if (!nombres.isEmpty() && !telefono.isEmpty() && !email.isEmpty()) {
                if (!categoria.equals(getStringSafe("category.select", "Seleccione una Categoria")) && !categoria.isEmpty()) {
                    contactos.get(modelRow).setNombre(nombres);
                    contactos.get(modelRow).setTelefono(telefono);
                    contactos.get(modelRow).setEmail(email);
                    contactos.get(modelRow).setCategoria(categoria);
                    contactos.get(modelRow).setFavorito(favorito);
                    try {
                        new personaDAO(new persona()).actualizarContactos(contactos);
                        cargarContactosRegistrados();
                        limpiarCampos();
                        JOptionPane.showMessageDialog(delegado, getStringSafe("msg.contact_modified", "El Contacto ha sido modificado!!!"));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(delegado, getStringSafe("msg.modify_failed", "Error al modificar el contacto"));
                    }
                } else {
                    JOptionPane.showMessageDialog(delegado, getStringSafe("msg.select_category", "Seleccione una Categoria!!!"));
                }
            } else {
                JOptionPane.showMessageDialog(delegado, getStringSafe("msg.fill_all_fields", "Deben estar llenados todos los campos!!!"));
            }
        } else {
            JOptionPane.showMessageDialog(delegado, getStringSafe("msg.select_to_modify", "Seleccione un contacto para modificar"));
        }
    }

    private void buscarContactos() {
        String busqueda = delegado.txt_buscar.getText();
        if (busqueda == null || busqueda.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + busqueda));
        }
    }

    // Helper seguro para leer el bundle con fallback
    private String getStringSafe(String key, String defaultValue) {
        try {
            if (delegado != null && delegado.bundle != null && delegado.bundle.containsKey(key)) {
                return delegado.bundle.getString(key);
            }
        } catch (Exception ex) {
            // ignore
        }
        return defaultValue;
    }

    // Calcula estadísticas y delega la visualización a la vista (ya usa delegado.bundle para textos)
    private void actualizarEstadisticas() {
        int familia = 0, amigo = 0, trabajo = 0;

        if (contactos != null) {
            String familyKey = getStringSafe("category.family", "Familia").toLowerCase();
            String friendsKey = getStringSafe("category.friends", "Amigos").toLowerCase();
            String workKey = getStringSafe("category.work", "Trabajo").toLowerCase();

            for (persona p : contactos) {
                String c = (p.getCategoria() == null) ? "" : p.getCategoria().trim().toLowerCase();
                if (c.equals(familyKey) || c.equals("familia") || c.equals("family") || c.equals("família")) {
                    familia++;
                } else if (c.equals(friendsKey) || c.equals("amigo") || c.equals("amigos") || c.equals("friends")) {
                    amigo++;
                } else if (c.equals(workKey) || c.equals("trabajo") || c.equals("work") || c.equals("trabalho")) {
                    trabajo++;
                }
            }
        }

        int total = (contactos == null) ? 0 : contactos.size();

        String summaryTitle = getStringSafe("stats.title", "Estadísticas de Contactos");
        String statsFamily = getStringSafe("stats.family", "Familia");
        String statsFriends = getStringSafe("stats.friends", "Amigos");
        String statsWork = getStringSafe("stats.work", "Trabajo");
        String statsTotal = getStringSafe("stats.total", "Total");

        String summary = summaryTitle + ":\n\n"
                + statsFamily + ": " + familia + "\n"
                + statsFriends + ": " + amigo + "\n"
                + statsWork + ": " + trabajo + "\n"
                + statsTotal + ": " + total;
        delegado.txt_estadisticas.setText(summary);

        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put(statsFamily, familia);
        counts.put(statsFriends, amigo);
        counts.put(statsWork, trabajo);

        String chartTitle = getStringSafe("chart.title", "Distribución por Categoría");
        delegado.mostrarGraficaEnPanel(counts, chartTitle);
    }

    private void exportarCSV() {
        try (FileWriter writer = new FileWriter("Gestion_contactos.csv")) {
            writer.write("Nombre,Telefono,Email,Categoria,Favorito\n");
            if (contactos != null) {
                for (persona p : contactos) {
                    writer.write(escapeCsv(p.getNombre()) + "," + escapeCsv(p.getTelefono()) + "," + escapeCsv(p.getEmail()) + "," + escapeCsv(p.getCategoria()) + "," + p.isFavorito() + "\n");
                }
            }
            JOptionPane.showMessageDialog(delegado, getStringSafe("msg.export_success", "Contactos exportados correctamente"));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(delegado, getStringSafe("msg.export_failed", "Error al exportar"));
        }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }

    @Override
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int row = delegado.tbl_contactos.getSelectedRow();
            if (row != -1) {
                int modelRow = delegado.tbl_contactos.convertRowIndexToModel(row);
                delegado.txt_nombres.setText(contactos.get(modelRow).getNombre());
                delegado.txt_telefono.setText(contactos.get(modelRow).getTelefono());
                delegado.txt_email.setText(contactos.get(modelRow).getEmail());
                delegado.chb_favorito.setSelected(contactos.get(modelRow).isFavorito());
                delegado.cmb_categoria.setSelectedItem(contactos.get(modelRow).getCategoria());
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == delegado.cmb_categoria && e.getStateChange() == ItemEvent.SELECTED) {
            categoria = (String) delegado.cmb_categoria.getSelectedItem();
        } else if (e.getSource() == delegado.chb_favorito && e.getStateChange() == ItemEvent.SELECTED) {
            favorito = delegado.chb_favorito.isSelected();
        }
    }
}