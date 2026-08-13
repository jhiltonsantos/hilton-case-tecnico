package br.com.jhiltonsantos.matricula.dto

import java.util.UUID

data class CriarAulaRequest(
    val disciplinaId: UUID,
    val professorId: UUID,
    val horarioId: UUID,
    val coordenadorId: UUID,
    val cursosAutorizados: List<UUID>,
    val vagasMaximas: Int,
)
