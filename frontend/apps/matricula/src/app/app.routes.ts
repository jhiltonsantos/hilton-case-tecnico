import { Route } from '@angular/router';
import { roleGuard } from '@frontend/auth';
import { Home } from './pages/home';
import { AreaCoordenador } from './pages/area-coordenador';
import { AreaAluno } from './pages/area-aluno';
import { AcessoNegado } from './pages/acesso-negado';

export const appRoutes: Route[] = [
  { path: '', component: Home },
  {
    path: 'coordenador',
    component: AreaCoordenador,
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
