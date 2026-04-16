import { Observable } from 'rxjs';
import { RackSummary } from '../models/rack-summary.model';

export abstract class LayoutRepository {
  abstract getLayout(): Observable<{ layoutJson: string; lastUpdate: Date }>;
  abstract saveLayout(json: string): Observable<void>;
  abstract getRackSummary(rackCode: string): Observable<RackSummary>;
  abstract searchLocations(query: string, type: 'RACK' | 'ZONE'): Observable<string[]>;
}