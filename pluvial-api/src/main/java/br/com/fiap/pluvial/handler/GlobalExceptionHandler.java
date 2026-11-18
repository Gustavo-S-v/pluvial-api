package br.com.fiap.pluvial.handler;

import br.com.fiap.pluvial.dto.ErroResponseDTO;
import br.com.fiap.pluvial.exception.RecursoNaoEncontradoException;
import br.com.fiap.pluvial.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Todo erro da API sai como JSON padronizado, sem stack trace nem detalhes de SQL. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacao(MethodArgumentNotValidException ex,
                                                           HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos. Verifique os campos informados.", req, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarCorpoInvalido(HttpMessageNotReadableException ex,
                                                               HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou com JSON inválido.", req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroInvalido(MethodArgumentTypeMismatchException ex,
                                                                   HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + ex.getName() + "'.", req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
                                                                    HttpServletRequest req) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado para esta rota.", req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarTipoNaoSuportado(HttpMediaTypeNotSupportedException ex,
                                                                  HttpServletRequest req) {
        return montar(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de conteúdo não suportado. Use application/json.", req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponseDTO> tratarRotaInexistente(NoResourceFoundException ex,
                                                                 HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "Rota não encontrada.", req, null);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                               HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> tratarRegraNegocio(RegraNegocioException ex,
                                                              HttpServletRequest req) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> tratarErroInesperado(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {}", req.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao processar a requisição.", req, null);
    }

    private ResponseEntity<ErroResponseDTO> montar(HttpStatus status, String mensagem,
                                                   HttpServletRequest req, Map<String, String> campos) {
        ErroResponseDTO corpo = new ErroResponseDTO(
                status.value(), status.getReasonPhrase(), mensagem, req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
