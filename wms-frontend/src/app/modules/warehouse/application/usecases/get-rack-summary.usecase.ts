import { Injectable } from '@angular/core';
import { LayoutRepository } from '../../domain/repositories/layout.repository';

@Injectable({ providedIn: 'root' })
export class GetRackSummaryUseCase {
  constructor(private repo: LayoutRepository) {}
  execute(code: string) { return this.repo.getRackSummary(code); }
}