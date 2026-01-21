import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ManageLocationUseCase } from '../../application/usecases/manage-location.usecase';
import { LocationModel } from '../../domain/models/location.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-location-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './location-list.component.html'
})
export class LocationListComponent implements OnInit {
  locations: LocationModel[] = [];

  constructor(private useCase: ManageLocationUseCase) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.useCase.getAll().subscribe(data => this.locations = data);
  }

  deleteLocation(code: string): void {
    Swal.fire({
      title: '¿Eliminar ubicación?',
      text: `Se eliminará ${code}. Solo es posible si está vacía.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar'
    }).then((result) => {
      if (result.isConfirmed) {
        this.useCase.delete(code).subscribe({
          next: () => {
            Swal.fire('Eliminado', 'Ubicación eliminada.', 'success');
            this.loadData();
          },
          error: () => Swal.fire('Error', 'No se pudo eliminar. Verifique que no tenga stock.', 'error')
        });
      }
    });
  }

  getOccupancyColorBg(percentage: number): string {
    if (percentage > 0.9) return 'bg-red-500';
    if (percentage > 0.7) return 'bg-yellow-500';
    return 'bg-green-500';
  }

  getOccupancyColorText(percentage: number): string {
    if (percentage > 0.9) return 'text-red-600';
    if (percentage > 0.7) return 'text-yellow-600';
    return 'text-green-600';
  }
}