package br.com.fiap.pluvial.exception;

/** Regra de negócio violada (e-mail repetido, remoção de registro com filhos...): vira HTTP 409. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
