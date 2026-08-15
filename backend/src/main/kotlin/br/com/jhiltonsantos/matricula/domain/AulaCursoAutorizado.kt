package br.com.jhiltonsantos.matricula.domain

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(name = "aula_curso_autorizado")
class AulaCursoAutorizado(
    var aulaMatrizId: UUID,
    var cursoId: UUID,
) : PanacheEntityBase {

    @Id
    @UuidGenerator
    var id: UUID? = null
}
