import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, switchMap, takeUntil, of, catchError } from 'rxjs'; 
import { DesignerStore } from '../../store/designer.store';
import { SearchLocationsUseCase } from '../../../application/usecases/search-locations.usecase';
import { GetRackSummaryUseCase } from '../../../application/usecases/get-rack-summary.usecase';
import { RackSummary } from '../../../domain/models/rack-summary.model';
import { FabricCanvasService } from '../../../application/service/fabric-canvas.service';

@Component({
  selector: 'app-property-inspector',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './property-inspector.component.html'
})
export class PropertyInspectorComponent implements OnInit, OnDestroy {
  public store = inject(DesignerStore);
  public fabricService = inject(FabricCanvasService);
  private searchLocationsUC = inject(SearchLocationsUseCase);
  private getRackSummary = inject(GetRackSummaryUseCase);
  
  public rackSummary: RackSummary | null = null;
  public searchResults: string[] = [];
  public showDropdown = false;
  public errorMessage: string | null = null;
  private skipBlurValidation = false;
  
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  ngOnInit() {
    this.searchSubject.pipe(
      debounceTime(300), 
      distinctUntilChanged(), 
      switchMap((term) => {
        const type = this.store.activeData()?.type;
        if (!term || term.length < 2 || (type !== 'RACK' && type !== 'ZONE')) {
            return of([]);
        }
        return this.searchLocationsUC.execute(term, type as any).pipe(
            catchError(() => of([]))
        );
      }),
      takeUntil(this.destroy$)
    ).subscribe(results => {
      this.searchResults = results;
      this.showDropdown = results.length > 0;
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearchInput(event: any) {
    this.errorMessage = null; 

    const value = event.target.value;
    this.updateLocalCode(value);
    this.searchSubject.next(value);
  }

  selectLocation(code: string) {
    this.skipBlurValidation = true;

    const activeObj = this.fabricService.getActiveObject();
    const isTaken = this.fabricService.isCodeInUse(code, activeObj);

    if (isTaken) {
        this.errorMessage = `El código ${code} ya está en uso.`;
        return; 
    }

    this.errorMessage = null;
    this.updateLocalCode(code);
    this.showDropdown = false;
    this.checkBinding();
  }
  private updateLocalCode(newCode: string) {
    const current = this.store.activeData();
    if (current) {
        const updated = { ...current, code: newCode.toUpperCase() };
        this.store.updateActiveData(updated);
        this.fabricService.updateSelectedData(updated);
    }
  }

  onBlur() {
    setTimeout(() => {
        if (this.skipBlurValidation) {
            this.skipBlurValidation = false; 
            return; 
        }

        const currentData = this.store.activeData();
        
        if (currentData && currentData.code && currentData.code.length > 0) {
             const activeObj = this.fabricService.getActiveObject();
             const isTaken = this.fabricService.isCodeInUse(currentData.code, activeObj);

            if (isTaken) {
                this.errorMessage = `¡Cuidado! ${currentData.code} está duplicado.`;
            } else {
                this.errorMessage = null;
            }
        }
        
        this.showDropdown = false;
        this.checkBinding();
    }, 200);
  }

  onFocus() {
      this.errorMessage = null;
  }

  checkBinding() {
    const data = this.store.activeData();
    if (data && data.code && data.code.length >= 3) {
        this.getRackSummary.execute(data.code).subscribe({
            next: (summary) => {
                this.rackSummary = summary;
                const updated = { ...data, status: 'BOUND' as const };
                this.store.updateActiveData(updated);
                this.fabricService.updateSelectedData(updated);
            },
            error: () => {
                this.rackSummary = null;
                const updated = { ...data, status: 'UNBOUND' as const };
                this.store.updateActiveData(updated);
                this.fabricService.updateSelectedData(updated);
            }
        });
    }
  }
}