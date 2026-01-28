import { Component, AfterViewInit, inject, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { catchError, forkJoin, of } from 'rxjs';
import { DesignerStore } from '../../store/designer.store';
import { GetLayoutUseCase } from '../../../application/usecases/get-layout.usecase';
import { GetRackSummaryUseCase } from '../../../application/usecases/get-rack-summary.usecase';
import { LayoutRepository } from '../../../domain/repositories/layout.repository';
import { LayoutHttpRepository } from '../../../infrastructure/repositories/layout-http.repository'; 
import { FabricCanvasService } from '../../../application/service/fabric-canvas.service';

@Component({
  selector: 'app-monitor-page',
  standalone: true,
  imports: [CommonModule],
  providers: [
    FabricCanvasService,
    DesignerStore, // ✅ Esto soluciona el error NG0201
    { provide: LayoutRepository, useClass: LayoutHttpRepository }
  ],
  templateUrl: './monitor-page.component.html'
})
export class MonitorPageComponent implements AfterViewInit {
  private fabricService = inject(FabricCanvasService);
  private getLayoutUC = inject(GetLayoutUseCase);
  private getRackSummaryUC = inject(GetRackSummaryUseCase);
  
  isLoading = true;
  lastUpdate: Date | null = null;

  ngAfterViewInit() {
    // Le damos un pequeño respiro al DOM para que calcule el tamaño del div contenedor
    setTimeout(() => {
        this.initCanvasResponsive();
    }, 100);
  }

  initCanvasResponsive() {
    const container = document.getElementById('monitor-container');
    
    if (container) {
        const width = container.clientWidth;
        const height = container.clientHeight;
        
        this.fabricService.init('monitorCanvas', width, height);
        this.loadAndVisualize();
    } else {
        console.error('No se encontró el contenedor del monitor');
    }
  }

  loadAndVisualize() {
    this.isLoading = true;
    
    this.getLayoutUC.execute().subscribe({
      next: (res) => {
        if (res.layoutJson) {
          this.fabricService.loadJSON(res.layoutJson);
          this.lastUpdate = res.lastUpdate;
          
          this.fabricService.setReadOnly(true);
          
          this.refreshHeatmap();
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error cargando layout:', err);
        this.isLoading = false;
      }
    });
  }

  refreshHeatmap() {
    const codes = this.fabricService.getAllRackCodes();
    
    if (codes.length === 0) return;

    // Tipamos explícitamente 'code' como string
    const requests = codes.map((code: string) => 
       this.getRackSummaryUC.execute(code).pipe(
          catchError(() => of({ rackCode: code, status: 'UNBOUND' } as any)) 
       )
    );

    // Tipamos la respuesta del forkJoin
    forkJoin(requests).subscribe((summaries: any[]) => {
        summaries.forEach(summary => {
            this.fabricService.updateObjectStatus(summary.rackCode, summary.status);
        });
    });
  }

  // Redimensionar si el usuario cambia el tamaño de la ventana
  @HostListener('window:resize')
  onResize() {
      const container = document.getElementById('monitor-container');
      if (container) {
          this.fabricService.resize(container.clientWidth, container.clientHeight);
      }
  }
}