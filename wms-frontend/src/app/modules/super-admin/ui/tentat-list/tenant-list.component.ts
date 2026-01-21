import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { GetTenantsUseCase } from '../../application/usecases/get-tenants.usecase';
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
                          : 'bg-slate-100 text-slate-600 border-slate-200'">
                    
                    <span class="w-1.5 h-1.5 rounded-full mr-1.5"
                          [ngClass]="tenant.isActive ? 'bg-emerald-500' : 'bg-slate-400'"></span>
                    {{ tenant.isActive ? 'Activo' : 'Inactivo' }}
                  </span>
                </td>

                <td class="px-6 py-4 whitespace-nowrap text-right">
                  <button class="text-slate-400 hover:text-indigo-600 font-medium text-sm transition-colors p-2 rounded-full hover:bg-indigo-50">
                    Gestionar
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
  
  tenants: Tenant[] = [];

  ngOnInit() {
    this.getTenantsUC.execute().subscribe({
      next: (data) => this.tenants = data,
      error: (err) => console.error('Error al cargar tenants:', err) 
    });
  }
}