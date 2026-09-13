package com.jcaa.imc.aplicacion.servicios;

import com.jcaa.imc.aplicacion.dto.CalcularImcCommand;
import com.jcaa.imc.aplicacion.dto.ResultadoImcDto;
import com.jcaa.imc.aplicacion.mapper.CalculoMapper;
import com.jcaa.imc.aplicacion.puertos.entrada.CalcularImcInputPort;
import com.jcaa.imc.dominio.modelos.Calculo;
import com.jcaa.imc.dominio.modelos.Resultado;
import java.util.Objects;

/**
 * Servicio de aplicación que implementa el caso de uso de calcular el IMC.
 * Responsabilidad única:
 * Orquestar la conversión del comando a dominio, invocar la lógica del dominio
 * y retornar el
 * resultado en formato DTO.
 */
public final class CalcularImcService implements CalcularImcInputPort {

  private final CalculoMapper calculoMapper;

  public CalcularImcService(final CalculoMapper calculoMapper) {
    this.calculoMapper = Objects.requireNonNull(calculoMapper, "El mapper de cálculo es obligatorio.");
  }

  @Override
  public ResultadoImcDto calcular(final CalcularImcCommand comando) {
    final Calculo calculo = calculoMapper.toDomain(comando);
    final Resultado resultado = calculo.calcular();
    return calculoMapper.toDto(resultado);
  }
}
