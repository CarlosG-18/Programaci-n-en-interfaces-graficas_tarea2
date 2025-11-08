package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import controlador.logica_ventana;

public class ventana extends JFrame {

    public JPanel contentPane;
    public JTextField txt_nombres, txt_telefono, txt_email, txt_buscar;
    public JCheckBox chb_favorito;
    public JComboBox<String> cmb_categoria;
    public JButton btn_add, btn_modificar, btn_eliminar, btn_exportar;
    public JTable tbl_contactos;
    public JProgressBar progressBar;
    public JTextArea txt_estadisticas;
    public JPopupMenu popupMenu;
    public JMenuItem menuItemEliminar, menuItemModificar;
    private JLabel lbl_buscar;
    private JComboBox<String> cmb_idioma;
    public ResourceBundle bundle;
    private Locale currentLocale;

    private JLabel lbl_nombres;
    private JLabel lbl_telefono;
    private JLabel lbl_email;

    public JPanel panelEstadisticas;

    // tabbedPane como atributo para poder actualizar títulos dinámicamente
    private JTabbedPane tabbedPane;

    // panelContactos como atributo para referencia desde otros métodos
    private JPanel panelContactos;

    // índices de pestañas como constantes
    private static final int CONTACTS_TAB_INDEX = 0;
    private static final int STATS_TAB_INDEX = 1;

    // Referencia pública al controlador para notificaciones (se asigna dentro del controlador)
    public controlador.logica_ventana controlador;

    public ventana() {
        currentLocale = new Locale("es");
        bundle = ResourceBundle.getBundle("resources.messages", currentLocale);

        setTitle(getStringSafe("app.title", "Gestión de Contactos"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1026, 748);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5,5,5,5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        cmb_idioma = new JComboBox<>(new String[] {"Español", "Inglés", "Português"});
        cmb_idioma.setBounds(800, 10, 150, 30);
        contentPane.add(cmb_idioma);

        cmb_idioma.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String seleccion = (String) cmb_idioma.getSelectedItem();
                Locale nuevoLocale;
                if ("Inglés".equals(seleccion)) nuevoLocale = new Locale("en");
                else if ("Português".equals(seleccion)) nuevoLocale = new Locale("pt");
                else nuevoLocale = new Locale("es");
                actualizarIdioma(nuevoLocale);
            }
        });

        // Usar el tabbedPane como atributo
        tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(0, 50, 1012, 661);
        contentPane.add(tabbedPane);

        // panelContactos como atributo
        panelContactos = new JPanel(null);
        tabbedPane.addTab(getStringSafe("tab.contacts", "Contactos"), null, panelContactos, null);

        lbl_nombres = new JLabel(getStringSafe("label.names", "Nombres"));
        lbl_nombres.setBounds(25, 41, 120, 20);
        lbl_nombres.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl_nombres.setForeground(new Color(0, 70, 140));
        panelContactos.add(lbl_nombres);

        txt_nombres = new JTextField();
        txt_nombres.setBounds(160, 34, 391, 31);
        txt_nombres.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelContactos.add(txt_nombres);

        lbl_telefono = new JLabel(getStringSafe("label.phone", "Teléfono"));
        lbl_telefono.setBounds(25, 80, 120, 20);
        lbl_telefono.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl_telefono.setForeground(new Color(0, 70, 140));
        panelContactos.add(lbl_telefono);

        txt_telefono = new JTextField();
        txt_telefono.setBounds(160, 74, 391, 31);
        txt_telefono.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelContactos.add(txt_telefono);

        lbl_email = new JLabel(getStringSafe("label.email", "Correo"));
        lbl_email.setBounds(25, 120, 120, 20);
        lbl_email.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl_email.setForeground(new Color(0, 70, 140));
        panelContactos.add(lbl_email);

        txt_email = new JTextField();
        txt_email.setBounds(160, 114, 391, 31);
        txt_email.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelContactos.add(txt_email);

        lbl_buscar = new JLabel(getStringSafe("label.search", "Buscar"));
        lbl_buscar.setBounds(25, 590, 192, 20);
        lbl_buscar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl_buscar.setForeground(new Color(0, 70, 140));
        panelContactos.add(lbl_buscar);

        txt_buscar = new JTextField();
        txt_buscar.setBounds(220, 585, 776, 31);
        txt_buscar.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelContactos.add(txt_buscar);

        chb_favorito = new JCheckBox(getStringSafe("label.favorite", "Favorito"));
        chb_favorito.setBounds(24, 170, 193, 25);
        chb_favorito.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        chb_favorito.setForeground(new Color(46, 139, 87));
        panelContactos.add(chb_favorito);

        cmb_categoria = new JComboBox<>();
        cmb_categoria.setBounds(300, 167, 251, 31);
        cmb_categoria.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelContactos.add(cmb_categoria);

        actualizarComboCategoria();

        btn_add = new JButton(getStringSafe("button.add", "Agregar"));
        btn_add.setBounds(601, 70, 125, 65);
        btn_add.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn_add.setBackground(new Color(46, 139, 87));
        btn_add.setForeground(Color.WHITE);
        panelContactos.add(btn_add);

        btn_modificar = new JButton(getStringSafe("button.modify", "Modificar"));
        btn_modificar.setBounds(736, 70, 125, 65);
        btn_modificar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn_modificar.setBackground(new Color(255, 215, 0));
        btn_modificar.setForeground(Color.WHITE);
        panelContactos.add(btn_modificar);

        btn_eliminar = new JButton(getStringSafe("button.delete", "Eliminar"));
        btn_eliminar.setBounds(871, 69, 125, 65);
        btn_eliminar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn_eliminar.setBackground(new Color(220, 20, 60));
        btn_eliminar.setForeground(Color.WHITE);
        panelContactos.add(btn_eliminar);

        btn_exportar = new JButton(getStringSafe("button.export", "Exportar"));
        btn_exportar.setBounds(601, 150, 125, 30);
        btn_exportar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn_exportar.setBackground(new Color(0, 120, 215)); // azul
        btn_exportar.setForeground(Color.WHITE);
        panelContactos.add(btn_exportar);

        progressBar = new JProgressBar();
        progressBar.setBounds(25, 200, 971, 20);
        progressBar.setStringPainted(true);
        panelContactos.add(progressBar);

        tbl_contactos = new JTable();
        tbl_contactos.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    getStringSafe("label.names", "Nombres"),
                    getStringSafe("label.phone", "Teléfono"),
                    getStringSafe("label.email", "Correo"),
                    getStringSafe("category.name", "Categoría"),
                    getStringSafe("label.favorite", "Favorito")
                }
        ) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 4 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tbl_contactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrTabla = new JScrollPane(tbl_contactos);
        scrTabla.setBounds(25, 242, 971, 338);
        panelContactos.add(scrTabla);

        popupMenu = new JPopupMenu();
        menuItemEliminar = new JMenuItem(getStringSafe("menu.delete", "Eliminar"));
        menuItemModificar = new JMenuItem(getStringSafe("menu.modify", "Modificar"));
        popupMenu.add(menuItemEliminar);
        popupMenu.add(menuItemModificar);
        tbl_contactos.setComponentPopupMenu(popupMenu);

        panelEstadisticas = new JPanel(new BorderLayout());
        tabbedPane.addTab(getStringSafe("tab.stats", "Estadísticas"), null, panelEstadisticas, null);

        txt_estadisticas = new JTextArea();
        txt_estadisticas.setEditable(false);
        txt_estadisticas.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txt_estadisticas.setVisible(false);
        panelEstadisticas.add(new JScrollPane(txt_estadisticas), BorderLayout.SOUTH);

        // Crear controlador y asignarlo a la vista
        this.controlador = new logica_ventana(this);
    }

    private void actualizarComboCategoria() {
        cmb_categoria.removeAllItems();
        cmb_categoria.addItem(getStringSafe("category.select", "Seleccione una Categoria"));
        cmb_categoria.addItem(getStringSafe("category.family", "Familia"));
        cmb_categoria.addItem(getStringSafe("category.friends", "Amigos"));
        cmb_categoria.addItem(getStringSafe("category.work", "Trabajo"));
    }

    // Actualiza todo el UI y también los títulos de las pestañas
    public void actualizarIdioma(Locale locale) {
        this.currentLocale = locale;
        this.bundle = ResourceBundle.getBundle("resources.messages", locale);

        setTitle(getStringSafe("app.title", "Gestión de Contactos"));

        btn_add.setText(getStringSafe("button.add", "Agregar"));
        btn_modificar.setText(getStringSafe("button.modify", "Modificar"));
        btn_eliminar.setText(getStringSafe("button.delete", "Eliminar"));
        btn_exportar.setText(getStringSafe("button.export", "Exportar"));

        lbl_nombres.setText(getStringSafe("label.names", "Nombres"));
        lbl_telefono.setText(getStringSafe("label.phone", "Teléfono"));
        lbl_email.setText(getStringSafe("label.email", "Correo"));

        lbl_buscar.setText(getStringSafe("label.search", "Buscar"));
        chb_favorito.setText(getStringSafe("label.favorite", "Favorito"));

        actualizarComboCategoria();
        menuItemEliminar.setText(getStringSafe("menu.delete", "Eliminar"));
        menuItemModificar.setText(getStringSafe("menu.modify", "Modificar"));

        DefaultTableModel model = (DefaultTableModel) tbl_contactos.getModel();
        model.setColumnIdentifiers(new String[]{
            getStringSafe("label.names", "Nombres"),
            getStringSafe("label.phone", "Teléfono"),
            getStringSafe("label.email", "Correo"),
            getStringSafe("category.name", "Categoría"),
            getStringSafe("label.favorite", "Favorito")
        });

        // Actualizar títulos de pestañas dinámicamente usando constantes
        try {
            if (tabbedPane.getTabCount() > CONTACTS_TAB_INDEX) {
                tabbedPane.setTitleAt(CONTACTS_TAB_INDEX, getStringSafe("tab.contacts", "Contactos"));
            }
            if (tabbedPane.getTabCount() > STATS_TAB_INDEX) {
                tabbedPane.setTitleAt(STATS_TAB_INDEX, getStringSafe("tab.stats", "Estadísticas"));
            }
        } catch (Exception ex) {
            // no bloquear si algo cambia en la estructura de pestañas
        }

        // Forzar repintado de la tabla
        tbl_contactos.repaint();

        // Notificar al controlador para que recalcule estadísticas y entregue conteos localizados
        if (controlador != null) {
            controlador.onLanguageChanged();
        }

        // En caso de que la vista tenga textos estáticos de estadísticas, forzamos revalidación
        panelEstadisticas.revalidate();
        panelEstadisticas.repaint();
    }

    // Helper seguro para leer del bundle
    private String getStringSafe(String key, String defaultValue) {
        try {
            if (bundle != null && bundle.containsKey(key)) return bundle.getString(key);
        } catch (Exception ex) {
            // ignore
        }
        return defaultValue;
    }

    // Mostrar gráfica; resumen usa bundle actualizado y formatos locales
    public void mostrarGraficaEnPanel(Map<String, Integer> counts, String chartTitle) {
        if (counts == null) counts = new LinkedHashMap<>();
        panelEstadisticas.removeAll();

        // Pie chart recibe las etiquetas tal como llegan en el mapa (el controlador debe pasar labels ya localizados)
        PieChartPanel pie = new PieChartPanel(counts, bundle, chartTitle);
        JScrollPane centerScroll = new JScrollPane(pie);
        centerScroll.setBorder(null);
        panelEstadisticas.add(centerScroll, BorderLayout.CENTER);

        // Texto resumen con formato local
        JTextArea resumen = new JTextArea();
        resumen.setEditable(false);
        resumen.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Formateadores locales para números y porcentajes
        NumberFormat intFmt = NumberFormat.getIntegerInstance(currentLocale != null ? currentLocale : Locale.getDefault());
        NumberFormat pctFmt = NumberFormat.getPercentInstance(currentLocale != null ? currentLocale : Locale.getDefault());
        pctFmt.setMinimumFractionDigits(1);
        pctFmt.setMaximumFractionDigits(1);

        StringBuilder sb = new StringBuilder();
        int total = 0;
        for (Integer v : counts.values()) total += (v == null ? 0 : v);

        // Construir líneas: Label: cantidad (xx.x%)
        for (Map.Entry<String,Integer> e : counts.entrySet()) {
            String label = e.getKey() == null ? "" : e.getKey();
            int v = e.getValue() == null ? 0 : e.getValue();
            double pct = total == 0 ? 0.0 : (double) v / total;
            sb.append(String.format("%s: %s (%s)\n", label, intFmt.format(v), pctFmt.format(pct)));
        }

        String totalLabel = getStringSafe("stats.total", "Total");
        sb.append("\n").append(totalLabel).append(": ").append(intFmt.format(total));
        resumen.setText(sb.toString());
        resumen.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        panelEstadisticas.add(resumen, BorderLayout.SOUTH);

        panelEstadisticas.revalidate();
        panelEstadisticas.repaint();
    }

    // PieChartPanel igual que antes
    private static class PieChartPanel extends JPanel {
        private final Map<String, Integer> data;
        private final ResourceBundle bundle;
        private final String title;
        private final Color[] palette = new Color[] {
            new Color(0x4E79A7), new Color(0xF28E2B), new Color(0xE15759),
            new Color(0x76B7B2), new Color(0x59A14F), new Color(0xEDC948),
            new Color(0xB07AA1), new Color(0xFF9DA7)
        };

        PieChartPanel(Map<String,Integer> data, ResourceBundle bundle, String title) {
            this.data = (data == null) ? new LinkedHashMap<>() : data;
            this.bundle = bundle;
            this.title = title;
            setBackground(Color.WHITE);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(480, 320);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padding = 20;
            int legendWidth = Math.min(220, Math.max(140, w / 3));
            int pieSize = Math.min(w - legendWidth - padding*3, h - padding*4);
            pieSize = Math.max(pieSize, 80);
            int pieX = padding;
            int pieY = padding + 10;

            int total = 0;
            for (Integer v : data.values()) total += (v == null ? 0 : v);

            double startAngle = 0.0;
            int i = 0;
            for (Map.Entry<String,Integer> entry : data.entrySet()) {
                double value = entry.getValue() == null ? 0 : entry.getValue();
                double angle = (total == 0) ? 0 : value / (double) total * 360.0;
                g2.setColor(palette[i % palette.length]);
                g2.fillArc(pieX, pieY, pieSize, pieSize, (int)Math.round(startAngle), (int)Math.round(angle));
                startAngle += angle;
                i++;
            }

            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(pieX, pieY, pieSize, pieSize);

            int legendX = pieX + pieSize + padding;
            int legendY = pieY;
            int box = 14;
            int gap = 8;
            i = 0;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            for (Map.Entry<String,Integer> entry : data.entrySet()) {
                Color c = palette[i % palette.length];
                int rectY = legendY + i * (box + gap);
                g2.setColor(c);
                g2.fillRect(legendX, rectY, box, box);
                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(legendX, rectY, box, box);

                int v = entry.getValue() == null ? 0 : entry.getValue();
                double pct = total == 0 ? 0.0 : v * 100.0 / total;
                String label = String.format("%s: %d (%.1f%%)", entry.getKey(), v, pct);
                g2.drawString(label, legendX + box + 8, rectY + box - 3);
                i++;
            }

            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String titleToDraw = (title != null && !title.isEmpty()) ? title : (bundle != null && bundle.containsKey("chart.title") ? bundle.getString("chart.title") : "Gráfica");
            g2.drawString(titleToDraw, pieX, pieY + pieSize + 24);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ventana frame = new ventana();
            frame.setVisible(true);
        });
    }
}