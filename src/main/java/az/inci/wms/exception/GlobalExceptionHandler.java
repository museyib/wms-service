package az.inci.wms.exception;

import az.inci.wms.model.v4.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static az.inci.wms.Utilities.getClearMessage;
import static az.inci.wms.Utilities.getMessage;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(OperationNotCompletedException.class)
    public ResponseEntity<Response<String>> handleOperationNotCompletedException(OperationNotCompletedException e) {
        String message = getMessage(e);
        log.error(message);
        return ResponseEntity.ok(Response.getUserErrorResponse(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<String>> handleGeneralException(Exception e) {
        String message = getMessage(e);
        log.error(message);
        return ResponseEntity.ok(Response.getServerErrorResponse(message));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Response<String>> handleIllegalStateException(IllegalStateException e) {
        String message = getClearMessage(e);
        log.error(message);
        return ResponseEntity.ok(Response.getUserErrorResponse(message));
    }
}
