package br.com.jhiltonsantos.matricula.repository

import br.com.jhiltonsantos.matricula.domain.Professor
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class ProfessorRepository : PanacheRepositoryBase<Professor, UUID>
