import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Curso, Disciplina, Horario, Professor } from '../models/catalogo.model';
import { API_BASE_URL } from '../config/api-base-url.token';

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  disciplinas(): Observable<Disciplina[]> {
    return this.http.get<Disciplina[]>(`${this.apiBaseUrl}/disciplinas`);
  }

  professores(): Observable<Professor[]> {
    return this.http.get<Professor[]>(`${this.apiBaseUrl}/professores`);
  }

  horarios(): Observable<Horario[]> {
    return this.http.get<Horario[]>(`${this.apiBaseUrl}/horarios`);
  }

  cursos(): Observable<Curso[]> {
    return this.http.get<Curso[]>(`${this.apiBaseUrl}/cursos`);
  }
}
