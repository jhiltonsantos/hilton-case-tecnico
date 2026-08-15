package br.com.jhiltonsantos.matricula.domain.exception

import java.util.UUID

open class RegraNegocioException(mensagem: String) : RuntimeException(mensagem);

class AulaComMatriculadosException(id: UUID) :
    RegraNegocioException(mensagem = "Aula possui alunos matriculados, exclusao nao permitida: $id")

class CursoNaoAutorizadoException(aulaId: UUID) :
    RegraNegocioException(mensagem = "Curso do aluno nao autorizado para a aula: $aulaId")

class ChoqueDeHorarioException(aulaId: UUID) :
    RegraNegocioException(mensagem = "Aula conflita com horario de outra matricula ativa: $aulaId")

class VagaIndisponivelException(aulaId: UUID) :
    RegraNegocioException(mensagem = "Nao ha vaga disponivel na aula: $aulaId")

class DisciplinaJaOfertadaNoHorarioException(disciplinaId: UUID, horarioId: UUID) :
    RegraNegocioException(mensagem = "Disciplina ja ofertada nesse horario: disciplina=$disciplinaId horario=$horarioId")
