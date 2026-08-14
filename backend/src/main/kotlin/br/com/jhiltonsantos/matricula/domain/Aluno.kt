package br.com.jhiltonsantos.matricula.domain

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "aluno")
class Aluno(
    var nome: String,
    var cursoId: UUID,
) : PanacheEntityBase {
    @Id
    var id: UUID? = null
}
