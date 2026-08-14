package br.com.jhiltonsantos.matricula.resource

import br.com.jhiltonsantos.matricula.domain.PeriodoDia
import br.com.jhiltonsantos.matricula.dto.AtualizarAulaRequest
import br.com.jhiltonsantos.matricula.dto.AulaResponse
import br.com.jhiltonsantos.matricula.dto.CriarAulaRequest
import br.com.jhiltonsantos.matricula.service.AulaMatrizService
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import org.eclipse.microprofile.jwt.JsonWebToken
import java.time.LocalTime
import java.util.UUID

@Path("/aulas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AulaMatrizResource(private val aulaMatrizService: AulaMatrizService) {
    @Inject
    lateinit var jwt: JsonWebToken

    @POST
    @RolesAllowed("COORDENADOR")
    fun criar(request: CriarAulaRequest): Response {
        val aula = aulaMatrizService.criar(request, UUID.fromString(jwt.subject))
        return Response.status(Response.Status.CREATED).entity(paraResponse(aula.id!!)).build()
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("COORDENADOR")
    fun editar(@PathParam("id") id: UUID, request: AtualizarAulaRequest): AulaResponse {
        aulaMatrizService.editar(id, UUID.fromString(jwt.subject), request)
        return paraResponse(id)
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("COORDENADOR")
    fun excluir(@PathParam("id") id: UUID): Response {
        aulaMatrizService.excluir(id, UUID.fromString(jwt.subject))
        return Response.noContent().build()
    }

    @GET
    @RolesAllowed("COORDENADOR", "ALUNO")
    fun pesquisar(
        @QueryParam("periodoDia") periodoDia: PeriodoDia?,
        @QueryParam("horarioInicio") horarioInicio: LocalTime?,
        @QueryParam("horarioFim") horarioFim: LocalTime?,
        @QueryParam("cursoId") cursoId: UUID?,
        @QueryParam("vagasMaximas") vagasMaximas: Int?,
        @Context securityContext: SecurityContext,
    ): List<AulaResponse> {
        val coordenadorId = if (securityContext.isUserInRole("COORDENADOR")) UUID.fromString(jwt.subject) else null
        return aulaMatrizService.pesquisar(periodoDia, horarioInicio, horarioFim, cursoId, vagasMaximas, coordenadorId)
            .map { AulaResponse(
                id = it.id!!,
                disciplinaId = it.disciplinaId,
                professorId = it.professorId,
                horarioId = it.horarioId,
                coordenadorId = it.coordenadorId,
                cursosAutorizados = aulaMatrizService.cursosAutorizadosDe(it.id!!),
                vagasMaximas = it.vagasMaximas,
                vagasOcupadas = it.vagasOcupadas,
                ativo = it.ativo,
            ) }
    }

    private fun paraResponse(id: UUID): AulaResponse {
        val aula = aulaMatrizService.pesquisar(null, null, null, null, null, null).first { it.id == id }
        return AulaResponse(
            id = aula.id!!,
            disciplinaId = aula.disciplinaId,
            professorId = aula.professorId,
            horarioId = aula.horarioId,
            coordenadorId = aula.coordenadorId,
            cursosAutorizados = aulaMatrizService.cursosAutorizadosDe(id),
            vagasMaximas = aula.vagasMaximas,
            vagasOcupadas = aula.vagasOcupadas,
            ativo = aula.ativo,
        )
    }
}
