import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { RetrieveAuditLogsUseCase } from '../../application/usecases/retrieve-audit-logs.usecase';
import { AuditLogModel, StockMovementType } from '../../domain/models/audit-log.model';
import { debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-audit-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './audit-list.component.html'
})
export class AuditListComponent implements OnInit {
  logs: AuditLogModel[] = [];
  totalElements = 0;
  currentPage = 0;
  pageSize = 20;
  
  filterForm!: FormGroup;

  constructor(
    private useCase: RetrieveAuditLogsUseCase,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadLogs();
    this.filterForm.valueChanges.pipe(
      debounceTime(600),
      distinctUntilChanged()
    ).subscribe(() => {
      this.currentPage = 0; 
      this.loadLogs();
    });
  }

  private initForm(): void {
    this.filterForm = this.fb.group({
      sku: [''],
      lpn: [''],
      startDate: [null],
      endDate: [null]
    });
  }

  loadLogs(): void {
    const vals = this.filterForm.getRawValue();
    
    this.useCase.execute({
      sku: vals.sku || undefined, 
      lpn: vals.lpn || undefined,
      startDate: vals.startDate ? new Date(vals.startDate).toISOString() : undefined,
      endDate: vals.endDate ? new Date(vals.endDate).toISOString() : undefined,
      page: this.currentPage,
      size: this.pageSize
    }).subscribe(result => {
      this.logs = result.items;
      this.totalElements = result.totalElements;
    });
  }

  nextPage(): void {
    if ((this.currentPage + 1) * this.pageSize < this.totalElements) {
      this.currentPage++;
      this.loadLogs();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadLogs();
    }
  }

  getTypeStyles(type: StockMovementType): string {
    switch (type) {
      case 'RECEPCION': return 'bg-green-100 text-green-700 border-green-200';
      case 'SALIDA': return 'bg-orange-100 text-orange-700 border-orange-200';
      case 'AJUSTE': return 'bg-red-50 text-red-700 border-red-100'; 
      case 'MOVIMIENTO': return 'bg-blue-50 text-blue-700 border-blue-100';
      default: return 'bg-gray-100 text-gray-700';
    }
  }
}