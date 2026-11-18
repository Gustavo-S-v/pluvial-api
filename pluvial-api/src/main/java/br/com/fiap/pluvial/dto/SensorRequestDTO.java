package br.com.fiap.pluvial.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class SensorRequestDTO {

    @NotNull(message = "A região é obrigatória")
    @Positive(message = "O id da região deve ser positivo")
    private Long regiaoId;

    @NotBlank(message = "O nome do sensor é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "O tipo é obrigatório")
    @Pattern(regexp = "PLUVIOMETRO|NIVEL_RIO", message = "Tipo inválido. Use: PLUVIOMETRO ou NIVEL_RIO")
    private String tipo;

    @NotBlank(message = "A localização é obrigatória")
    @Size(max = 200, message = "A localização deve ter no máximo 200 caracteres")
    private String localizacao;

    @NotNull(message = "O limite de alerta é obrigatório")
    @DecimalMin(value = "0.01", message = "O limite de alerta deve ser maior que zero")
    @Digits(integer = 6, fraction = 2, message = "O limite de alerta aceita até 6 inteiros e 2 decimais")
    private BigDecimal limiteAlerta;

    @NotBlank(message = "O status é obrigatório")
    @Pattern(regexp = "ATIVO|INATIVO", message = "Status inválido. Use: ATIVO ou INATIVO")
    private String status;

    public Long getRegiaoId() {
        return regiaoId;
    }

    public void setRegiaoId(Long regiaoId) {
        this.regiaoId = regiaoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public BigDecimal getLimiteAlerta() {
        return limiteAlerta;
    }

    public void setLimiteAlerta(BigDecimal limiteAlerta) {
        this.limiteAlerta = limiteAlerta;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
