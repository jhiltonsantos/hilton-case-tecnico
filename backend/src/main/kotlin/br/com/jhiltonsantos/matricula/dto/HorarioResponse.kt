package br.com.jhiltonsantos.matricula.dto

import br.com.jhiltonsantos.matricula.domain.DiaSemana
import java.time.LocalTime
import java.util.UUID

data class HorarioResponse(
    val id: UUID,
    val diaSemana: DiaSemana,
    val horarioInicio: LocalTime,
    val horarioFim: LocalTime
)
