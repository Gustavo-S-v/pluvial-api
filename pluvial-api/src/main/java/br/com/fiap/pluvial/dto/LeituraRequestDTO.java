package br.com.fiap.pluvial.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class LeituraRequestDTO {

    @NotNull(message = "O valor da leitura é obrigatório")
    @DecimalMin(value = "0.0", message = "O valor da leitura não pode ser negativo")
    @Digits(integer = 6, fraction = 2, message = "O valor aceita até 6 inteiros e 2 decimais")
    private BigDecimal valor;

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
