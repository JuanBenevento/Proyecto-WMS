import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { GetInventoryUseCase } from '../../application/usecases/query/get-inventory.usecase';
import { debounceTime } from 'rxjs';
import { InventoryItemModel } from '../../domain/models/inventory-item.model';

@Component({
  selector: 'app-inventory-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './inventory-list.component.html'
})
export class InventoryListComponent implements OnInit {
  private getInventoryUseCase = inject(GetInventoryUseCase);

  items: InventoryItemModel[] = [];         // Todos los datos
  filteredItems: InventoryItemModel[] = []; // Datos mostrados en tabla
  
  searchControl = new FormControl('');      // Input del buscador

  ngOnInit() {
    this.loadData();
    this.setupSearch();
  }

  loadData() {
    this.getInventoryUseCase.execute().subscribe({
      next: (data) => {
        this.items = data;
        this.filterData(this.searchControl.value || '');
      },
      error: (err) => console.error('Error cargando inventario', err)
    });
  }

  private setupSearch() {
    this.searchControl.valueChanges
      .pipe(debounceTime(300)) // Esperar 300ms a que deje de escribir
      .subscribe(term => {
        this.filterData(term || '');
      });
  }

  private filterData(term: string) {
    if (!term) {
      this.filteredItems = this.items;
      return;
    }

    const lowerTerm = term.toLowerCase();
    
    // Filtramos por LPN, SKU o Ubicación
    this.filteredItems = this.items.filter(item => 
      item.lpn.toLowerCase().includes(lowerTerm) ||
      item.sku.toLowerCase().includes(lowerTerm) ||
      item.locationCode.toLowerCase().includes(lowerTerm)
    );
  }
}