import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { providePrimeNG } from 'primeng/config';
import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';
import { API_BASE_URL } from '@frontend/data-access';

const MatriculaPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '{amber.50}',
      100: '{amber.100}',
      200: '{amber.200}',
      300: '{amber.300}',
      400: '{amber.400}',
      500: '{amber.500}',
      600: '{amber.600}',
      700: '{amber.700}',
      800: '{amber.800}',
      900: '{amber.900}',
      950: '{amber.950}',
    },
  },
});
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
    provideAnimationsAsync(),
    providePrimeNG({
      theme: { preset: MatriculaPreset },
      translation: {
        emptyMessage: 'Nenhum resultado encontrado',
        emptyFilterMessage: 'Nenhum resultado encontrado',
        selectionMessage: '{0} itens selecionados',
        emptySelectionMessage: 'Nenhum item selecionado',
        clear: 'Limpar',
        apply: 'Aplicar',
        accept: 'Sim',
        reject: 'Não',
        aria: {
          selectAll: 'Selecionar todos',
          unselectAll: 'Limpar seleção',
          close: 'Fechar',
        },
      },
    }),
    { provide: API_BASE_URL, useValue: 'http://localhost:8080' },
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
