import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AlunoPerfil } from '../models/aluno.model';

const API_BASE_URL = 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class AlunoService {
  private readonly http = inject(HttpClient);

  perfil(): Observable<AlunoPerfil> {
    return this.http.get<AlunoPerfil>(`${API_BASE_URL}/aluno/perfil`);
  }
}
