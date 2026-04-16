import { Component, AfterViewInit, inject, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PropertyInspectorComponent } from '../../components/property-inspector/property-inspector.component';
import { DesignerStore, InteractionMode } from '../../store/designer.store';
import { FabricCanvasService } from '../../../application/service/fabric-canvas.service';
import { SaveLayoutUseCase } from '../../../application/usecases/save-layout.usecase';
import { GetLayoutUseCase } from '../../../application/usecases/get-layout.usecase';
import { LayoutRepository } from '../../../domain/repositories/layout.repository';
import { LayoutHttpRepository } from '../../../infrastructure/repositories/layout-http.repository';
import { SearchLocationsUseCase } from '../../../application/usecases/search-locations.usecase';

@Component({
  selector: 'app-designer-page',
  standalone: true,
  imports: [CommonModule, PropertyInspectorComponent],
  providers: [
    FabricCanvasService, 
    DesignerStore,
    SearchLocationsUseCase,
    { provide: LayoutRepository, useClass: LayoutHttpRepository }
  ], 
  templateUrl: './designer-page.component.html',
  styles: [`
    .blueprint-grid {
      background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
      background-size: 20px 20px;
    }
  `]
})
export class DesignerPageComponent implements AfterViewInit {
  public fabricService = inject(FabricCanvasService);
  public store = inject(DesignerStore);
  
  private saveUC = inject(SaveLayoutUseCase);
  private getUC = inject(GetLayoutUseCase);

  ngAfterViewInit() {
    setTimeout(() => {
        this.initCanvasResponsive();
    }, 0);
  }

  private initCanvasResponsive() {
    const container = document.getElementById('canvas-container');
    if (container) {
        this.fabricService.init('mainCanvas', container.clientWidth, container.clientHeight);
        this.loadCurrentDesign();
    }
  }

  loadCurrentDesign() {
    this.getUC.execute().subscribe({
      next: (res: any) => {
        const json = res?.layoutJson;

        const hasObjects = json && json !== "{}" && !json.includes('"objects":[]');

        if (hasObjects) {
          this.fabricService.loadJSON(json);
        }
      },
      error: (err) => {
        console.error('Error al obtener el diseño:', err);
      }
    });
  }

  saveDesign() {
    try {
        const json = this.fabricService.toJSON();
        
        this.saveUC.execute(json).subscribe({
          next: () => {
            alert('Diseño guardado correctamente'); 
          },
          error: (err) => {
            console.error('Error en la petición de guardado:', err);
            alert('Ocurrió un error al intentar guardar el diseño.');
          }
        });
    } catch (e: any) {
        alert(e.message);
    }
  }

  setMode(mode: InteractionMode) {
      this.store.setInteractionMode(mode);
      
      const canvas = this.fabricService.canvas;
      if (canvas) {
          canvas.selection = (mode === 'SELECT');
          canvas.defaultCursor = (mode === 'SELECT') ? 'default' : 'crosshair';
          canvas.requestRenderAll();
      }
  }

  onDragStart(e: DragEvent, type: string) {
      e.dataTransfer?.setData('type', type);
      this.setMode('SELECT');
  }

  onDragOver(e: DragEvent) {
      e.preventDefault(); 
  }

  onDrop(e: DragEvent) {
      e.preventDefault();
      const type = e.dataTransfer?.getData('type') as any;
      
      if (type && (type === 'RACK' || type === 'ZONE')) {
          this.fabricService.dropElement(type, e.clientX, e.clientY);
      }
  }

  @HostListener('window:resize')
  onResize() {
      const container = document.getElementById('canvas-container');
      if (container) {
          this.fabricService.resize(container.clientWidth, container.clientHeight);
      }
  }
  
  @HostListener('window:keydown.delete')
  onDelete() { this.fabricService.deleteSelected(); }
}