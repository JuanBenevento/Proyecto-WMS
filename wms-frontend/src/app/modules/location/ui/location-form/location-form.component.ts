import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { ManageLocationUseCase } from '../../application/usecases/manage-location.usecase';
import { CreateLocationCommand } from '../../domain/ports/commands/create-location.command';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-location-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './location-form.component.html'
})
export class LocationFormComponent implements OnInit {
  form!: FormGroup;
  isEditMode = false;
  targetCode: string | null = null;
  generatedCode = '';

  constructor(
    private fb: FormBuilder,
    private useCase: ManageLocationUseCase,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.targetCode = this.route.snapshot.paramMap.get('code');
    
    if (this.targetCode) {
      this.isEditMode = true;
      this.loadLocation(this.targetCode);
    }
  }

  private initForm(): void {
    this.form = this.fb.group({
      aislePart: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9]+$/)]],
      rackPart: ['', [Validators.required]],
      levelPart: ['', [Validators.required]],
      
      zoneType: ['DRY_STORAGE', Validators.required],
      maxWeight: [1000, [Validators.required, Validators.min(1)]],
      maxVolume: [1000000, [Validators.required, Validators.min(1)]]
    });
  }

  updatePreview(): void {
    const { aislePart, rackPart, levelPart } = this.form.getRawValue();
    if (aislePart && rackPart && levelPart) {
      this.generatedCode = `${aislePart}-${rackPart}-${levelPart}`.toUpperCase();
    } else {
      this.generatedCode = '';
    }
  }

  private loadLocation(code: string): void {
    this.useCase.getByCode(code).subscribe(loc => {
      const parts = loc.locationCode.split('-');
      if (parts.length >= 3) {
        this.form.patchValue({
          aislePart: parts[0],
          rackPart: parts[1],
          levelPart: parts[2]
        });
      }
      
      this.form.patchValue({
        zoneType: loc.zoneType,
        maxWeight: loc.maxWeight,
        maxVolume: loc.maxVolume
      });

      this.generatedCode = loc.locationCode;
      
      this.form.get('aislePart')?.disable();
      this.form.get('rackPart')?.disable();
      this.form.get('levelPart')?.disable();
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    
    const raw = this.form.getRawValue();
    const finalCode = this.generatedCode; 

    const command: CreateLocationCommand = {
      locationCode: finalCode,
      zoneType: raw.zoneType,
      maxWeight: raw.maxWeight,
      maxVolume: raw.maxVolume
    };

    const request$ = this.isEditMode && this.targetCode
      ? this.useCase.update(this.targetCode, command)
      : this.useCase.create(command);

    request$.subscribe({
      next: () => this.router.navigate(['/admin/locations']),
      error: (err) => {
        Swal.fire('Error', 'No se pudo guardar. Verifica capacidades o si el código ya existe.', 'error');
      }
    });
  }
}