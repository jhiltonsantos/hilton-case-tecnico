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

const DIA_SEMANA_ORDEM: Record<DiaSemana, number> = {
  SEGUNDA: 0,
  TERCA: 1,
  QUARTA: 2,
  QUINTA: 3,
  SEXTA: 4,
  SABADO: 5,
};

export function compararHorarios(a: Horario, b: Horario): number {
  const diaDiff = DIA_SEMANA_ORDEM[a.diaSemana] - DIA_SEMANA_ORDEM[b.diaSemana];
  return diaDiff !== 0 ? diaDiff : a.horarioInicio.localeCompare(b.horarioInicio);
}

export function ordenarPorHorario<T extends { horarioId: string }>(itens: T[], horarios: Horario[]): T[] {
  const horarioPorId = new Map(horarios.map((h) => [h.id, h]));
  return [...itens].sort((a, b) => {
    const horarioA = horarioPorId.get(a.horarioId);
    const horarioB = horarioPorId.get(b.horarioId);
    if (!horarioA || !horarioB) return 0;
    return compararHorarios(horarioA, horarioB);
  });
}
