package com.jcaa.imc.aplicacion.puertos.entrada;

import com.jcaa.imc.aplicacion.dto.CalcularImcCommand;
import com.jcaa.imc.aplicacion.dto.ResultadoImcDto;

/** Puerto de entrada (Caso de Uso) para el cálculo de IMC. */
public interface CalcularImcInputPort {
  ResultadoImcDto calcular(CalcularImcCommand comando);
}
