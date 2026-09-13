package com.jcaa.imc.entrypoint.gui;

import com.jcaa.imc.adaptadores.red.ObservadorServidor;
import com.jcaa.imc.adaptadores.red.util.RedUtil;
import com.jcaa.imc.aplicacion.excepciones.ServidorRedException;
import com.jcaa.imc.aplicacion.puertos.entrada.GestionarServidorInputPort;
import com.jcaa.imc.dominio.enums.EstadoServidor;
import com.jcaa.imc.dominio.modelos.EventoServidor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Ventana principal de la interfaz gráfica del Servidor (Entrypoint GUI).
 * Responsabilidad única:
 * Renderizar la interfaz visual, capturar acciones del usuario e interactuar
 * exclusivamente con los
 * puertos de entrada y observadores de eventos.
 */
public class ServidorFrame extends JFrame implements ObservadorServidor {

  @Serial
  private static final long serialVersionUID = 1L;

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private static final Color COLOR_ACTIVO = new Color(46, 125, 50);
  private static final Color COLOR_INACTIVO = new Color(198, 40, 40);

  private final transient GestionarServidorInputPort gestionarServidorPort;

  private JTextField txtPuerto;
  private JButton btnIniciarDetener;
  private JLabel lblEstado;
  private JTextArea txtLogs;

  public ServidorFrame(final GestionarServidorInputPort gestionarServidorPort) {
    super("Servidor UDP - Cálculo de IMC (Arquitectura Hexagonal / SOLID)");
    this.gestionarServidorPort = Objects.requireNonNull(
        gestionarServidorPort, "El puerto de gestión del servidor es obligatorio.");
    initUI();
  }

  private void initUI() {
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(800, 580);
    setMinimumSize(new Dimension(700, 480));
    setLocationRelativeTo(null);
    setLayout(new BorderLayout(10, 10));

    add(crearPanelNorte(), BorderLayout.NORTH);
    add(crearPanelCentral(), BorderLayout.CENTER);

    agregarLineaLogInicial();
  }

  private JPanel crearPanelNorte() {
    final JPanel panelNorte = new JPanel(new BorderLayout(5, 5));
    panelNorte.setBorder(new EmptyBorder(12, 12, 5, 12));

    final JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT));
    final JLabel lblTitulo = new JLabel("Servidor de Cálculo de IMC (Protocolo UDP)");
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
    lblTitulo.setForeground(new Color(33, 33, 33));
    panelTitulo.add(lblTitulo);
    panelNorte.add(panelTitulo, BorderLayout.NORTH);
    panelNorte.add(crearPanelConfig(), BorderLayout.CENTER);
    return panelNorte;
  }

  private JPanel crearPanelConfig() {
    final JPanel panelConfig = new JPanel(new GridBagLayout());
    panelConfig.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                "Configuración del Host y Servicio",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)),
            new EmptyBorder(8, 10, 8, 10)));

    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(6, 6, 6, 6);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Fila 0 – IP y Puerto
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.0;
    panelConfig.add(labelBold("IP del Host Server:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.4;
    final JTextField txtHostIp = new JTextField(RedUtil.obtenerIpLocal());
    txtHostIp.setEditable(false);
    txtHostIp.setFont(new Font("Consolas", Font.BOLD, 13));
    txtHostIp.setBackground(new Color(240, 240, 240));
    panelConfig.add(txtHostIp, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.0;
    panelConfig.add(labelBold("Puerto de Escucha:"), gbc);

    gbc.gridx = 3;
    gbc.weightx = 0.3;
    txtPuerto = new JTextField("9876");
    txtPuerto.setFont(new Font("Consolas", Font.PLAIN, 13));
    panelConfig.add(txtPuerto, gbc);

    // Fila 1 – Estado y botón
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 0.0;
    panelConfig.add(labelBold("Estado Actual:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.4;
    lblEstado = new JLabel("● DETENIDO");
    lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 13));
    lblEstado.setForeground(COLOR_INACTIVO);
    panelConfig.add(lblEstado, gbc);

    gbc.gridx = 2;
    gbc.gridwidth = 2;
    gbc.weightx = 0.6;
    btnIniciarDetener = new JButton("Iniciar Servicio");
    btnIniciarDetener.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btnIniciarDetener.setBackground(COLOR_ACTIVO);
    btnIniciarDetener.setForeground(Color.WHITE);
    btnIniciarDetener.setFocusPainted(false);
    btnIniciarDetener.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnIniciarDetener.addActionListener(e -> alternarServicio());
    panelConfig.add(btnIniciarDetener, gbc);

    return panelConfig;
  }

  private JPanel crearPanelCentral() {
    final JPanel panelCentral = new JPanel(new BorderLayout(5, 5));
    panelCentral.setBorder(new EmptyBorder(0, 12, 12, 12));

    final JPanel panelLogHeader = new JPanel(new BorderLayout());
    final JLabel lblLogsTitulo = new JLabel(
        "Registro de Eventos y Conexiones (fecha-hora: IP:Puerto [datos recibidos/enviados]):");
    lblLogsTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
    panelLogHeader.add(lblLogsTitulo, BorderLayout.WEST);

    final JButton btnLimpiarLog = new JButton("Limpiar Log");
    btnLimpiarLog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    btnLimpiarLog.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnLimpiarLog.addActionListener(e -> txtLogs.setText(""));
    panelLogHeader.add(btnLimpiarLog, BorderLayout.EAST);

    panelCentral.add(panelLogHeader, BorderLayout.NORTH);

    txtLogs = new JTextArea();
    txtLogs.setEditable(false);
    txtLogs.setFont(new Font("Consolas", Font.PLAIN, 12));
    txtLogs.setBackground(new Color(250, 250, 250));
    txtLogs.setForeground(new Color(30, 30, 30));
    txtLogs.setLineWrap(true);
    txtLogs.setWrapStyleWord(true);

    final JScrollPane scrollLogs = new JScrollPane(txtLogs);
    scrollLogs.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    panelCentral.add(scrollLogs, BorderLayout.CENTER);

    return panelCentral;
  }

  private static JLabel labelBold(final String texto) {
    final JLabel lbl = new JLabel(texto);
    lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
    return lbl;
  }

  private void alternarServicio() {
    if (!gestionarServidorPort.estaCorriendo()) {
      iniciarServicio();
    } else {
      gestionarServidorPort.detenerServidor();
    }
  }

  private void iniciarServicio() {
    final int puerto;
    try {
      puerto = Integer.parseInt(txtPuerto.getText().trim());
      if (puerto < 1024 || puerto > 65535) {
        JOptionPane.showMessageDialog(
            this,
            "El puerto debe ser un número entre 1024 y 65535.",
            "Puerto Inválido",
            JOptionPane.WARNING_MESSAGE);
        return;
      }
    } catch (final NumberFormatException excepcion) {
      JOptionPane.showMessageDialog(
          this, "El puerto ingresado no es válido.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      gestionarServidorPort.iniciarServidor(puerto);
    } catch (final ServidorRedException excepcion) {
      JOptionPane.showMessageDialog(
          this,
          "No se pudo iniciar el servicio en el puerto " + puerto + ":\n" + excepcion.getMessage(),
          "Error de Red",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public void onEvento(final EventoServidor evento) {
    if (Objects.isNull(evento)) {
      return;
    }
    final String timestamp = evento.getFechaHora().format(DATE_FORMAT);
    final String linea;
    if ("EVENTO".equalsIgnoreCase(evento.getCategoria())) {
      linea = String.format("%s: %s (%s)%n", timestamp, evento.getEndpoint(), evento.getDescripcion());
    } else {
      linea = String.format(
          "%s: [SERVIDOR] [%s] %s%n",
          timestamp, evento.getCategoria(), evento.getDescripcion());
    }
    SwingUtilities.invokeLater(
        () -> {
          txtLogs.append(linea);
          txtLogs.setCaretPosition(txtLogs.getDocument().getLength());
        });
  }

  @Override
  public void onCambioEstado(final EstadoServidor nuevoEstado, final int puerto) {
    SwingUtilities.invokeLater(
        () -> {
          if (nuevoEstado == EstadoServidor.EN_LINEA) {
            txtPuerto.setEditable(false);
            btnIniciarDetener.setText("Detener Servicio");
            btnIniciarDetener.setBackground(COLOR_INACTIVO);
            lblEstado.setText("● EN LÍNEA (Escuchando)");
            lblEstado.setForeground(COLOR_ACTIVO);
          } else {
            txtPuerto.setEditable(true);
            btnIniciarDetener.setText("Iniciar Servicio");
            btnIniciarDetener.setBackground(COLOR_ACTIVO);
            lblEstado.setText("● DETENIDO");
            lblEstado.setForeground(COLOR_INACTIVO);
          }
        });
  }

  private void agregarLineaLogInicial() {
    final String timestamp = LocalDateTime.now().format(DATE_FORMAT);
    final String linea = String.format(
        "%s: [SERVIDOR] [SISTEMA] Aplicación lista. Presione 'Iniciar Servicio' "
            + "para activar la recepción de paquetes UDP.%n",
        timestamp);
    SwingUtilities.invokeLater(
        () -> {
          txtLogs.append(linea);
          txtLogs.setCaretPosition(txtLogs.getDocument().getLength());
        });
  }
}
