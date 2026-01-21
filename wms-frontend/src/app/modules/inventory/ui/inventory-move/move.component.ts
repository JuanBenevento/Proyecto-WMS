import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MoveInventoryUseCase } from '../../application/usecases/internal/move-inventory.usecase';
import { SuggestLocationUseCase } from '../../application/usecases/internal/suggest-location.usecase';
import Swal from 'sweetalert2';
import { InventoryItemModel } from '../../domain/models/inventory-item.model';

@Component({
  selector: 'app-inventory-move',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './inventory-move.component.html'
})
export class InventoryMoveComponent {
  private fb = inject(FormBuilder);
  private moveUseCase = inject(MoveInventoryUseCase);
  private suggestUseCase = inject(SuggestLocationUseCase);

  form: FormGroup = this.fb.group({
    lpn: ['', Validators.required],
    targetLocationCode: ['', Validators.required],
    reason: [''] // Opcional
  });

  scannedItem: InventoryItemModel | null = null;
  suggestion: string | null = null;
  isLoading = false;

  // Se ejecuta cuando el usuario hace "Blur" (sale) del campo LPN o presiona Enter
  onScanLpn() {
    const lpn = this.form.get('lpn')?.value;
    if (!lpn) return;

    this.isLoading = true; // Feedback sutil

    // 1. Obtener info del LPN para mostrar al operario
    this.moveUseCase.getLpnInfo(lpn).subscribe({
      next: (item) => {
        this.scannedItem = item;
        this.isLoading = false;
        
        // 2. Pedir sugerencia inteligente al backend
        this.getSuggestion(item.sku, item.quantity);
      },
      error: () => {
        this.isLoading = false;
        this.scannedItem = null;
        Swal.fire('Atención', 'LPN no encontrado o no disponible.', 'warning');
      }
    });
  }

  private getSuggestion(sku: string, qty: number) {
    this.suggestUseCase.execute(sku, qty).subscribe({
      next: (loc) => this.suggestion = loc,
      error: () => this.suggestion = null // Si no hay sugerencia, no mostramos nada
    });
  }

  applySuggestion() {
    if (this.suggestion) {
      this.form.patchValue({ targetLocationCode: this.suggestion });
    }
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.isLoading = true;
    const command = this.form.getRawValue();

    // Aquí usamos executeMove, pero en el backend es la misma lógica de validación
    this.moveUseCase.executeMove(command).subscribe({
      next: () => {
        this.isLoading = false;
        Swal.fire({
          icon: 'success',
          title: 'Movimiento Confirmado',
          text: `LPN movido a ${command.targetLocationCode}`,
          timer: 1500,
          showConfirmButton: false
        });
        this.reset();
      },
      error: (err) => {
        this.isLoading = false;
        Swal.fire('Error', 'No se pudo mover. Verifique capacidad o bloqueos.', 'error');
      }
    });
  }

  reset() {
    this.form.reset();
    this.scannedItem = null;
    this.suggestion = null;
  }
}