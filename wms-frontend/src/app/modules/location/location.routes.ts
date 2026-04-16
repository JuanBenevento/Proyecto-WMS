import { Routes } from '@angular/router';
import { LocationRepository } from './domain/ports/repository/location.repository';
import { LocationRepositoryAdapter } from './infrastructure/adapters/location-repository.adapter';
import { ManageLocationUseCase } from './application/usecases/manage-location.usecase';
import { LocationListComponent } from './ui/location-list/location-list.component';
import { LocationFormComponent } from './ui/location-form/location-form.component';

export const LOCATION_ROUTES: Routes = [
  {
    path: '',
    providers: [
      ManageLocationUseCase,
      { provide: LocationRepository, useClass: LocationRepositoryAdapter }
    ],
    children: [
      { path: '', component: LocationListComponent },
      { path: 'create', component: LocationFormComponent },
      { path: 'edit/:code', component: LocationFormComponent }
    ]
  }
];