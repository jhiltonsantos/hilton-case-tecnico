package br.com.jhiltonsantos.matricula.repository

import br.com.jhiltonsantos.matricula.domain.AulaMatriz
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class AulaMatrizRepository : PanacheRepositoryBase<AulaMatriz, UUID> {

    fun findByIdAtivo(id: UUID): AulaMatriz? =
        find("id = ?1 and ativo = true", id).firstResult()

    fun buscar(horarioIds: List<UUID>?, aulaIds: List<UUID>?, vagasMaximas: Int?, coordenadorId: UUID?): List<AulaMatriz> {
        val condicoes = mutableListOf("ativo = true")
        val params = mutableMapOf<String, Any>()

        horarioIds?.let {
            condicoes += "horarioId in :horarioIds"
            params["horarioIds"] = it
        }
        aulaIds?.let {
            condicoes += "id in :aulaIds"
            params["aulaIds"] = it
        }
        vagasMaximas?.let {
            condicoes += "vagasMaximas = :vagasMaximas"
            params["vagasMaximas"] = it
        }
        coordenadorId?.let {
            condicoes += "coordenadorId = :coordenadorId"
            params["coordenadorId"] = it
        }

        return find(condicoes.joinToString(" and "), params).list()
    }
    
    fun ocuparVaga(aulaId: UUID): Boolean {
        val linhasAfetadas = update("vagasOcupadas = vagasOcupadas + 1 where id = ?1 and vagasOcupadas < vagasMaximas", aulaId)
        return linhasAfetadas > 0
    }

    fun existeAtivaComDisciplinaEHorario(disciplinaId: UUID, horarioId: UUID, excluirId: UUID? = null): Boolean =
        find(
            "disciplinaId = ?1 and horarioId = ?2 and ativo = true and (?3 is null or id != ?3)",
            disciplinaId, horarioId, excluirId,
        ).count() > 0

}
