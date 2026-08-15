import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import Keycloak from 'keycloak-js';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  protected keycloak = inject(Keycloak);

  hasRole(role: string): boolean {
    const token = this.keycloak.tokenParsed as { realm_access?: { roles?: string[] } } | undefined;
    return token?.realm_access?.roles?.includes(role) ?? false;
  }

  login(): void {
    this.keycloak.login();
  }

  logout(): void {
    this.keycloak.logout({ redirectUri: window.location.origin });
  }
}
