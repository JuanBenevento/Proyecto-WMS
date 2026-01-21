import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AllocateStockUseCase } from '../../application/usecases/outbound/allocate-stock.usecase';
import { ShipStockUseCase } from '../../application/usecases/outbound/ship-stock.usecase';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-inventory-dispatch',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './inventory-dispatch.component.html'
})
export class InventoryDispatchComponent {
  private fb = inject(FormBuilder);
  private allocateUseCase = inject(AllocateStockUseCase);
  private shipUseCase = inject(ShipStockUseCase);

  mode: 'ALLOCATE' | 'SHIP' = 'ALLOCATE'; // Estado inicial
  isLoading = false;

  form: FormGroup = this.fb.group({
    sku: ['', Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]]
  });

  setMode(newMode: 'ALLOCATE' | 'SHIP') {
    this.mode = newMode;
    // Opcional: Resetear form al cambiar modo
    // this.form.reset({ quantity: 1 }); 
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.isLoading = true;
    const command = this.form.getRawValue();

    // Polimorfismo simple: elegimos el caso de uso según el modo
    const useCase$ = this.mode === 'ALLOCATE'
      ? this.allocateUseCase.execute(command)
      : this.shipUseCase.execute(command);

    useCase$.subscribe({
      next: (message) => {
        this.isLoading = false;
        
        // Mensaje diferente según acción
        const title = this.mode === 'ALLOCATE' ? 'Stock Reservado' : 'Despacho Confirmado';
        
        Swal.fire({
          icon: 'success',
          title: title,
          text: message, // Mensaje que viene del backend
        });

        this.form.reset({ quantity: 1 }); // Limpiar para el siguiente
      },
      error: (err) => {
        this.isLoading = false;
        // Manejo de errores específicos (Ej: Stock insuficiente)
        const msg = err.error?.message || 'Ocurrió un error en la operación.';
        Swal.fire('Error Operativo', msg, 'error');
      }
    });
  }
}