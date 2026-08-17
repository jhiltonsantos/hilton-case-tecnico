import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Matricula, MatricularRequest } from '../models/matricula.model';
import { API_BASE_URL } from '../config/api-base-url.token';

@Injectable({ providedIn: 'root' })
export class MatriculaService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  minhasMatriculas(): Observable<Matricula[]> {
    return this.http.get<Matricula[]>(`${this.apiBaseUrl}/matriculas`);
  }

  matricular(request: MatricularRequest): Observable<Matricula> {
    return this.http.post<Matricula>(`${this.apiBaseUrl}/matriculas`, request);
  }
}
