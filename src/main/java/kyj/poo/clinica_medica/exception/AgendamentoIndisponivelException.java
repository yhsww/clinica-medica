package kyj.poo.clinica_medica.exception;

public class AgendamentoIndisponivelException extends RuntimeException {

    public AgendamentoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}