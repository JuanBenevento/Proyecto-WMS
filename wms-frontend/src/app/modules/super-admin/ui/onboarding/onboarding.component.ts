import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { OnboardCompanyUseCase } from '../../application/usecases/onboard-company.usecase';
import { OnboardData } from '../../domain/models/onboard-data.model';

@Component({
  selector: 'app-onboarding',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="max-w-2xl mx-auto">
      
      <div class="mb-6 flex items-center gap-2 text-sm text-slate-500">
        <a routerLink="/saas/tenants" class="hover:text-indigo-600 transition-colors">Empresas</a>
        <i class="bi bi-chevron-right text-xs"></i>
        <span class="font-medium text-slate-800">Nueva Alta</span>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 sm:p-8">
        
        <div class="mb-8 border-b border-slate-100 pb-6">
          <h2 class="text-xl font-bold text-slate-800 mb-2">Alta de Nuevo Cliente</h2>
          <p class="text-slate-500 text-sm">Configure la empresa y cree el usuario administrador inicial.</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-6">
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="col-span-2">
              <label class="block text-sm font-medium text-slate-700 mb-1">Nombre de la Empresa</label>
              <input type="text" formControlName="companyName" placeholder="Ej: Logística Global S.A."
                     class="w-full px-4 py-2 rounded-lg border border-slate-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all">
              
              <div *ngIf="form.get('companyName')?.touched && form.get('companyName')?.invalid" class="text-red-500 text-xs mt-1">
                El nombre es obligatorio.
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">ID Empresa (Tenant ID)</label>
              <input type="text" formControlName="companyId" placeholder="Ej: LOG-GLOBAL"
                     class="w-full px-4 py-2 rounded-lg border border-slate-300 focus:ring-2 focus:ring-indigo-500 outline-none bg-slate-50 uppercase font-mono text-sm">
              <p class="text-xs text-slate-400 mt-1">Sin espacios, identificador único.</p>
            </div>
          </div>

          <div class="relative py-2">
            <div class="absolute inset-0 flex items-center"><div class="w-full border-t border-slate-100"></div></div>
            <div class="relative flex justify-center"><span class="bg-white px-3 text-xs font-medium text-slate-400 uppercase tracking-wider">Administrador Inicial</span></div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="col-span-2">
               <label class="block text-sm font-medium text-slate-700 mb-1">Email Corporativo</label>
               <input type="email" formControlName="adminEmail" placeholder="admin@empresa.com"
                      class="w-full px-4 py-2 rounded-lg border border-slate-300 focus:ring-2 focus:ring-indigo-500 outline-none">
            </div>

            <div>
               <label class="block text-sm font-medium text-slate-700 mb-1">Usuario Admin</label>
               <input type="text" formControlName="adminUsername" placeholder="admin_global"
                      class="w-full px-4 py-2 rounded-lg border border-slate-300 focus:ring-2 focus:ring-indigo-500 outline-none">
            </div>

            <div>
               <label class="block text-sm font-medium text-slate-700 mb-1">Contraseña</label>
               <input type="password" formControlName="adminPassword"
                      class="w-full px-4 py-2 rounded-lg border border-slate-300 focus:ring-2 focus:ring-indigo-500 outline-none">
            </div>
          </div>

          <div class="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
            <a routerLink="/saas/tenants" class="px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-800 transition-colors">
              Cancelar
            </a>
            
            <button type="submit" 
                    [disabled]="form.invalid || isSubmitting"
                    class="px-6 py-2 bg-indigo-600 text-white text-sm font-bold rounded-lg hover:bg-indigo-700 transition-all shadow-md disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2">
              
              <span *ngIf="isSubmitting" class="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"></span>
              
              {{ isSubmitting ? 'Procesando...' : 'Dar de Alta Empresa' }}
            </button>
          </div>

        </form>
      </div>
    </div>
  `
})
export class OnboardingComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private onboardUC = inject(OnboardCompanyUseCase);

  isSubmitting = false;

  form: FormGroup = this.fb.group({
    companyName: ['', [Validators.required]],
    companyId: ['', [Validators.required, Validators.pattern('^[A-Za-z0-9-]+$')]], 
    adminEmail: ['', [Validators.required, Validators.email]],
    adminUsername: ['', [Validators.required]],
    adminPassword: ['', [Validators.required, Validators.minLength(4)]]
  });

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched(); 
      return;
    }

    this.isSubmitting = true;

    const requestData: OnboardData = this.form.getRawValue();

    this.onboardUC.execute(requestData).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.router.navigate(['/saas/tenants']); 
      },
      error: (error) => {
        console.error('Error en onboarding:', error);
        alert('Ocurrió un error al crear la empresa. Verifique los datos o intente más tarde.');
        this.isSubmitting = false;
      }
    });
  }
}