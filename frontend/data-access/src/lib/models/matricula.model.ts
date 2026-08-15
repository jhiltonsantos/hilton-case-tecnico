export type StatusMatricula = 'ATIVA' | 'CANCELADA';

export interface Matricula {
  id: string;
  aulaMatrizId: string;
  disciplinaId: string;
  professorId: string;
  horarioId: string;
  status: StatusMatricula;
  criadoEm: string;
}

export interface MatricularRequest {
  aulaMatrizId: string;
}
