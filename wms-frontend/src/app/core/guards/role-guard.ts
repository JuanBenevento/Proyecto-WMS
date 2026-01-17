import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import Swal from 'sweetalert2';


export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  const user = authService.currentUser();
  const requiredRole = route.data['role'] as string;

  if (!user) {
    router.navigate(['/login']);
    return false;
  }

  if (user.role === 'SUPER_ADMIN') return true; 
  if (user.role === requiredRole) return true;

  Swal.fire({
    icon: 'error',
    title: 'Acceso Denegado',
    text: `Esta sección es exclusiva para: ${requiredRole}`,
    confirmButtonColor: '#d33'
  });

  if (user.role === 'ADMIN') router.navigate(['/admin/dashboard']);
  else if (user.role === 'OPERATOR') router.navigate(['/operation/receive']);
  
  return false;
};