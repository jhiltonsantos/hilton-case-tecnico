import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Aula, AtualizarAulaRequest, CriarAulaRequest } from '../models/aula.model';

const API_BASE_URL = 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class AulaService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Aula[]> {
    return this.http.get<Aula[]>(`${API_BASE_URL}/aulas`);
  }

  criar(request: CriarAulaRequest): Observable<Aula> {
    return this.http.post<Aula>(`${API_BASE_URL}/aulas`, request);
  }

  editar(id: string, request: AtualizarAulaRequest): Observable<Aula> {
    return this.http.put<Aula>(`${API_BASE_URL}/aulas/${id}`, request);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/aulas/${id}`);
  }
}
