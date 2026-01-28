import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { LayoutRepository } from '../../domain/repositories/layout.repository';

@Injectable({ providedIn: 'root' })
export class SaveLayoutUseCase {
  private repository = inject(LayoutRepository);

  execute(json: string): Observable<void> {
    if (!json || json === '{}' || json === '{"objects":[]}') {
       throw new Error('El diseño está vacío, agrega elementos antes de guardar.');
    }
    return this.repository.saveLayout(json);
  }
}