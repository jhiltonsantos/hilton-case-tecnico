import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import Keycloak from 'keycloak-js';
import { AlunoService, CoordenadorService } from '@frontend/data-access';
import { CabecalhoPagina } from '@frontend/ui';

interface PerfilExibido {
  id: string;
  usuario: string;
  nome: string;
  curso?: string;
}

interface LinhaPerfil {
  campo: string;
  valor: string;
}

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, TableModule, CabecalhoPagina],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss',
})
export class Perfil implements OnInit {
  private readonly keycloak = inject(Keycloak);
  private readonly alunoService = inject(AlunoService);
  private readonly coordenadorService = inject(CoordenadorService);

  perfil = signal<PerfilExibido | null>(null);

  linhas = computed<LinhaPerfil[]>(() => {
    const p = this.perfil();
    if (!p) return [];
    const linhas: LinhaPerfil[] = [
      { campo: 'ID', valor: p.id },
      { campo: 'Usuário', valor: p.usuario },
      { campo: 'Nome', valor: p.nome },
    ];
    if (p.curso) linhas.push({ campo: 'Curso', valor: p.curso });
    return linhas;
  });

  private hasRole(role: string): boolean {
    const token = this.keycloak.tokenParsed as { realm_access?: { roles?: string[] } } | undefined;
    return token?.realm_access?.roles?.includes(role) ?? false;
  }

  ngOnInit(): void {
    const usuario = (this.keycloak.tokenParsed?.['preferred_username'] as string) ?? '';

    if (this.hasRole('COORDENADOR')) {
      this.coordenadorService.perfil().subscribe((p) =>
        this.perfil.set({ id: p.id, usuario, nome: p.nome }),
      );
    } else {
      this.alunoService.perfil().subscribe((p) =>
        this.perfil.set({ id: p.id, usuario, nome: p.nome, curso: p.cursoNome }),
      );
    }
  }
}
