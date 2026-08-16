import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AlunoPerfil } from '../models/aluno.model';
import { API_BASE_URL } from '../config/api-base-url.token';

@Injectable({ providedIn: 'root' })
export class AlunoService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  perfil(): Observable<AlunoPerfil> {
    return this.http.get<AlunoPerfil>(`${this.apiBaseUrl}/aluno/perfil`);
  }
}
