package com.jcaa.imc.adaptadores.red.mapper;

import com.jcaa.imc.adaptadores.red.response.UdpResponse;
import com.jcaa.imc.dominio.excepciones.RespuestaInvalidaException;
import com.jcaa.imc.dominio.modelos.RespuestaCliente;
import com.jcaa.imc.dominio.modelos.Resultado;
import java.util.Objects;

/**
 * Mapper del adaptador de red. Responsabilidad única: Convertir objetos de
 * dominio
 * (RespuestaCliente) a respuestas formateadas para el protocolo de red UDP
 * (UdpResponse).
 */
public final class UdpNetworkMapper {

  public UdpResponse toNetworkResponse(final RespuestaCliente respuesta) {
    if (Objects.isNull(respuesta)) {
      throw new RespuestaInvalidaException("No se puede serializar una respuesta nula.");
    }

    final String payload = switch (respuesta.getTipo()) {
      case CONECTADO -> "CONECTADO_OK;" + respuesta.getMensaje();
      case DESCONECTADO -> "DESCONECTADO_OK;" + respuesta.getMensaje();
      case OK_CALCULO -> buildPayloadCalculo(respuesta.getResultado());
      case ERROR ->
        "ERROR;"
            + (Objects.nonNull(respuesta.getMensaje())
                ? respuesta.getMensaje()
                : "Error desconocido.");
    };

    return new UdpResponse(
        payload, respuesta.getDestinatario().ip(), respuesta.getDestinatario().puerto());
  }

  private static String buildPayloadCalculo(final Resultado resultado) {
    if (Objects.isNull(resultado)) {
      throw new RespuestaInvalidaException("No se puede serializar un cálculo sin resultado.");
    }
    return String.format(
        "OK_CALCULO;%s;%s;%s",
        resultado.getImcFormateado(), resultado.getClasificacion(), resultado.getRecomendaciones());
  }
}
