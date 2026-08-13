package br.com.jhiltonsantos.matricula.domain

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.EnumType
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

enum class StatusMatricula { ATIVA, CANCELADA }

@Entity
@Table(name = "matricula")
class Matricula(
    var alunoId: UUID,
    var aulaMatrizId: UUID,
    @Enumerated(EnumType.STRING)
    var status: StatusMatricula = StatusMatricula.ATIVA,
    var criadoEm: OffsetDateTime = OffsetDateTime.now(),
) : PanacheEntityBase {

    @Id
    @UuidGenerator
    var id: UUID? = null

    fun cancelar() {
        status = StatusMatricula.CANCELADA
    }
}
