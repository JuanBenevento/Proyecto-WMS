package com.juanbenevento.wms.identity.application.port.in.usescases;

import com.juanbenevento.wms.identity.application.port.in.command.OnboardCompanyCommand;
import com.juanbenevento.wms.identity.application.port.in.command.UpdateTenantCommand;
import com.juanbenevento.wms.identity.application.port.in.dto.TenantResponse;

import java.util.List;

public interface ManageSaaSUseCase {
    void onboardNewCustomer(OnboardCompanyCommand command);
    List<TenantResponse> getAllTenants();
    void toggleTenantStatus(String tenantId, boolean isActive);
    void updateTenant(String tenantId, UpdateTenantCommand command);
}