import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ManageOrderUseCase } from '../../application/usecases/manage-order.usecase';
import { Order } from '../../domain/models/order.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './order-detail.component.html'
})
export class OrderDetailComponent implements OnInit {
  order: Order | null = null;
  loading = false;
  actionLoading = false;

  constructor(
    private route: ActivatedRoute,
    private manageOrderUseCase: ManageOrderUseCase
  ) {}

  ngOnInit(): void {
    const orderId = this.route.snapshot.paramMap.get('orderId');
    if (orderId) {
      this.loadOrder(orderId);
    }
  }

  loadOrder(orderId: string): void {
    this.loading = true;
    this.manageOrderUseCase.getOrder(orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading order:', err);
        this.loading = false;
        Swal.fire('Error', 'No se pudo cargar la orden.', 'error');
      }
    });
  }

  // Acciones de transición de estado
  confirmOrder(): void {
    if (!this.order) return;
    this.actionLoading = true;
    this.manageOrderUseCase.confirmOrder(this.order.orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.actionLoading = false;
        Swal.fire('Éxito', 'Orden confirmada.', 'success');
      },
      error: (err) => this.handleError(err)
    });
  }

  cancelOrder(): void {
    if (!this.order) return;
    Swal.fire({
      title: 'Cancelar Orden',
      input: 'textarea',
      inputPlaceholder: 'Ingresa la razón de cancelación...',
      showCancelButton: true,
      confirmButtonText: 'Sí, cancelar',
      cancelButtonText: 'No'
    }).then((result) => {
      if (result.isConfirmed && result.value) {
        this.actionLoading = true;
        this.manageOrderUseCase.cancelOrder(this.order!.orderId, result.value).subscribe({
          next: (order) => {
            this.order = order;
            this.actionLoading = false;
            Swal.fire('Cancelada', 'Orden cancelada.', 'success');
          },
          error: (err) => this.handleError(err)
        });
      }
    });
  }

  holdOrder(): void {
    if (!this.order) return;
    this.actionLoading = true;
    this.manageOrderUseCase.holdOrder(this.order.orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.actionLoading = false;
        Swal.fire('Éxito', 'Orden en espera.', 'success');
      },
      error: (err) => this.handleError(err)
    });
  }

  releaseOrder(): void {
    if (!this.order) return;
    this.actionLoading = true;
    this.manageOrderUseCase.releaseOrder(this.order.orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.actionLoading = false;
        Swal.fire('Éxito', 'Orden liberada.', 'success');
      },
      error: (err) => this.handleError(err)
    });
  }

  startPicking(): void {
    if (!this.order) return;
    this.actionLoading = true;
    this.manageOrderUseCase.startPicking(this.order.orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.actionLoading = false;
        Swal.fire('Éxito', 'Picking iniciado.', 'success');
      },
      error: (err) => this.handleError(err)
    });
  }

  packOrder(): void {
    if (!this.order) return;
    this.actionLoading = true;
    this.manageOrderUseCase.packOrder(this.order.orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.actionLoading = false;
        Swal.fire('Éxito', 'Orden empacada.', 'success');
      },
      error: (err) => this.handleError(err)
    });
  }

  shipOrder(): void {
    if (!this.order) return;
    Swal.fire({
      title: 'Enviar Orden',
      html: `
        <input id="carrierId" class="swal2-input" placeholder="Carrier ID (obligatorio)" required>
        <input id="trackingNumber" class="swal2-input" placeholder="Tracking Number (opcional)">
      `,
      showCancelButton: true,
      confirmButtonText: 'Enviar',
      cancelButtonText: 'Cancelar',
      preConfirm: () => {
        const carrierId = (document.getElementById('carrierId') as HTMLInputElement).value;
        const trackingNumber = (document.getElementById('trackingNumber') as HTMLInputElement).value;
        if (!carrierId) {
          Swal.showValidationMessage('Carrier ID es obligatorio');
          return false;
        }
        return { carrierId, trackingNumber };
      }
    }).then((result) => {
      if (result.isConfirmed && result.value) {
        this.actionLoading = true;
        this.manageOrderUseCase.shipOrder(this.order!.orderId, result.value.carrierId, result.value.trackingNumber).subscribe({
          next: (order) => {
            this.order = order;
            this.actionLoading = false;
            Swal.fire('Enviada', 'Orden enviada al carrier.', 'success');
          },
          error: (err) => this.handleError(err)
        });
      }
    });
  }

  deliverOrder(): void {
    if (!this.order) return;
    this.actionLoading = true;
    this.manageOrderUseCase.deliverOrder(this.order.orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.actionLoading = false;
        Swal.fire('Éxito', 'Orden entregada.', 'success');
      },
      error: (err) => this.handleError(err)
    });
  }

  // Determina qué acciones están disponibles según el estado
  getAvailableActions(): { key: string; label: string; icon: string; action: () => void; variant: string }[] {
    if (!this.order) return [];

    const actions: { key: string; label: string; icon: string; action: () => void; variant: string }[] = [];

    switch (this.order.status) {
      case 'CREATED':
        actions.push(
          { key: 'confirm', label: 'Confirmar', icon: 'check', action: () => this.confirmOrder(), variant: 'primary' },
          { key: 'cancel', label: 'Cancelar', icon: 'x', action: () => this.cancelOrder(), variant: 'danger' }
        );
        break;
      case 'CONFIRMED':
        actions.push(
          { key: 'hold', label: 'En Espera', icon: 'pause', action: () => this.holdOrder(), variant: 'warning' },
          { key: 'cancel', label: 'Cancelar', icon: 'x', action: () => this.cancelOrder(), variant: 'danger' }
        );
        break;
      case 'PENDING':
        // Solo allocate (del lado del sistema)
        break;
      case 'ALLOCATED':
        actions.push(
          { key: 'pick', label: 'Iniciar Picking', icon: 'package', action: () => this.startPicking(), variant: 'primary' },
          { key: 'hold', label: 'En Espera', icon: 'pause', action: () => this.holdOrder(), variant: 'warning' }
        );
        break;
      case 'PICKING':
        actions.push(
          { key: 'pack', label: 'Empacar', icon: 'box', action: () => this.packOrder(), variant: 'primary' }
        );
        break;
      case 'PACKED':
        actions.push(
          { key: 'ship', label: 'Enviar', icon: 'truck', action: () => this.shipOrder(), variant: 'primary' }
        );
        break;
      case 'SHIPPED':
        actions.push(
          { key: 'deliver', label: 'Entregar', icon: 'check-circle', action: () => this.deliverOrder(), variant: 'success' }
        );
        break;
      case 'HOLD':
        actions.push(
          { key: 'release', label: 'Liberar', icon: 'play', action: () => this.releaseOrder(), variant: 'primary' },
          { key: 'cancel', label: 'Cancelar', icon: 'x', action: () => this.cancelOrder(), variant: 'danger' }
        );
        break;
    }

    return actions;
  }

  getStatusClass(status: string): string {
    const classes: Record<string, string> = {
      'CREATED': 'bg-blue-50 text-blue-700 border-blue-100',
      'CONFIRMED': 'bg-indigo-50 text-indigo-700 border-indigo-100',
      'PENDING': 'bg-yellow-50 text-yellow-700 border-yellow-100',
      'ALLOCATED': 'bg-purple-50 text-purple-700 border-purple-100',
      'PICKING': 'bg-orange-50 text-orange-700 border-orange-100',
      'PACKED': 'bg-teal-50 text-teal-700 border-teal-100',
      'SHIPPED': 'bg-cyan-50 text-cyan-700 border-cyan-100',
      'DELIVERED': 'bg-green-50 text-green-700 border-green-100',
      'HOLD': 'bg-gray-50 text-gray-700 border-gray-100',
      'CANCELLED': 'bg-red-50 text-red-700 border-red-100'
    };
    return classes[status] || 'bg-gray-50 text-gray-700 border-gray-100';
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('es-CO');
  }

  formatDateTime(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString('es-CO');
  }

  private handleError(err: any): void {
    this.actionLoading = false;
    const message = err.error?.message || 'Error en la operación.';
    Swal.fire('Error', message, 'error');
  }
}