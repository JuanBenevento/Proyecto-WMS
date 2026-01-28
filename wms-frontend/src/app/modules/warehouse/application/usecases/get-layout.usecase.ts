import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { LayoutRepository } from '../../domain/repositories/layout.repository';

@Injectable({ providedIn: 'root' })
export class GetLayoutUseCase {
  private repository = inject(LayoutRepository); 

  execute(): Observable<any> {
    return this.repository.getLayout();
  }
}