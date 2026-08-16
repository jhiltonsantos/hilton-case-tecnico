import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Aula, AtualizarAulaRequest, CriarAulaRequest } from '../models/aula.model';
import { PeriodoDia } from '../models/catalogo.model';
import { API_BASE_URL } from '../config/api-base-url.token';

export interface FiltrosAula {
  periodoDia?: PeriodoDia;
  horarioInicio?: string;
  horarioFim?: string;
  cursoId?: string;
  vagasMaximas?: number;
}

@Injectable({ providedIn: 'root' })
export class AulaService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  listar(filtros?: FiltrosAula): Observable<Aula[]> {
    const params: Record<string, string> = {};
    if (filtros?.periodoDia) {
      params['periodoDia'] = filtros.periodoDia;
    } else if (filtros?.horarioInicio && filtros?.horarioFim) {
      params['horarioInicio'] = filtros.horarioInicio;
      params['horarioFim'] = filtros.horarioFim;
    }
    if (filtros?.cursoId) params['cursoId'] = filtros.cursoId;
    if (filtros?.vagasMaximas != null) params['vagasMaximas'] = String(filtros.vagasMaximas);
    return this.http.get<Aula[]>(`${this.apiBaseUrl}/aulas`, { params });
  }

  criar(request: CriarAulaRequest): Observable<Aula> {
    return this.http.post<Aula>(`${this.apiBaseUrl}/aulas`, request);
  }

  editar(id: string, request: AtualizarAulaRequest): Observable<Aula> {
    return this.http.put<Aula>(`${this.apiBaseUrl}/aulas/${id}`, request);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/aulas/${id}`);
  }
}
