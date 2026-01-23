// core/layout/components/header/header.component.ts
import { Component, Input, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: 'header.component.html'
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Input() softwareName: string = 'WMS Enterprise';
  @Input() subtitle: string = '';
  
  public auth = inject(AuthService);
  currentDate = new Date();
  private timer: any;

  ngOnInit() {
    // Actualiza la hora cada segundo
    this.timer = setInterval(() => {
      this.currentDate = new Date();
    }, 1000);
  }

  ngOnDestroy() {
    if (this.timer) clearInterval(this.timer);
  }
}