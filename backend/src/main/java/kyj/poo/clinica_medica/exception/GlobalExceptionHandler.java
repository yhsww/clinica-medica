package kyj.poo.clinica_medica.exception;

import kyj.poo.clinica_medica.dto.ErroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage());
    }

    @ExceptionHandler(OperacaoInvalidaException.class)
    public ResponseEntity<ErroResponse> invalida(OperacaoInvalidaException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Operação inválida", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> argumentoInvalido(IllegalArgumentException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida", ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida", ex.getMostSpecificCause().getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        List<ErroResponse.CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErroResponse.CampoErro(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.badRequest()
                .body(new ErroResponse(HttpStatus.BAD_REQUEST.value(), "Erro de validação", "Campos inválidos", campos));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(NoHandlerFoundException ex) {
        return resposta(HttpStatus.NOT_FOUND, "Não encontrado", "Endpoint não existe: " + ex.getRequestURL());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> interno(Exception ex) {
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", ex.getMessage());
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), erro, mensagem, List.of()));
    }
}
