package com.jcaa.imc.aplicacion.servicios;

import com.jcaa.imc.aplicacion.dto.ProcesarPeticionUdpCommand;
import com.jcaa.imc.aplicacion.mapper.CalculoMapper;
import com.jcaa.imc.aplicacion.mapper.PeticionMapper;
import com.jcaa.imc.aplicacion.puertos.entrada.ProcesarPeticionUdpInputPort;
import com.jcaa.imc.dominio.excepciones.DominioException;
import com.jcaa.imc.dominio.modelos.Calculo;
import com.jcaa.imc.dominio.modelos.EventoServidor;
import com.jcaa.imc.dominio.modelos.RespuestaCliente;
import com.jcaa.imc.dominio.modelos.Resultado;
import com.jcaa.imc.dominio.puertos.salida.PuertoNotificacionEvento;
import com.jcaa.imc.dominio.puertos.salida.PuertoSalidaRed;
import com.jcaa.imc.dominio.vo.Destinatario;
import java.util.Locale;
import java.util.Objects;

/**
 * Servicio de aplicación para procesar las peticiones del protocolo UDP de IMC.
 * Responsabilidad
 * única: Decodificar y orquestar el procesamiento de los comandos de la red,
 * coordinar con el
 * dominio y despachar respuestas a través de los puertos de salida.
 */
public final class ProcesarPeticionUdpService implements ProcesarPeticionUdpInputPort {

  private final PuertoSalidaRed puertoSalidaRed;
  private final PuertoNotificacionEvento puertoNotificacion;
  private final PeticionMapper peticionMapper;
  private final CalculoMapper calculoMapper;

  public ProcesarPeticionUdpService(
      final PuertoSalidaRed puertoSalidaRed,
      final PuertoNotificacionEvento puertoNotificacion,
      final PeticionMapper peticionMapper,
      final CalculoMapper calculoMapper) {
    this.puertoSalidaRed = Objects.requireNonNull(puertoSalidaRed, "El puerto de salida es obligatorio.");
    this.puertoNotificacion = Objects.requireNonNull(puertoNotificacion, "El puerto de notificación es obligatorio.");
    this.peticionMapper = Objects.requireNonNull(peticionMapper, "El mapper de petición es obligatorio.");
    this.calculoMapper = Objects.requireNonNull(calculoMapper, "El mapper de cálculo es obligatorio.");
  }

  @Override
  public void procesar(final ProcesarPeticionUdpCommand comando) {
    Objects.requireNonNull(comando, "El comando es obligatorio.");

    final Destinatario destinatario = peticionMapper.toDestinatario(comando);
    final String mensaje = comando.mensaje();

    if (Objects.isNull(mensaje) || mensaje.isBlank()) {
      puertoSalidaRed.enviarRespuesta(
          RespuestaCliente.error(destinatario, "Mensaje vacío recibido."));
      notificarEvento(destinatario.endpoint(), "datos recibidos --> [Vacío]");
      return;
    }

    final String comandoTexto = mensaje.trim();

    if (comandoTexto.equalsIgnoreCase("CONECTAR")) {
      puertoSalidaRed.enviarRespuesta(
          RespuestaCliente.conectado(
              destinatario, "Servidor UDP listo para recibir cálculos de IMC"));
      notificarEvento(
          destinatario.endpoint(),
          "conectado --> Solicitud de verificación recibida y aceptada");
      return;
    }

    if (comandoTexto.equalsIgnoreCase("DESCONECTAR")) {
      puertoSalidaRed.enviarRespuesta(
          RespuestaCliente.desconectado(destinatario, "Sesión finalizada"));
      notificarEvento(destinatario.endpoint(), "desconectado --> Cliente ha cerrado la sesión");
      return;
    }

    if (comandoTexto.toUpperCase(Locale.ROOT).startsWith("CALCULAR;")) {
      procesarCalculo(destinatario, comandoTexto);
      return;
    }

    puertoSalidaRed.enviarRespuesta(
        RespuestaCliente.error(destinatario, "Comando no reconocido por el servidor UDP."));
    notificarEvento(
        destinatario.endpoint(),
        "datos recibidos --> Comando desconocido: [" + comandoTexto + "]");
  }

  private void procesarCalculo(final Destinatario destinatario, final String comandoTexto) {
    final String[] partes = comandoTexto.split(";", -1);
    if (partes.length != 3) {
      puertoSalidaRed.enviarRespuesta(
          RespuestaCliente.error(
              destinatario, "Formato inválido. Se esperaba CALCULAR;peso;altura"));
      notificarEvento(
          destinatario.endpoint(),
          "datos recibidos --> Formato incorrecto: " + comandoTexto);
      return;
    }

    try {
      final double valorPeso = Double.parseDouble(partes[1].trim().replace(',', '.'));
      final double valorAltura = Double.parseDouble(partes[2].trim().replace(',', '.'));

      final Calculo calculo = calculoMapper.toDomain(valorPeso, valorAltura);
      final Resultado resultado = calculo.calcular();

      puertoSalidaRed.enviarRespuesta(RespuestaCliente.calculoExitoso(destinatario, resultado));

      final String logInfo = String.format(
          Locale.US,
          "datos recibidos (Peso: %.2f kg, Altura: %.2f m) --> datos enviados (IMC: %s, %s)",
          calculo.getPeso().valor(),
          calculo.getAltura().valorEnMetros(),
          resultado.getImcFormateado(),
          resultado.getClasificacion());

      notificarEvento(destinatario.endpoint(), logInfo);

    } catch (final NumberFormatException excepcion) {
      puertoSalidaRed.enviarRespuesta(
          RespuestaCliente.error(
              destinatario, "Los parámetros de peso y altura deben ser numéricos."));
      notificarEvento(
          destinatario.endpoint(),
          "datos recibidos --> Error de formato numérico: " + comandoTexto);

    } catch (final DominioException excepcion) {
      puertoSalidaRed.enviarRespuesta(RespuestaCliente.error(destinatario, excepcion.getMessage()));
      notificarEvento(
          destinatario.endpoint(),
          "datos recibidos --> Validación fallida: " + excepcion.getMessage());
    }
  }

  private void notificarEvento(final String endpoint, final String descripcion) {
    puertoNotificacion.notificarEvento(new EventoServidor("EVENTO", endpoint, descripcion));
  }
}
