import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, retry, throwError } from 'rxjs';
import Swal from 'sweetalert2';

/**
 * Retry interceptor for failed requests
 * Automatically retries up to 3 times for network errors or 5xx errors
 */
export const retryInterceptor: HttpInterceptorFn = (req, next) => {
  const shouldRetry = (error: HttpErrorResponse) => {
    // Retry on network errors (status 0) or server errors (500-599)
    return error.status === 0 || (error.status >= 500 && error.status < 600);
  };
  
  return next(req).pipe(
    retry({
      count: 3,
      delay: 1000 // 1 second delay between retries
    }),
    catchError((error: HttpErrorResponse) => {
      if (shouldRetry(error)) {
        console.error('Max retries exhausted for:', req.url);
        Swal.fire({
          icon: 'error',
          title: 'Error de conexión',
          text: 'No se pudo completar la solicitud después de varios intentos.',
          confirmButtonText: 'Aceptar'
        });
      }
      return throwError(() => error);
    })
  );
};