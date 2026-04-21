import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ManageOrderUseCase } from '../../application/usecases/manage-order.usecase';
import { CreateOrderCommand } from '../../domain/ports/repository/order.repository';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-order-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './order-form.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrderFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;

  priorityOptions = [
    { value: 'HIGH', label: 'Alta' },
    { value: 'MEDIUM', label: 'Media' },
    { value: 'LOW', label: 'Baja' }
  ];

  constructor(
    private fb: FormBuilder,
    private manageOrderUseCase: ManageOrderUseCase,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  get lines(): FormArray {
    return this.form.get('lines') as FormArray;
  }

  private initForm(): void {
    this.form = this.fb.group({
      customerId: ['', Validators.required],
      customerName: ['', Validators.required],
      customerEmail: [''],
      shippingAddress: ['', Validators.required],
      priority: ['MEDIUM'],
      promisedShipDate: [''],
      promisedDeliveryDate: [''],
      warehouseId: ['', Validators.required],
      notes: [''],
      lines: this.fb.array([this.createLineGroup()])
    });
  }

  private createLineGroup(): FormGroup {
    return this.fb.group({
      productSku: ['', Validators.required],
      requestedQuantity: [1, [Validators.required, Validators.min(1)]],
      promisedDeliveryDate: [''],
      notes: ['']
    });
  }

  addLine(): void {
    this.lines.push(this.createLineGroup());
  }

  removeLine(index: number): void {
    if (this.lines.length > 1) {
      this.lines.removeAt(index);
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      Swal.fire('Error', 'Por favor completa todos los campos requeridos.', 'error');
      return;
    }

    this.loading = true;
    const rawValue = this.form.getRawValue();

    const command: CreateOrderCommand = {
      customerId: rawValue.customerId,
      customerName: rawValue.customerName,
      customerEmail: rawValue.customerEmail,
      shippingAddress: rawValue.shippingAddress,
      priority: rawValue.priority,
      promisedShipDate: rawValue.promisedShipDate || undefined,
      promisedDeliveryDate: rawValue.promisedDeliveryDate || undefined,
      warehouseId: rawValue.warehouseId,
      notes: rawValue.notes || undefined,
      lines: rawValue.lines.map((line: any) => ({
        productSku: line.productSku,
        requestedQuantity: line.requestedQuantity,
        promisedDeliveryDate: line.promisedDeliveryDate || undefined,
        notes: line.notes || undefined
      }))
    };

    this.manageOrderUseCase.createOrder(command).subscribe({
      next: (order) => {
        this.loading = false;
        Swal.fire('Éxito', `Orden ${order.orderNumber} creada.`, 'success');
        this.router.navigate(['/orders']);
      },
      error: (err) => {
        this.loading = false;
        const message = err.error?.message || 'No se pudo crear la orden.';
        Swal.fire('Error', message, 'error');
      }
    });
  }
}