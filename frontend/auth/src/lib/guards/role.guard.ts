import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { inject } from '@angular/core';
import { AuthGuardData, createAuthGuard } from 'keycloak-angular';
import Keycloak from 'keycloak-js';

const isAccessAllowed = async (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot,
  authData: AuthGuardData,
): Promise<boolean | UrlTree> => {
  const { authenticated, grantedRoles } = authData;
  const router = inject(Router);

  if (!authenticated) {
    const keycloak = inject(Keycloak);
    await keycloak.login({ redirectUri: window.location.origin + state.url });
    return false;
  }

  const rolesPermitidas = route.data['roles'] as string[] | undefined;
  if (!rolesPermitidas || rolesPermitidas.length === 0) {
    return true;
  }

  const temRolePermitida = rolesPermitidas.some((role) => grantedRoles.realmRoles.includes(role));
  return temRolePermitida ? true : router.parseUrl('/acesso-negado');
};

export const roleGuard = createAuthGuard<CanActivateFn>(isAccessAllowed);
