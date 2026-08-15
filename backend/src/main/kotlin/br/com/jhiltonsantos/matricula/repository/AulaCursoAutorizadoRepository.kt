package br.com.jhiltonsantos.matricula.repository

import br.com.jhiltonsantos.matricula.domain.AulaCursoAutorizado
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class AulaCursoAutorizadoRepository : PanacheRepositoryBase<AulaCursoAutorizado, UUID> {
    fun aulaIdsPorCurso(cursoId: UUID): List<UUID> =
    find("cursoId", cursoId).list().mapNotNull { it.aulaMatrizId }
}
