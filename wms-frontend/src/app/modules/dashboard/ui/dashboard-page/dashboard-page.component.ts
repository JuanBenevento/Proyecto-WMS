/**
 * Dashboard Page Component
 */

import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GetDashboardKpisUseCase } from '../../application/usecases/get-dashboard-kpis.usecase';
import { DashboardKpis } from '../../domain/models/kpi.model';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-page.component.html'
})
export class DashboardPageComponent implements OnInit {
  private readonly getKpisUseCase = inject(GetDashboardKpisUseCase);

  kpis: DashboardKpis | null = null;
  loading = true;
  error = false;

  // Computed values
  utilizationPercent = 0;
  completionPercent = 0;
  inProgressPercent = 0;

  ngOnInit(): void {
    this.loadKpis();
  }

  loadKpis(): void {
    this.loading = true;
    this.error = false;

    this.getKpisUseCase.execute().subscribe({
      next: (kpis) => {
        this.kpis = kpis;
        this.utilizationPercent = this.getKpisUseCase.getUtilization(kpis);
        this.completionPercent = this.getKpisUseCase.getCompletionRate(kpis);
        
        // Calculate in-progress percentage
        if (kpis.orders.total > 0) {
          this.inProgressPercent = Math.round((kpis.orders.inProgress / kpis.orders.total) * 100);
        }
        
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  refresh(): void {
    this.loadKpis();
  }
}