import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventoryService } from '../../../core/services/inventory.service';
import Swal from 'sweetalert2'; // <--- Importamos SweetAlert
import { showBackendError } from '../../../shared/utils/error-handler'; // <--- Usamos tu manejador de errores

@Component({
  selector: 'app-stock-movement',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stock-movement.html'
})
export class StockMovement {
  
  private inventoryService = inject(InventoryService);

  data = { lpn: '', locationCode: '' };
  isProcessing = false; 

  onConfirm() {
    if (!this.data.lpn || !this.data.locationCode) {
      Swal.fire('Atención', 'Debes completar el LPN y la Ubicación', 'warning');
      return;
    }

    this.isProcessing = true;
    
    this.inventoryService.confirmPutAway(this.data.lpn, this.data.locationCode)
      .subscribe({
        next: () => {
          this.isProcessing = false;
          
          Swal.fire({
            icon: 'success',
            title: '¡Ubicación Confirmada!',
            text: `El LPN ${this.data.lpn} ahora está disponible en ${this.data.locationCode}`,
            timer: 2500,
            showConfirmButton: false
          });

          this.data = { lpn: '', locationCode: '' }; 
        },
        error: (err) => {
          this.isProcessing = false;
          showBackendError(err, 'Error en Put-Away');
        }
      });
  }
}