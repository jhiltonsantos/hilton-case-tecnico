import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Curso, Disciplina, Horario, Professor } from '../models/catalogo.model';

const API_BASE_URL = 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);

  disciplinas(): Observable<Disciplina[]> {
    return this.http.get<Disciplina[]>(`${API_BASE_URL}/disciplinas`);
  }

  professores(): Observable<Professor[]> {
    return this.http.get<Professor[]>(`${API_BASE_URL}/professores`);
  }

  horarios(): Observable<Horario[]> {
    return this.http.get<Horario[]>(`${API_BASE_URL}/horarios`);
  }

  cursos(): Observable<Curso[]> {
    return this.http.get<Curso[]>(`${API_BASE_URL}/cursos`);
  }
}
