import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ManageProductUseCase } from '../../application/usecases/manage-product.usecase';
import Swal from 'sweetalert2';
import { CreateProductCommand } from '../../domain/ports/commands/create-product.command';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './product-form.component.html'
})
export class ProductFormComponent implements OnInit {
  form!: FormGroup;
  isEditMode = false;
  skuParam: string | null = null;

  constructor(
    private fb: FormBuilder,
    private manageProductUseCase: ManageProductUseCase,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.skuParam = this.route.snapshot.paramMap.get('sku');
    if (this.skuParam) {
      this.isEditMode = true;
      this.loadProduct(this.skuParam);
    }
  }

  private initForm(): void {
    this.form = this.fb.group({
      sku: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9-]+$/)]],
      name: ['', Validators.required],
      description: [''],
      width: [0, [Validators.required, Validators.min(0.01)]],
      height: [0, [Validators.required, Validators.min(0.01)]],
      depth: [0, [Validators.required, Validators.min(0.01)]],
      weight: [0, [Validators.required, Validators.min(0.01)]]
    });
  }

  private loadProduct(sku: string): void {
    this.form.get('sku')?.disable();
    this.manageProductUseCase.getBySku(sku).subscribe(p => this.form.patchValue(p));
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const rawValue = this.form.getRawValue();
    const command: CreateProductCommand = {
      ...rawValue,
      sku: rawValue.sku.toUpperCase() 
    };

    const request$ = this.isEditMode && this.skuParam
      ? this.manageProductUseCase.update(this.skuParam, command)
      : this.manageProductUseCase.create(command);

    request$.subscribe({
      next: () => this.router.navigate(['/admin/products']),
      error: (err) => {
        if (err.status === 409) Swal.fire('Error', 'Conflicto de SKU o Stock.', 'error');
        else Swal.fire('Error', 'No se pudo guardar.', 'error');
      }
    });
  }
}