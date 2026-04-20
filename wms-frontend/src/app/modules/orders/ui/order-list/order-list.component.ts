import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ManageOrderUseCase } from '../../application/usecases/manage-order.usecase';
import { Order, OrderFilters, OrderStatus, OrderPriority } from '../../domain/models/order.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './order-list.component.html'
})
export class OrderListComponent implements OnInit {
  Math = Math; // Para usar en template
  orders: Order[] = [];
  totalItems = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 20;

  // Filtros
  filters: OrderFilters = {
    status: undefined,
    priority: undefined,
    customerId: undefined,
    warehouseId: undefined,
    page: 0,
    size: 20
  };

  // Opciones para filtros
  statusOptions = [
    { value: '', label: 'Todos los estados' },
    { value: 'CREATED', label: 'Creada' },
    { value: 'CONFIRMED', label: 'Confirmada' },
    { value: 'PENDING', label: 'Pendiente' },
    { value: 'ALLOCATED', label: 'Asignada' },
    { value: 'PICKING', label: 'En Picking' },
    { value: 'PACKED', label: 'Empacada' },
    { value: 'SHIPPED', label: 'Enviada' },
    { value: 'DELIVERED', label: 'Entregada' },
    { value: 'HOLD', label: 'En Espera' },
    { value: 'CANCELLED', label: 'Cancelada' }
  ];

  priorityOptions = [
    { value: '', label: 'Todas las prioridades' },
    { value: 'HIGH', label: 'Alta' },
    { value: 'MEDIUM', label: 'Media' },
    { value: 'LOW', label: 'Baja' }
  ];

  loading = false;

  constructor(private manageOrderUseCase: ManageOrderUseCase) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.filters.page = this.currentPage;
    this.filters.size = this.pageSize;

    this.manageOrderUseCase.listOrders(this.filters).subscribe({
      next: (response) => {
        this.orders = response.orders;
        this.totalItems = response.totalItems;
        this.totalPages = response.totalPages;
        this.currentPage = response.currentPage;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading orders:', err);
        this.loading = false;
        Swal.fire('Error', 'No se pudieron cargar las órdenes.', 'error');
      }
    });
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadOrders();
  }

  clearFilters(): void {
    this.filters = {
      status: undefined,
      priority: undefined,
      customerId: undefined,
      warehouseId: undefined,
      page: 0,
      size: 20
    };
    this.currentPage = 0;
    this.loadOrders();
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadOrders();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadOrders();
    }
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

  getPriorityClass(priority: string): string {
    const classes: Record<string, string> = {
      'HIGH': 'text-red-600 bg-red-50',
      'MEDIUM': 'text-yellow-600 bg-yellow-50',
      'LOW': 'text-green-600 bg-green-50'
    };
    return classes[priority] || 'text-gray-600 bg-gray-50';
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-CO');
  }
}