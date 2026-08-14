import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import Keycloak from 'keycloak-js';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloak = inject(Keycloak);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401) {
        keycloak.login({ redirectUri: window.location.origin + router.url });
      } else if (error.status === 403) {
        router.navigateByUrl('/acesso-negado');
      }
      return throwError(() => error);
    }),
  );
};
