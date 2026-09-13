package com.jcaa.imc.adaptadores.red;

import com.jcaa.imc.adaptadores.red.response.UdpResponse;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Objects;

/**
 * Encapsula la administración y operaciones sobre el DatagramSocket del sistema
 * operativo.
 */
public class CanalUdp {

  private DatagramSocket socket;
  private volatile boolean abierto = false;
  private int puertoActual = 0;

  public synchronized void abrir(final int puerto) throws SocketException {
    if (abierto && Objects.nonNull(socket) && !socket.isClosed()) {
      return;
    }
    socket = new DatagramSocket(puerto);
    puertoActual = puerto;
    abierto = true;
  }

  public synchronized void cerrar() {
    abierto = false;
    if (Objects.nonNull(socket) && !socket.isClosed()) {
      socket.close();
    }
    socket = null;
    puertoActual = 0;
  }

  public synchronized boolean isAbierto() {
    return abierto && Objects.nonNull(socket) && !socket.isClosed();
  }

  public synchronized int getPuertoActual() {
    return puertoActual;
  }

  public void enviar(final UdpResponse respuesta) throws IOException {
    final DatagramSocket socketAbierto = obtenerSocketAbierto();
    final byte[] buffer = respuesta.getBytes();
    final InetAddress direccion = InetAddress.getByName(respuesta.getIpDestino());
    final DatagramPacket paquete = new DatagramPacket(buffer, buffer.length, direccion, respuesta.getPuertoDestino());
    socketAbierto.send(paquete);
  }

  public void recibir(final DatagramPacket paquete) throws IOException {
    obtenerSocketAbierto().receive(paquete);
  }

  private synchronized DatagramSocket obtenerSocketAbierto() throws SocketException {
    if (Objects.isNull(socket) || socket.isClosed()) {
      throw new SocketException("El socket UDP está cerrado.");
    }
    return socket;
  }
}
