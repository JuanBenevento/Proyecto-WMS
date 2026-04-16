import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReceiveInventoryUseCase } from '../../application/usecases/inbound/receive-inventory.usecase';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-inventory-receive',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './inventory-receive.component.html'
})
export class InventoryReceiveComponent {
  private fb = inject(FormBuilder);
  private receiveUseCase = inject(ReceiveInventoryUseCase);
  
  isLoading = false;
  lastLpn: string | null = null;

  form: FormGroup = this.fb.group({
    productSku: ['', Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    batchNumber: ['', Validators.required], // El usuario debe escribir o escanear
    expiryDate: [new Date().toISOString().split('T')[0], Validators.required], // Hoy por defecto
    locationCode: ['DOCK', Validators.required] // Por defecto entra al Muelle
  });

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const command = this.form.getRawValue();

    this.receiveUseCase.execute(command).subscribe({
      next: (item) => {
        this.isLoading = false;
        this.lastLpn = item.lpn;
        
        // Feedback Visual
        Swal.fire({
          icon: 'success',
          title: 'Recepción Exitosa',
          text: `Se generó el LPN: ${item.lpn}`,
          timer: 2000,
          showConfirmButton: false
        });

        this.resetFormForNextScan();
      },
      error: (err) => {
        this.isLoading = false;
        console.error(err);
        Swal.fire('Error', 'No se pudo recibir la mercadería. Verifique el SKU.', 'error');
      }
    });
  }

  private resetFormForNextScan() {
    // Mantenemos batch y fecha para agilizar ingresos masivos del mismo lote
    // Solo reseteamos cantidad y SKU
    this.form.patchValue({
      productSku: '',
      quantity: 1
    });
    // Aquí podrías usar un ViewChild para volver a enfocar el input de SKU
  }
}