import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LocationService } from '../../../core/services/location.service';
import { Location } from '../../../core/models/location.model';
import Swal from 'sweetalert2'; 

@Component({
  selector: 'app-warehouse-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './warehouse-map.html',
  styles: [`
    .location-card { transition: transform 0.2s; }
    .location-card:hover { transform: scale(1.05); cursor: pointer; z-index: 10; }
  `]
})
export class WarehouseMapComponent implements OnInit {
  
  private locationService = inject(LocationService);
  locations: Location[] = [];

  ngOnInit() {
    this.loadMap();
  }

  loadMap() {
    this.locationService.getLocations().subscribe(data => {
      this.locations = data.sort((a, b) => a.locationCode.localeCompare(b.locationCode));
    });
  }

  showContent(loc: Location) {
    if (!loc.items || loc.items.length === 0) {
      Swal.fire({
        title: `Ubicación ${loc.locationCode}`,
        text: 'Esta ubicación está vacía (0% Ocupación)',
        icon: 'info',
        confirmButtonText: 'Entendido',
        confirmButtonColor: '#64748b'
      });
      return;
    }

    const itemsHtml = loc.items.map(item => `
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 10px; border-bottom: 1px solid #eee;">
        <div style="text-align: left;">
          <div style="font-weight: bold; color: #1e293b;">${item.sku}</div>
          <div style="font-size: 0.8em; color: #64748b; font-family: monospace;">${item.lpn}</div>
        </div>
        <div style="font-weight: bold; color: #4f46e5;">${item.quantity} un.</div>
      </div>
    `).join('');

    Swal.fire({
      title: `Contenido de ${loc.locationCode}`,
      html: `<div style="max-height: 300px; overflow-y: auto;">${itemsHtml}</div>`,
      confirmButtonText: 'Cerrar',
      confirmButtonColor: '#4f46e5'
    });
  }

  getOccupancyPercentage(loc: Location): number {
    if (loc.maxWeight === 0) return 0;
    const pct = (loc.currentWeight / loc.maxWeight) * 100;
    return Math.min(pct, 100); 
  }

  getProgressColor(pct: number): string {
    if (pct < 50) return 'bg-success'; 
    if (pct < 90) return 'bg-warning';
    return 'bg-danger';               
  }
}