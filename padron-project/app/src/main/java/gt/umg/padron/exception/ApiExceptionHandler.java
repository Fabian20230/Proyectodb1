package gt.umg.padron.exception;

import gt.umg.padron.api.dto.MensajeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<MensajeResponse> regla(ReglaNegocioException e) {
        return ResponseEntity.status(e.getStatus()).body(new MensajeResponse(e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MensajeResponse> cuerpoInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new MensajeResponse("La solicitud no tiene un formato válido."));
    }

    /** Errores 50000+ (THROW/RAISERROR del procedimiento) se muestran tal cual; el resto, mensaje genérico. */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<MensajeResponse> baseDeDatos(DataAccessException e) {
        Throwable t = e;
        while (t != null && !(t instanceof SQLException)) {
            t = t.getCause();
        }
        if (t instanceof SQLException sql) {
            int c = sql.getErrorCode();
            if (c >= 50000) {
                return ResponseEntity.badRequest().body(new MensajeResponse(sql.getMessage()));
            }
            if (c == 547 || c == 2627 || c == 2601) {
                return ResponseEntity.badRequest().body(
                        new MensajeResponse("Los datos no cumplen las reglas del padrón o el registro ya existe."));
            }
        }
        log.error("Error de base de datos", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MensajeResponse("No se pudo completar la operación. Intente de nuevo."));
    }
}
