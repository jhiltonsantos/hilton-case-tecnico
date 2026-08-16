import { Route } from '@angular/router';
import { roleGuard } from '@frontend/auth';
import { Home } from './pages/home/home';
import { AulasCoordenador } from '@frontend/feature-coordenador';
import { MatriculaAluno } from '@frontend/feature-aluno';
import { AcessoNegado } from './pages/acesso-negado/acesso-negado';
import { Perfil } from './pages/perfil/perfil';

export const appRoutes: Route[] = [
  { path: '', component: Home },
  {
    path: 'perfil',
    component: Perfil,
    canActivate: [roleGuard],
    data: { roles: ['ALUNO', 'COORDENADOR'] },
  },
  {
    path: 'coordenador',
    component: AulasCoordenador,
    canActivate: [roleGuard],
    data: { roles: ['COORDENADOR'] },
  },
  {
    path: 'aluno',
    component: MatriculaAluno,
    canActivate: [roleGuard],
    data: { roles: ['ALUNO'] },
  },
  { path: 'acesso-negado', component: AcessoNegado },
];
