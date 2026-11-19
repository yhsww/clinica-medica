package kyj.poo.clinica_medica.exception;

public class RecursoNaoEncontradoException
        extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}