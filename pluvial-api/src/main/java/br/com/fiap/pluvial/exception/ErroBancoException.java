package br.com.fiap.pluvial.exception;

/** Falha inesperada no banco: vira HTTP 500 com mensagem genérica (o detalhe só vai para o log). */
public class ErroBancoException extends RuntimeException {

    public ErroBancoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
