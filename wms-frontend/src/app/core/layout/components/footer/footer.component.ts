import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: 'footer.component.html'
})
export class FooterComponent {
  @Input() mode: 'card' | 'transparent' = 'card'; 
  currentYear = new Date().getFullYear();
}