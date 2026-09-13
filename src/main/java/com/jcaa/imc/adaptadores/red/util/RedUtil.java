package com.jcaa.imc.adaptadores.red.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Utilidad de red para inspeccionar las interfaces del host. Responsabilidad
 * única: Detección y
 * resolución de la dirección IP local del host.
 */
public final class RedUtil {

  private RedUtil() {
    // Evita instanciación – clase de utilidad estática
  }

  /**
   * Obtiene la dirección IPv4 local no loopback de la máquina. Si no se encuentra
   * conectividad
   * externa, retorna la dirección de loopback del sistema.
   */
  public static String obtenerIpLocal() {
    try {
      final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        final NetworkInterface iface = interfaces.nextElement();
        if (iface.isLoopback() || !iface.isUp()) {
          continue;
        }
        final Enumeration<InetAddress> addresses = iface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          final InetAddress addr = addresses.nextElement();
          if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
            return addr.getHostAddress();
          }
        }
      }
      return InetAddress.getLocalHost().getHostAddress();
    } catch (final SocketException | UnknownHostException excepcion) {
      return InetAddress.getLoopbackAddress().getHostAddress();
    }
  }
}
