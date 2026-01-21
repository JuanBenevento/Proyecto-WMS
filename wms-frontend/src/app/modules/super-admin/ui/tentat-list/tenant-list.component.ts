import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { GetTenantsUseCase } from '../../application/usecases/get-tenants.usecase';
import { ChangeTenantStatusUseCase } from '../../application/usecases/change-tenant-status.usecase'; 
import { Tenant } from '../../domain/models/tenant.model';

@Component({
  selector: 'app-tenant-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="space-y-6">
      
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 class="text-2xl font-bold text-slate-800 tracking-tight">Gestión de Tenants</h2>
          <p class="text-sm text-slate-500 mt-1">Supervise y administre las empresas registradas en la plataforma.</p>
        </div>
        
        <a routerLink="/saas/onboarding" 
           class="inline-flex items-center justify-center px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-all font-medium shadow-sm hover:shadow-md gap-2">
           <i class="bi bi-plus-lg text-lg"></i>
           <span>Nueva Empresa</span>
        </a>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        
        <div class="overflow-x-auto">
          <table class="w-full text-sm text-left">
            
            <thead class="bg-slate-50 border-b border-slate-200">
              <tr>
                <th scope="col" class="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">Empresa</th>
                <th scope="col" class="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">Tenant ID</th>
                <th scope="col" class="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">Estado</th>
                <th scope="col" class="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider text-right">Acciones</th>
              </tr>
            </thead>

            <tbody class="divide-y divide-slate-100">
              
              <tr *ngFor="let tenant of tenants" class="group hover:bg-slate-50/80 transition-colors duration-150">
                
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center gap-4">
                    <div class="h-10 w-10 rounded-lg bg-indigo-50 border border-indigo-100 flex items-center justify-center text-indigo-700 font-bold shrink-0">
                      {{ tenant.name.charAt(0) | uppercase }}
                    </div>
                    <div>
                      <div class="font-medium text-slate-900">{{ tenant.name }}</div>
                    </div>
                  </div>
                </td>

                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="font-mono text-xs text-slate-500 bg-slate-100 px-2 py-1 rounded border border-slate-200">
                    {{ tenant.id }}
                  </span>
                </td>

                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border"
                        [ngClass]="tenant.isActive 
                          ? 'bg-emerald-50 text-emerald-700 border-emerald-200' 
                          : 'bg-red-50 text-red-700 border-red-200'">
                    
                    <span class="w-1.5 h-1.5 rounded-full mr-1.5"
                          [ngClass]="tenant.isActive ? 'bg-emerald-500' : 'bg-red-500'"></span>
                    {{ tenant.isActive ? 'Activo' : 'Suspendido' }}
                  </span>
                </td>

                <td class="px-6 py-4 whitespace-nowrap text-right">
                  
                  <button (click)="toggleStatus(tenant)"
                          [disabled]="loadingId === tenant.id"
                          class="group relative inline-flex items-center justify-center px-3 py-1.5 text-xs font-medium rounded-md border transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                          [ngClass]="tenant.isActive 
                              ? 'text-red-600 border-red-200 hover:bg-red-50 hover:border-red-300' 
                              : 'text-emerald-600 border-emerald-200 hover:bg-emerald-50 hover:border-emerald-300'">
                      
                      <span *ngIf="loadingId === tenant.id" class="absolute flex h-3 w-3">
                          <span class="animate-spin h-3 w-3 rounded-full border-2 border-current border-t-transparent"></span>
                      </span>

                      <span class="flex items-center gap-1" [class.opacity-0]="loadingId === tenant.id">
                          <i class="bi" [ngClass]="tenant.isActive ? 'bi-power' : 'bi-check-circle-fill'"></i>
                          {{ tenant.isActive ? 'Suspender' : 'Habilitar' }}
                      </span>

                  </button>

                </td>

              </tr>

              <tr *ngIf="tenants.length === 0">
                <td colspan="4" class="px-6 py-16 text-center">
                  <div class="flex flex-col items-center justify-center">
                    <div class="h-12 w-12 rounded-full bg-slate-50 flex items-center justify-center mb-3">
                      <i class="bi bi-buildings text-slate-400 text-xl"></i>
                    </div>
                    <h3 class="text-slate-900 font-medium mb-1">No hay empresas registradas</h3>
                    <p class="text-slate-500 text-sm max-w-xs mx-auto mb-4">
                      Comience dando de alta su primer cliente SaaS para gestionar sus operaciones.
                    </p>
                    <a routerLink="/saas/onboarding" class="text-indigo-600 hover:text-indigo-700 font-medium text-sm">
                      Crear primera empresa &rarr;
                    </a>
                  </div>
                </td>
              </tr>

            </tbody>
          </table>
        </div>
        
        <div class="bg-slate-50 px-6 py-3 border-t border-slate-200 flex items-center justify-between text-xs text-slate-500" *ngIf="tenants.length > 0">
          <span>Mostrando {{ tenants.length }} resultados</span>
          <div class="flex gap-2">
            <button class="disabled:opacity-50 hover:text-slate-800" disabled>Anterior</button>
            <button class="hover:text-slate-800">Siguiente</button>
          </div>
        </div>

      </div>
    </div>
  `
})
export class TenantListComponent implements OnInit {
  private getTenantsUC = inject(GetTenantsUseCase);
  private changeStatusUC = inject(ChangeTenantStatusUseCase); 
  
  tenants: Tenant[] = [];
  loadingId: string | null = null; 

  ngOnInit() {
    this.loadTenants();
  }

  loadTenants() {
    this.getTenantsUC.execute().subscribe({
      next: (data) => this.tenants = data,
      error: (err) => console.error('Error al cargar tenants:', err) 
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