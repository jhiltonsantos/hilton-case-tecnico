import { Component, inject, input } from '@angular/core';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import Keycloak from 'keycloak-js';

interface TokenPerfil {
  name?: string;
  given_name?: string;
  family_name?: string;
  preferred_username?: string;
}

@Component({
  selector: 'lib-cabecalho-pagina',
  standalone: true,
  templateUrl: './cabecalho-pagina.html',
  styleUrl: './cabecalho-pagina.scss',
})
export class CabecalhoPagina {
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly keycloak = inject(Keycloak);

  eyebrow = input.required<string>();
  titulo = input.required<string>();
  subtitulo = input<string>();

  voltar(): void {
    this.location.back();
  }

  nomeCompleto(): string {
    const token = this.keycloak.tokenParsed as TokenPerfil | undefined;
    if (token?.name) return token.name;
    if (token?.given_name || token?.family_name) {
      return [token?.given_name, token?.family_name].filter(Boolean).join(' ');
    }
    return token?.preferred_username ?? '';
  }

  username(): string {
    return (this.keycloak.tokenParsed?.['preferred_username'] as string) ?? '';
  }

  irParaPerfil(): void {
    this.router.navigate(['/perfil']);
  }

  sair(): void {
    this.keycloak.logout({ redirectUri: window.location.origin });
  }
}
