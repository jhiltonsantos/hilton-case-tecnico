package br.com.jhiltonsantos.matricula.domain.exception

import java.util.UUID

open class EntidadeNaoEncontradaException(mensagem: String) : RuntimeException(mensagem)

class DisciplinaNaoEncontradaException(id: UUID) :
    EntidadeNaoEncontradaException(mensagem = "Disciplina nao encontrada: $id")

class ProfessorNaoEncontradoException(id: UUID) :
    EntidadeNaoEncontradaException(mensagem = "Professor nao encontrado: $id")

class HorarioNaoEncontradoException(id: UUID) :
    EntidadeNaoEncontradaException(mensagem = "Horario nao encontrado: $id")

class CursoNaoEncontradoException(id: UUID) :
    EntidadeNaoEncontradaException(mensagem = "Curso nao encontrado: $id")

class AulaNaoEncontradaException(id: UUID) :
    EntidadeNaoEncontradaException(mensagem = "Aula nao encontrada: $id")

class AlunoNaoEncontradoException(id: UUID) :
    EntidadeNaoEncontradaException(mensagem = "Aluno nao encontrado: $id")
