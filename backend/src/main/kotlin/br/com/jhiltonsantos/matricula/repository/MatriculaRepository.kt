package br.com.jhiltonsantos.matricula.repository

import br.com.jhiltonsantos.matricula.domain.Matricula
import br.com.jhiltonsantos.matricula.domain.StatusMatricula
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class MatriculaRepository : PanacheRepositoryBase<Matricula, UUID> {

    fun ativasDoAluno(alunoId: UUID): List<Matricula> =
        find("alunoId = ?1 and status = ?2", alunoId, StatusMatricula.ATIVA).list()
}
