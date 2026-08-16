import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CoordenadorPerfil } from '../models/coordenador.model';
import { API_BASE_URL } from '../config/api-base-url.token';

@Injectable({ providedIn: 'root' })
export class CoordenadorService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  perfil(): Observable<CoordenadorPerfil> {
    return this.http.get<CoordenadorPerfil>(
      `${this.apiBaseUrl}/coordenador/perfil`,
    );
  }
}
