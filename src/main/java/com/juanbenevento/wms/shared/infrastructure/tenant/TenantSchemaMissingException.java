package com.juanbenevento.wms.shared.infrastructure.tenant;

/**
 * Excepción lanzada cuando el schema de un tenant no existe en la base de datos
 * y el aislamiento está en modo fail-closed (sin fallback a public).
 *
 * <p>Indica un estado inconsistente: el tenant está autenticado pero su schema
 * no fue provisionado. Debe responder 5xx con mensaje claro, nunca datos de public.
 */
public class TenantSchemaMissingException extends RuntimeException {

    public TenantSchemaMissingException(String message) {
        super(message);
    }
}
