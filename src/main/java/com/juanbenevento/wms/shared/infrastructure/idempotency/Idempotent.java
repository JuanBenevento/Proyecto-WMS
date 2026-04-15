package com.juanbenevento.wms.shared.infrastructure.idempotency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar métodos que deben ser idempotentes.
 * Una operación idempotente puede ejecutarse múltiples veces con el mismo resultado.
 * 
 * Utilizada para prevenir operaciones duplicadas en caso de reintentos
 * porTimeouts de red o errores transitorios.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
}
