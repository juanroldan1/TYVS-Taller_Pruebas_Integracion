package edu.unisabana.tyvs.registry.application.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para los controladores REST.
 *
 * <p>Convierte excepciones no controladas en respuestas HTTP apropiadas,
 * evitando que el cliente reciba un 500 genérico cuando el error es
 * causado por datos de entrada inválidos.</p>
 *
 * <p>Esto resuelve el <b>Defecto 05</b> del registro de defectos:
 * enviar un género inexistente retornaba HTTP 500 en lugar de 400.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura errores de conversión de enums (p.ej. género inválido "OTHER").
     * Retorna HTTP 400 Bad Request con un mensaje descriptivo.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Dato inválido: " + ex.getMessage());
    }

    /**
     * Captura errores internos de persistencia o lógica inesperada.
     * Retorna HTTP 500 con el mensaje original de la excepción.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno: " + ex.getMessage());
    }
}
