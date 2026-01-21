import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <footer class="w-full py-4 px-6 text-xs text-gray-500 transition-colors"
            [ngClass]="mode === 'card' ? 'bg-white/50 backdrop-blur-sm border-t border-gray-200' : 'bg-transparent text-gray-400'">
      
      <div class="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-4">
        
        <div class="flex items-center gap-1">
          <span>&copy; {{ currentYear }} <strong>WMS Enterprise</strong>.</span>
          <span class="hidden sm:inline">Todos los derechos reservados.</span>
        </div>

        <div class="flex items-center gap-6">
          <a href="#" class="hover:text-indigo-600 transition-colors">Ayuda</a>
          
          <div class="flex items-center gap-2 pl-4 border-l" [ngClass]="mode === 'card' ? 'border-gray-300' : 'border-gray-600'">
            <span class="relative flex h-2 w-2">
              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
              <span class="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
            </span>
            <span class="font-mono font-medium">v1.0.2</span>
          </div>
        </div>

      </div>
    </footer>
  `
})
export class FooterComponent {
  @Input() mode: 'card' | 'transparent' = 'card'; 
  currentYear = new Date().getFullYear();
}