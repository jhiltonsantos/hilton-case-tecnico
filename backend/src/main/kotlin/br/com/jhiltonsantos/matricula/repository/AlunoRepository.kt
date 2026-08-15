package br.com.jhiltonsantos.matricula.repository

import br.com.jhiltonsantos.matricula.domain.Aluno
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class AlunoRepository : PanacheRepositoryBase<Aluno, UUID>
