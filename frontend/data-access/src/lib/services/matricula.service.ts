import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Matricula, MatricularRequest } from '../models/matricula.model';

const API_BASE_URL = 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class MatriculaService {
  private readonly http = inject(HttpClient);

  minhasMatriculas(): Observable<Matricula[]> {
    return this.http.get<Matricula[]>(`${API_BASE_URL}/matriculas`);
  }

  matricular(request: MatricularRequest): Observable<Matricula> {
    return this.http.post<Matricula>(`${API_BASE_URL}/matriculas`, request);
  }
}
