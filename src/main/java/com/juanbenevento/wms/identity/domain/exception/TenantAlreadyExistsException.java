package com.juanbenevento.wms.identity.domain.exception;

import com.juanbenevento.wms.shared.domain.exception.DomainException;

public class TenantAlreadyExistsException extends DomainException {
    public TenantAlreadyExistsException(String companyId) {
        super("La empresa con ID " + companyId + " ya está registrada.");
    }
}