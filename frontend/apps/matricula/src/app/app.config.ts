import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  provideKeycloak,
  createInterceptorCondition,
  IncludeBearerTokenCondition,
  includeBearerTokenInterceptor,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  withAutoRefreshToken,
  AutoRefreshTokenService,
  UserActivityService,
} from 'keycloak-angular';
import { appRoutes } from './app.routes';
import { httpErrorInterceptor } from './http-error.interceptor';

const bearerTokenCondition =
  createInterceptorCondition<IncludeBearerTokenCondition>({
    urlPattern: /^http:\/\/localhost:8080(\/.*)?$/i,
    bearerPrefix: 'Bearer',
  });

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideKeycloak({
      config: {
        url: 'http://localhost:8081',
        realm: 'matricula',
        clientId: 'matricula-app',
      },
      initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri:
          window.location.origin + '/silent-check-sso.html',
      },
      features: [
        withAutoRefreshToken({
          onInactivityTimeout: 'logout',
          sessionTimeout: 600_000,
        }),
      ],
      providers: [AutoRefreshTokenService, UserActivityService],
    }),
    {
      provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
      useValue: [bearerTokenCondition],
    },
    provideHttpClient(
      withInterceptors([includeBearerTokenInterceptor, httpErrorInterceptor]),
    ),
    provideRouter(appRoutes),
  ],
};
