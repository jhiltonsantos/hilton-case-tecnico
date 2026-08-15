package br.com.jhiltonsantos.matricula.dto

import java.util.UUID

data class AtualizarAulaRequest(
    val professorId: UUID,
    val horarioId: UUID,
    val cursosAutorizados: List<UUID>,
)
