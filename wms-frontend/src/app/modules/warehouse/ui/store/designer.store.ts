import { Injectable, signal } from '@angular/core';
import { VisualData } from '../../domain/models/visual-element.model';

export type InteractionMode = 'SELECT' | 'DRAW_WALL';

@Injectable()
export class DesignerStore {
  readonly selectedObject = signal<any | null>(null);
  readonly activeData = signal<VisualData | null>(null);
  
  readonly interactionMode = signal<InteractionMode>('SELECT');

  selectObject(obj: any | null) {
    this.selectedObject.set(obj);
    this.activeData.set(obj ? (obj.data as VisualData) : null);
  }

  updateActiveData(partial: Partial<VisualData>) {
    const current = this.activeData();
    if (current) {
      this.activeData.set({ ...current, ...partial });
    }
  }

  setInteractionMode(mode: InteractionMode) {
    this.interactionMode.set(mode);
    if (mode !== 'SELECT') {
      this.selectObject(null);
    }
  }
}