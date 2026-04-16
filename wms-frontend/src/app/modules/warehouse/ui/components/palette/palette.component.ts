import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-palette',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './palette.component.html' 
})
export class PaletteComponent {
  @Output() dragStart = new EventEmitter<DragEvent>();

  tools = [
    { 
      type: 'RACK', 
      label: 'Rack', 
      icon: 'bi bi-grid-3x3', 
      class: 'bg-indigo-50 border-indigo-100 text-indigo-600 hover:border-indigo-500' 
    },
    { 
      type: 'ZONE', 
      label: 'Zona', 
      icon: 'bi bi-bounding-box', 
      class: 'bg-emerald-50 border-emerald-100 text-emerald-600 hover:border-emerald-500' 
    },
    { 
      type: 'WALL', 
      label: 'Muro', 
      icon: 'bi bi-bricks', 
      class: 'bg-slate-50 border-slate-100 text-slate-600 hover:border-slate-500' 
    },
  ];

  onDragStart(e: DragEvent, type: string) {
    if (e.dataTransfer) {
        e.dataTransfer.setData('type', type);
        e.dataTransfer.effectAllowed = 'copy'; 
    }
    this.dragStart.emit(e);
}
}