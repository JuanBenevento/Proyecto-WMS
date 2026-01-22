import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { GetTenantsUseCase } from '../../application/usecases/get-tenants.usecase';
import { ChangeTenantStatusUseCase } from '../../application/usecases/change-tenant-status.usecase'; 
import { Tenant } from '../../domain/models/tenant.model';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UpdateTenantUseCase } from '../../application/usecases/update-tenant.usecase';

@Component({
  selector: 'app-tenant-list',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: 'tenant-list.component.html'
})
export class TenantListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private getTenantsUC = inject(GetTenantsUseCase);
  private changeStatusUC = inject(ChangeTenantStatusUseCase);
  private updateTenantUC = inject(UpdateTenantUseCase);
  
  tenants: Tenant[] = [];
  loadingId: string | null = null;
  isModalOpen = false;
  isSaving = false;
  selectedTenantId: string | null = null;
  editForm: FormGroup;

  constructor() {
    this.editForm = this.fb.group({
      id: [{value: '', disabled: true}], 
      name: ['', [Validators.required]],
      contactEmail: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit() {
    this.loadTenants();
  }

  loadTenants() {
    this.getTenantsUC.execute().subscribe({
      next: (data) => this.tenants = data,
      error: (err) => console.error(err) 
    });
  }

  openEditModal(tenant: Tenant) {
    this.selectedTenantId = tenant.id;
    
    this.editForm.patchValue({
        id: tenant.id,
        name: tenant.name,
        contactEmail: tenant.contactEmail || '' 
    });
    
    this.isModalOpen = true;
  }

  closeModal() {
    this.isModalOpen = false;
    this.selectedTenantId = null;
    this.editForm.reset();
  }

  submitEdit() {
    if (this.editForm.invalid || !this.selectedTenantId) return;

    this.isSaving = true;
    const { name, contactEmail } = this.editForm.getRawValue();

    this.updateTenantUC.execute(this.selectedTenantId, { name, contactEmail }).subscribe({
        next: () => {
            alert('Empresa actualizada correctamente');
            this.isSaving = false;
            this.closeModal();
            this.loadTenants(); 
        },
        error: (err) => {
            console.error(err);
            alert('Error al actualizar');
            this.isSaving = false;
        }
    });
  }

  toggleStatus(tenant: Tenant) {
    const newStatus = !tenant.isActive;
    
    const message = newStatus 
        ? `¿Confirmas REACTIVAR el servicio para ${tenant.name}?` 
        : `ADVERTENCIA: Estás a punto de SUSPENDER a ${tenant.name}.\n\nSus usuarios perderán el acceso inmediatamente. ¿Continuar?`;

    if (!confirm(message)) return;

    this.loadingId = tenant.id;

    this.changeStatusUC.execute(tenant.id, newStatus).subscribe({
      next: () => {
        tenant.isActive = newStatus;
        this.loadingId = null;
      },
      error: (err) => {
        console.error('Error al cambiar estado', err);
        alert('Hubo un error al intentar cambiar el estado.');
        this.loadingId = null;
        this.loadTenants(); 
      }
    });
  }
}