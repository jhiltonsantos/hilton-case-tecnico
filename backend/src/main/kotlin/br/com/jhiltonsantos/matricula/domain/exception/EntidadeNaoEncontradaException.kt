package br.com.jhiltonsantos.matricula.domain.exception

import java.util.UUID

open class EntidadeNaoEncontradaException(mensagem: String) : RuntimeException(mensagem)

class DisciplinaNaoEncontradaException(id: UUID) :
    EntidadeNaoEncontradaException("Disciplina nao encontrada: $id")

class ProfessorNaoEncontradoException(id: UUID) :
    EntidadeNaoEncontradaException("Professor nao encontrado: $id")

class HorarioNaoEncontradoException(id: UUID) :
    EntidadeNaoEncontradaException("Horario nao encontrado: $id")

class CursoNaoEncontradoException(id: UUID) :
    EntidadeNaoEncontradaException("Curso nao encontrado: $id")

class AulaNaoEncontradaException(id: UUID) :
    EntidadeNaoEncontradaException("Aula nao encontrada: $id")
