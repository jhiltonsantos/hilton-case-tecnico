export interface Disciplina {
  id: string;
  nome: string;
}
export interface Professor {
  id: string;
  nome: string;
}
export interface Curso {
  id: string;
  nome: string;
}

export type DiaSemana =
  | 'SEGUNDA'
  | 'TERCA'
  | 'QUARTA'
  | 'QUINTA'
  | 'SEXTA'
  | 'SABADO';

export interface Horario {
  id: string;
  diaSemana: DiaSemana;
  horarioInicio: string;
  horarioFim: string;
}

export type PeriodoDia = 'MANHA' | 'TARDE' | 'NOITE';

export const PERIODO_DIA_LABEL: Record<PeriodoDia, string> = {
  MANHA: 'Manhã',
  TARDE: 'Tarde',
  NOITE: 'Noite',
};

export const DIA_SEMANA_LABEL: Record<DiaSemana, string> = {
  SEGUNDA: 'Segunda',
  TERCA: 'Terça',
  QUARTA: 'Quarta',
  QUINTA: 'Quinta',
  SEXTA: 'Sexta',
  SABADO: 'Sábado',
};

function semSegundos(hora: string): string {
  return hora.slice(0, 5);
}

export function formatarHorario(horario: Horario): string {
  return `${DIA_SEMANA_LABEL[horario.diaSemana]} ${semSegundos(horario.horarioInicio)}–${semSegundos(horario.horarioFim)}`;
}
