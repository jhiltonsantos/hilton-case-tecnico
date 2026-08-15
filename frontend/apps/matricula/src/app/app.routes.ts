import { Route } from '@angular/router';
import { roleGuard } from '@frontend/auth';
import { Home } from './pages/home/home';
import { AulasCoordenador } from '@frontend/feature-coordenador';
import { AreaAluno } from './pages/area-aluno/area-aluno';
import { AcessoNegado } from './pages/acesso-negado/acesso-negado';

export const appRoutes: Route[] = [
  { path: '', component: Home },
  {
    path: 'coordenador',
    component: AulasCoordenador,
    canActivate: [roleGuard],
    data: { roles: ['COORDENADOR'] },
  },
  {
    path: 'aluno',
    component: AreaAluno,
    canActivate: [roleGuard],
    data: { roles: ['ALUNO'] },
  },
  { path: 'acesso-negado', component: AcessoNegado },
];
