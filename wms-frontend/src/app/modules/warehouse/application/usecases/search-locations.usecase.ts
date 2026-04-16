import { Injectable } from '@angular/core';
import { LayoutRepository } from '../../domain/repositories/layout.repository';

@Injectable({ providedIn: 'root' })
export class SearchLocationsUseCase {
  constructor(private repo: LayoutRepository) {}

  execute(query: string, type: 'RACK' | 'ZONE') {
    return this.repo.searchLocations(query, type);
  }
}