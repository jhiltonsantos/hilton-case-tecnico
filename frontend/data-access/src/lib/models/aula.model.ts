export interface Aula {
  id: string;
  disciplinaId: string;
  professorId: string;
  horarioId: string;
  coordenadorId: string;
  cursosAutorizados: string[];
  vagasMaximas: number;
  vagasOcupadas: number;
  ativo: boolean;
}

export interface CriarAulaRequest {
  disciplinaId: string;
  professorId: string;
  horarioId: string;
  cursosAutorizados: string[];
  vagasMaximas: number;
}

export interface AtualizarAulaRequest {
  professorId: string;
  horarioId: string;
  cursosAutorizados: string[];
}
