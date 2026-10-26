package gt.umg.padron.exception;

import org.springframework.http.HttpStatus;

/** Error esperado que se muestra tal cual al usuario (regla de negocio, dato inválido, no encontrado). */
public class ReglaNegocioException extends RuntimeException {
    private final HttpStatus status;

    public ReglaNegocioException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public ReglaNegocioException(String mensaje) {
        this(HttpStatus.BAD_REQUEST, mensaje);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
