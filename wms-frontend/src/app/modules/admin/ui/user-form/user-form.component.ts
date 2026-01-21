import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { ManageUsersUseCase } from '../../application/usecases/manage-users.usecase';
import { User } from '../../domain/models/user.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-form.component.html'
})
export class UserFormComponent implements OnChanges {
  private fb = inject(FormBuilder);
  private userUseCase = inject(ManageUsersUseCase);

  @Input() userToEdit: User | null = null; // Si viene null, es CREAR. Si viene objeto, es EDITAR.
  @Output() close = new EventEmitter<boolean>();

  isSubmitting = signal(false);
  isEditMode = signal(false);

  userForm: FormGroup = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(4), Validators.pattern('^[a-zA-Z0-9_]*$')]],
    password: [''], // La validación de required se define dinámicamente
    role: ['OPERATOR', Validators.required]
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['userToEdit']) {
      const user = this.userToEdit;
      
      if (user) {
        // MODO EDICIÓN
        this.isEditMode.set(true);
        this.userForm.patchValue({
          username: user.username,
          role: user.role,
          password: '' 
        });
        this.userForm.get('password')?.clearValidators();
        this.userForm.get('password')?.addValidators([Validators.minLength(6)]); // Solo longitud si escribe algo
      } else {
        this.isEditMode.set(false);
        this.userForm.reset({ role: 'OPERATOR' });
        this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
      }
      this.userForm.get('password')?.updateValueAndValidity();
    }
  }

  hasError(field: string, error: string): boolean {
    const control = this.userForm.get(field);
    return Boolean(control?.hasError(error) && control?.touched);
  }

  onSubmit() {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const formValue = this.userForm.value;

    if (this.isEditMode() && this.userToEdit) {

      const updateCmd = {
        id: this.userToEdit.id,
        username: formValue.username,
        role: formValue.role,
        password: formValue.password || undefined 
      };

      this.userUseCase.updateUser(updateCmd).subscribe({
        next: () => this.handleSuccess('Usuario actualizado'),
        error: () => this.handleError()
      });

    } else {
      this.userUseCase.createUser(formValue).subscribe({
        next: () => this.handleSuccess('Usuario creado'),
        error: () => this.handleError()
      });
    }
  }

  private handleSuccess(msg: string) {
    this.isSubmitting.set(false);
    Swal.fire('Éxito', msg, 'success');
    this.close.emit(true);
  }

  private handleError() {
    this.isSubmitting.set(false);
    Swal.fire('Error', 'Operación fallida. Verifique los datos o si el usuario ya existe.', 'error');
  }

  cancel() {
    this.close.emit(false);
  }
}