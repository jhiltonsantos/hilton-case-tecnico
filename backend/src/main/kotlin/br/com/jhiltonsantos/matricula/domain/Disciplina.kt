package br.com.jhiltonsantos.matricula.domain

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "disciplina")
class Disciplina(
    var nome: String,
) : PanacheEntityBase {
    @Id
    var id: UUID? = null
}
