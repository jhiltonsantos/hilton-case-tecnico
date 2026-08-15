package br.com.jhiltonsantos.matricula.domain

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(name = "aula_matriz")
class AulaMatriz(
    var disciplinaId: UUID,
    var professorId: UUID,
    var horarioId: UUID,
    var coordenadorId: UUID,
    var vagasMaximas: Int,
    var vagasOcupadas: Int = 0,
    var ativo: Boolean = true,
) : PanacheEntityBase {

    @Id
    @UuidGenerator
    var id: UUID? = null

    fun possuiVaga(): Boolean = vagasOcupadas < vagasMaximas

    fun podeSerExcluida(): Boolean = vagasOcupadas == 0

}

