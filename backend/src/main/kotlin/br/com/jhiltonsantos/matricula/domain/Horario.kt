package br.com.jhiltonsantos.matricula.domain

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.EnumType
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalTime
import java.util.UUID

enum class DiaSemana { SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO }

enum class PeriodoDia { MANHA, TARDE, NOITE }

@Entity
@Table(name = "horario")
class Horario(
    @Enumerated(EnumType.STRING)
    var diaSemana: DiaSemana,
    var horarioInicio: LocalTime,
    var horarioFim: LocalTime,
) : PanacheEntityBase {

    @Id
    var id: UUID? = null

    fun paraIntervalo() = IntervaloHorario(diaSemana, horarioInicio, horarioFim)

    fun periodo(): PeriodoDia = when {
        horarioInicio < LocalTime.of(12, 0) -> PeriodoDia.MANHA
        horarioInicio < LocalTime.of(18, 0) -> PeriodoDia.TARDE
        else -> PeriodoDia.NOITE
    }
}
