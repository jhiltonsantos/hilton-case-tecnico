package br.com.jhiltonsantos.matricula.dto

import br.com.jhiltonsantos.matricula.domain.StatusMatricula
import java.time.OffsetDateTime
import java.util.UUID

data class MatriculaResponse(
    val id: UUID,
    val aulaMatrizId: UUID,
    val disciplinaId: UUID,
    val professorId: UUID,
    val horarioId: UUID,
    val status: StatusMatricula,
    val criadoEm: OffsetDateTime,
)
