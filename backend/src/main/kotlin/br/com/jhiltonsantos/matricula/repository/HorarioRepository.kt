package br.com.jhiltonsantos.matricula.repository

import br.com.jhiltonsantos.matricula.domain.Horario
import br.com.jhiltonsantos.matricula.domain.PeriodoDia
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalTime
import java.util.UUID

@ApplicationScoped
class HorarioRepository : PanacheRepositoryBase<Horario, UUID> {
    fun idsPorPeriodo(periodo: PeriodoDia): List<UUID> =
        listAll().filter { it.periodo() == periodo }.mapNotNull { it.id }


            fun idsPorIntervalo(inicio: LocalTime, fim: LocalTime): List<UUID> =
        find("horarioInicio >= ?1 and horarioFim <= ?2", inicio, fim)
            .list()
            .mapNotNull { it.id }
}
