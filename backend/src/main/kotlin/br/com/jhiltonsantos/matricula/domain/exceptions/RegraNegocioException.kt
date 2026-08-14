package br.com.jhiltonsantos.matricula.domain.exception

import java.util.UUID

open class RegraNegocioException(mensagem: String) : RuntimeException(mensagem);

class AulaComMatriculadosException(id: UUID) :
    RegraNegocioException(mensagem = "Aula possui alunos matriculados, exclusao nao permitida: $id")
