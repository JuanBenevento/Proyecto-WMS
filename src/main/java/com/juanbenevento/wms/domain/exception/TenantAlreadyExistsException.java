package com.juanbenevento.wms.domain.exception;

public class TenantAlreadyExistsException extends DomainException {
    public TenantAlreadyExistsException(String companyId) {
        super("La empresa con ID " + companyId + " ya está registrada.");
    }
}