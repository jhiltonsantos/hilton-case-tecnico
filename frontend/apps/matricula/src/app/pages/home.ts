import { Component, inject } from '@angular/core';
import Keycloak from 'keycloak-js';

@Component({
  selector: 'app-home',
  template: `
    @if (keycloak.authenticated) {
      <p>Logado como {{ keycloak.tokenParsed?.['preferred_username'] }}</p>
      <button (click)="logout()">Sair</button>
    } @else {
      <button (click)="login()">Entrar</button>
    }
  `,
})
export class Home {
  protected keycloak = inject(Keycloak);

  login(): void {
    this.keycloak.login();
  }

  logout(): void {
    this.keycloak.logout({ redirectUri: window.location.origin });
  }
}
