package br.com.jhiltonsantos.matricula.dto

import java.util.UUID

data class AulaResponse(
    val id: UUID,
    val disciplinaId: UUID,
    val professorId: UUID,
    val horarioId: UUID,
    val coordenadorId: UUID,
    val cursosAutorizados: List<UUID>,
    val vagasMaximas: Int,
    val vagasOcupadas: Int,
    val ativo: Boolean,
)
