package br.com.jhiltonsantos.matricula.repository

import br.com.jhiltonsantos.matricula.domain.Coordenador
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class CoordenadorRepository : PanacheRepositoryBase<Coordenador, UUID>
