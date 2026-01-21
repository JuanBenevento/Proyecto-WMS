import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserFormComponent } from '../user-form/user-form.component'; 
import { ManageUsersUseCase } from '../../application/usecases/manage-users.usecase';
import { User } from '../../domain/models/user.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, UserFormComponent], 
  template: `
    <div class="space-y-6 relative">
      
      <div class="flex justify-between items-center bg-white p-6 rounded-xl shadow-sm border border-slate-200">
        <div>
          <h2 class="text-xl font-bold text-slate-800">Gestión de Personal</h2>
          <p class="text-slate-500 text-sm">Administra los accesos de tu empresa</p>
        </div>
        <button (click)="openModal(null)" 
                class="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg font-bold text-sm flex items-center gap-2 transition-colors shadow-md">
          <i class="bi bi-person-plus-fill"></i> Nuevo Empleado
        </button>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <table class="w-full text-sm text-left">
          <thead class="bg-slate-50 text-slate-500 uppercase text-xs border-b border-slate-100">
            <tr>
              <th class="px-6 py-4 font-bold">Usuario</th>
              <th class="px-6 py-4 font-bold">Rol</th>
              <th class="px-6 py-4 font-bold text-right">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
            @for (user of users(); track user.id) {
              <tr class="hover:bg-slate-50 transition-colors">
                
                <td class="px-6 py-4">
                  <div class="flex items-center gap-3">
                    <div class="w-9 h-9 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center text-slate-600 font-bold shadow-sm">
                        {{ user.username.charAt(0) | uppercase }}
                    </div>
                    <div>
                      <div class="font-bold text-slate-700">{{ user.username }}</div>
                    </div>
                  </div>
                </td>

                <td class="px-6 py-4">
                  <span class="px-3 py-1 rounded-full text-[10px] font-bold border tracking-wider uppercase"
                        [ngClass]="{
                          'bg-indigo-50 text-indigo-700 border-indigo-200': user.role === 'ADMIN',
                          'bg-emerald-50 text-emerald-700 border-emerald-200': user.role === 'OPERATOR',
                          'bg-amber-50 text-amber-700 border-amber-200': user.role === 'SUPER_ADMIN'
                        }">
                    {{ user.role }}
                  </span>
                </td>

                <td class="px-6 py-4 text-right flex justify-end gap-2">
                  
                  <button (click)="openModal(user)" 
                          class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 transition-all"
                          title="Editar">
                    <i class="bi bi-pencil-square"></i>
                  </button>

                  <button (click)="deleteUser(user)" 
                          class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition-all"
                          title="Eliminar">
                    <i class="bi bi-trash3"></i>
                  </button>
                </td>

              </tr>
            } @empty {
              <tr>
                <td colspan="3" class="text-center py-12 text-slate-400">
                  <i class="bi bi-inbox text-3xl mb-2 block opacity-50"></i>
                  No hay usuarios registrados.
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>

      @if (isModalOpen()) {
        <app-user-form 
            [userToEdit]="selectedUser()" 
            (close)="handleModalClose($event)">
        </app-user-form>
      }

    </div>
  `
})
export class UserManagementComponent implements OnInit {

  private userUseCase = inject(ManageUsersUseCase);
 
  users = signal<User[]>([]);        
  isModalOpen = signal(false);        
  selectedUser = signal<User | null>(null); 

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.userUseCase.getUsers().subscribe({
      next: (data) => this.users.set(data),
      error: (e) => console.error('Error cargando usuarios', e)
    });
  }

  openModal(user: User | null) {
    this.selectedUser.set(user); 
    this.isModalOpen.set(true);  
  }

  handleModalClose(refresh: boolean) {
    this.isModalOpen.set(false); 
    this.selectedUser.set(null); 
    
    if (refresh) {
      this.loadUsers();
    }
  }
  deleteUser(user: User) {
    Swal.fire({
      title: '¿Estás seguro?',
      text: `Se eliminará permanentemente a: ${user.username}`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#f43f5e', 
      cancelButtonColor: '#64748b', 
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        this.userUseCase.deleteUser(user.id).subscribe({
          next: () => {
            this.loadUsers(); 
            const Toast = Swal.mixin({
              toast: true, position: 'top-end', showConfirmButton: false, timer: 3000,
              timerProgressBar: true
            });
            Toast.fire({ icon: 'success', title: 'Usuario eliminado' });
          },
          error: () => Swal.fire('Error', 'No se pudo eliminar el usuario', 'error')
        });
      }
    });
  }
}