package com.juanbenevento.wms.application.ports.in.usecases;

import com.juanbenevento.wms.application.ports.in.command.OnboardCompanyCommand;
import com.juanbenevento.wms.application.ports.in.dto.TenantResponse;
import com.juanbenevento.wms.domain.model.Tenant;
import java.util.List;

public interface ManageSaaSUseCase {
    void onboardNewCustomer(OnboardCompanyCommand command);
    List<TenantResponse> getAllTenants();
}