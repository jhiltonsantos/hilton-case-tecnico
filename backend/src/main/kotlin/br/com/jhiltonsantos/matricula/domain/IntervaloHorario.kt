package br.com.jhiltonsantos.matricula.domain

import java.time.LocalTime

data class IntervaloHorario(
    val diaSemana: DiaSemana,
    val horarioInicio: LocalTime,
    val horarioFim: LocalTime,
) {
    init {
        require(horarioInicio < horarioFim) { "Horario inicial deve ser antes do horario final" }
    }

    fun conflitaCom(outro: IntervaloHorario): Boolean =
        diaSemana == outro.diaSemana &&
            horarioInicio < outro.horarioFim &&
            outro.horarioInicio < horarioFim
}
