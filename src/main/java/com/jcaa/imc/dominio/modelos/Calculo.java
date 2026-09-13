package com.jcaa.imc.dominio.modelos;

import com.jcaa.imc.dominio.enums.ClasificacionImc;
import com.jcaa.imc.dominio.excepciones.CalculoInvalidoException;
import com.jcaa.imc.dominio.vo.Altura;
import com.jcaa.imc.dominio.vo.Peso;
import java.util.Objects;

/**
 * Entidad/Modelo del dominio para el cálculo del Índice de Masa Corporal (IMC). Responsabilidad
 * única: Aplicar las fórmulas matemáticas y criterios diagnósticos de la OMS.
 */
public final class Calculo {

  private final Peso peso;
  private final Altura altura;

  public Calculo(final Peso peso, final Altura altura) {
    if (Objects.isNull(peso) || Objects.isNull(altura)) {
      throw new CalculoInvalidoException(
          "El peso y la altura son obligatorios para calcular el IMC.");
    }
    this.peso = peso;
    this.altura = altura;
  }

  /**
   * Calcula el IMC a partir del peso y la estatura aplicando las fórmulas y rangos diagnósticos de
   * la Organización Mundial de la Salud (OMS).
   *
   * @return Objeto Resultado con el valor numérico, clasificación y recomendación.
   */
  public Resultado calcular() {
    final double alturaEnMetros = altura.valorEnMetros();
    final double imc = peso.valor() / (alturaEnMetros * alturaEnMetros);
    final ClasificacionImc clasificacion = ClasificacionImc.deImc(imc);

    return new Resultado(
        imc, clasificacion.getClasificacion(), clasificacion.getRecomendacion(), clasificacion);
  }

  public Peso getPeso() {
    return peso;
  }

  public Altura getAltura() {
    return altura;
  }
}
