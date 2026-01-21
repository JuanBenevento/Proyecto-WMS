import { Component } from '@angular/core';

@Component({
  standalone: true,
  template: `
    <div class="p-6 bg-white rounded-xl shadow-sm border border-slate-200">
      <h2 class="text-xl font-bold text-slate-800">🗺️ Mapa del Depósito</h2>
      <p class="text-slate-500">Aquí se visualizarán los racks y ubicaciones.</p>
    </div>
  `
})
export class WarehouseMapComponent {}