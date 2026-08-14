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
