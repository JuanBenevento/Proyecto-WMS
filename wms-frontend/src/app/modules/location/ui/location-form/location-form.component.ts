import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { ManageLocationUseCase } from '../../application/usecases/manage-location.usecase';
import { CreateLocationCommand } from '../../domain/ports/commands/create-location.command';
import { ZoneType } from '../../domain/models/location.model';
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
  
  // Flag para controlar la UI (Rack vs Zona)
  isOperationalZone = false;

  // --- CONSTANTES DE VALIDACIÓN (REGEX) ---
  readonly PATTERN_AISLE = /^[a-zA-Z0-9]+$/;      // Solo letras/números (A, B1)
  readonly PATTERN_NUMERIC = /^[0-9]+$/;          // Solo números (01, 10)
  readonly PATTERN_ZONE_CODE = /^[a-zA-Z0-9-]+$/; // Letras, números y guiones (REC-01)

  // Listas para el Select Agrupado
  storageTypes = [
    { value: ZoneType.DRY_STORAGE, label: 'Almacenamiento Seco' },
    { value: ZoneType.COLD_STORAGE, label: 'Refrigerado (0°C - 5°C)' },
    { value: ZoneType.FROZEN_STORAGE, label: 'Congelado (-18°C)' },
    { value: ZoneType.HAZMAT, label: 'Materiales Peligrosos' }
  ];

  operationalTypes = [
    { value: ZoneType.RECEIVING_AREA, label: 'Recepción / Ingreso' },
    { value: ZoneType.DISPATCH_AREA, label: 'Despacho / Expedición' },
    { value: ZoneType.DOCK_DOOR, label: 'Puerta de Muelle' },
    { value: ZoneType.PICKING_AREA, label: 'Zona de Picking / Armado' },
    { value: ZoneType.YARD, label: 'Patio de Maniobras' }
  ];

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
    } else {
      // Disparar lógica inicial para configurar validadores por defecto
      this.onZoneTypeChange(ZoneType.DRY_STORAGE);
    }
  }

  private initForm(): void {
    this.form = this.fb.group({
      // --- CAMPOS DE RACK (Storage) ---
      // Inicialmente requeridos, se apagan dinámicamente si cambia el tipo
      aislePart: ['', [Validators.required, Validators.pattern(this.PATTERN_AISLE), Validators.maxLength(3)]],
      columnPart: ['', [Validators.required, Validators.pattern(this.PATTERN_NUMERIC), Validators.maxLength(3)]],
      levelPart: ['', [Validators.required, Validators.pattern(this.PATTERN_NUMERIC), Validators.maxLength(2)]],
      
      // --- CAMPO DE ZONA (Operational) ---
      zoneCode: [''], 
      
      // --- COMUNES ---
      zoneType: [ZoneType.DRY_STORAGE, Validators.required],
      maxWeight: [1000, [Validators.required, Validators.min(1), Validators.max(100000)]],
      maxVolume: [1000000, [Validators.required, Validators.min(1)]]
    });

    // Escuchar cambios de tipo para adaptar el formulario (Validadores)
    this.form.get('zoneType')?.valueChanges.subscribe((val) => {
      this.onZoneTypeChange(val);
    });

    // Escuchar cambios en cualquier input para actualizar el preview visual
    this.form.valueChanges.subscribe(() => this.updatePreview());
  }

  /**
   * Cerebro del Formulario: Decide qué validadores encender o apagar
   */
  private onZoneTypeChange(type: ZoneType): void {
    this.isOperationalZone = this.checkIsOperational(type);
    
    const aisleCtrl = this.form.get('aislePart');
    const colCtrl = this.form.get('columnPart');
    const lvlCtrl = this.form.get('levelPart');
    const zoneCodeCtrl = this.form.get('zoneCode');

    // SI ESTAMOS EDITANDO, NO TOCAMOS ESTRUCTURA (Es inmutable)
    if (this.isEditMode) return;

    if (this.isOperationalZone) {
      // --- MODO ZONA OPERATIVA ---
      // 1. Limpiamos Rack
      aisleCtrl?.clearValidators();
      colCtrl?.clearValidators();
      lvlCtrl?.clearValidators();
      
      // Reset visual para no confundir
      aisleCtrl?.setValue('', { emitEvent: false });
      colCtrl?.setValue('', { emitEvent: false });
      lvlCtrl?.setValue('', { emitEvent: false });

      // 2. Encendemos Zona
      zoneCodeCtrl?.setValidators([
        Validators.required, 
        Validators.pattern(this.PATTERN_ZONE_CODE), 
        Validators.minLength(3),
        Validators.maxLength(20)
      ]);

    } else {
      // --- MODO RACK ---
      // 1. Limpiamos Zona
      zoneCodeCtrl?.clearValidators();
      zoneCodeCtrl?.setValue('', { emitEvent: false });

      // 2. Encendemos Rack
      aisleCtrl?.setValidators([Validators.required, Validators.pattern(this.PATTERN_AISLE), Validators.maxLength(3)]);
      colCtrl?.setValidators([Validators.required, Validators.pattern(this.PATTERN_NUMERIC), Validators.maxLength(3)]);
      lvlCtrl?.setValidators([Validators.required, Validators.pattern(this.PATTERN_NUMERIC), Validators.maxLength(2)]);
    }

    // Actualizar estado de validación de los inputs
    aisleCtrl?.updateValueAndValidity({ emitEvent: false });
    colCtrl?.updateValueAndValidity({ emitEvent: false });
    lvlCtrl?.updateValueAndValidity({ emitEvent: false });
    zoneCodeCtrl?.updateValueAndValidity({ emitEvent: false });
    
    this.updatePreview();
  }

  updatePreview(): void {
    const raw = this.form.getRawValue();

    if (this.isOperationalZone) {
        // Preview simple: REC-01
        this.generatedCode = raw.zoneCode ? raw.zoneCode.toUpperCase() : '';
    } else {
        // Preview compuesto: A-01-01
        if (raw.aislePart && raw.columnPart && raw.levelPart) {
            const aisle = raw.aislePart.toUpperCase();
            const col = raw.columnPart.toString().padStart(2, '0');
            const lvl = raw.levelPart.toString().padStart(2, '0');
            this.generatedCode = `${aisle}-${col}-${lvl}`;
        } else {
            this.generatedCode = '';
        }
    }
  }

  private loadLocation(code: string): void {
    this.useCase.getByCode(code).subscribe({
      next: (loc) => {
        // 1. Determinar si es zona operativa o rack basado en los datos que llegan
        // Si tiene 'aisle', es un Rack. Si no, es Zona.
        const isRack = !!loc.aisle;
        
        // 2. Cargar datos
        this.form.patchValue({
          zoneType: loc.zoneType,
          maxWeight: loc.maxWeight,
          maxVolume: loc.maxVolume,
          
          // Mapeo condicional
          aislePart: isRack ? loc.aisle : '',
          columnPart: isRack ? loc.column : '',
          levelPart: isRack ? loc.level : '',
          zoneCode: !isRack ? loc.locationCode : ''
        }, { emitEvent: true }); // Emit true para actualizar UI

        this.generatedCode = loc.locationCode;
        
        // 3. BLOQUEO DE INTEGRIDAD (Inmutable en edición)
        this.form.get('aislePart')?.disable();
        this.form.get('columnPart')?.disable();
        this.form.get('levelPart')?.disable();
        this.form.get('zoneCode')?.disable();
        // El tipo de zona también suele bloquearse para no corromper la lógica del mapa
        this.form.get('zoneType')?.disable(); 

        // 4. VALIDACIÓN DE NEGOCIO (No bajar capacidad menor a lo ocupado)
        this.form.get('maxWeight')?.addValidators(Validators.min(loc.currentWeight));
        this.form.get('maxWeight')?.updateValueAndValidity();
        
        this.form.get('maxVolume')?.addValidators(Validators.min(loc.currentVolume));
        this.form.get('maxVolume')?.updateValueAndValidity();
      },
      error: () => {
        Swal.fire('Error', 'No se pudo cargar la ubicación.', 'error');
        this.router.navigate(['/admin/locations']);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
        this.form.markAllAsTouched(); // Mostrar errores visuales
        return;
    }
    
    const raw = this.form.getRawValue();
    // Usamos el código generado o el target si es edición
    const finalCode = this.isEditMode ? this.targetCode! : this.generatedCode;

    const command: CreateLocationCommand = {
      locationCode: finalCode,
      zoneType: raw.zoneType, // Usamos raw para obtener valor aunque esté disabled
      maxWeight: raw.maxWeight,
      maxVolume: raw.maxVolume
    };

    const request$ = this.isEditMode
      ? this.useCase.update(finalCode, command)
      : this.useCase.create(command);

    request$.subscribe({
      next: () => {
        const msg = this.isEditMode ? 'Ubicación actualizada correctamente.' : 'Ubicación creada correctamente.';
        Swal.fire('Éxito', msg, 'success');
        this.router.navigate(['/admin/locations']);
      },
      error: (err) => {
        const msg = err.error?.message || 'No se pudo guardar la ubicación.';
        Swal.fire('Error Operativo', msg, 'error');
      }
    });
  }

  private checkIsOperational(type: ZoneType): boolean {
    return [
      ZoneType.RECEIVING_AREA, 
      ZoneType.DISPATCH_AREA, 
      ZoneType.DOCK_DOOR, 
      ZoneType.PICKING_AREA, 
      ZoneType.YARD
    ].includes(type);
  }
}