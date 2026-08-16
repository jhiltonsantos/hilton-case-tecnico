import { Component, inject, input } from '@angular/core';
import { Router } from '@angular/router';
import Keycloak from 'keycloak-js';

@Component({
  selector: 'lib-cabecalho-pagina',
  standalone: true,
  templateUrl: './cabecalho-pagina.html',
  styleUrl: './cabecalho-pagina.scss',
})
export class CabecalhoPagina {
  private readonly router = inject(Router);
  private readonly keycloak = inject(Keycloak);

  eyebrow = input.required<string>();
  titulo = input.required<string>();
  subtitulo = input<string>();

  voltar(): void {
    this.router.navigate(['/']);
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
