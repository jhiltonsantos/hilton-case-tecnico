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
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement
import org.eclipse.microprofile.openapi.annotations.tags.Tag

@Path("/aulas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Aulas", description = "Gestao de aulas da matriz curricular (CRUD do coordenador)")
@SecurityRequirement(name = "bearerAuth")
class AulaMatrizResource(private val aulaMatrizService: AulaMatrizService) {
    @Inject
    lateinit var jwt: JsonWebToken

    @Operation(
        summary = "Cria uma aula da matriz curricular",
        description = "Valida a existencia de disciplina, professor, horario e cursos autorizados. O coordenador dono e extraido do token JWT, nao vai no corpo da requisicao.",
    )
    @APIResponses(
        APIResponse(responseCode = "201", description = "Aula criada"),
        APIResponse(responseCode = "404", description = "Disciplina, professor, horario ou curso inexistente"),
        APIResponse(responseCode = "401", description = "Requisicao sem token valido"),
        APIResponse(responseCode = "403", description = "Token sem role COORDENADOR"),
    )
    @POST
    @RolesAllowed("COORDENADOR")
    fun criar(request: CriarAulaRequest): Response {
        val aula = aulaMatrizService.criar(request, UUID.fromString(jwt.subject))
        return Response.status(Response.Status.CREATED).entity(paraResponse(aula.id!!)).build()
    }

    @Operation(
        summary = "Edita professor, horario e cursos autorizados de uma aula",
        description = "Disciplina e vagas maximas nao sao editaveis. So o coordenador dono da aula pode editar.",
    )
    @APIResponses(
        APIResponse(responseCode = "200", description = "Aula editada"),
        APIResponse(responseCode = "404", description = "Aula, professor, horario ou curso inexistente"),
        APIResponse(
            responseCode = "403",
            description = "Aula nao pertence a este coordenador, ou token sem role COORDENADOR"
        ),
    )
    @PUT
    @Path("/{id}")
    @RolesAllowed("COORDENADOR")
    fun editar(@PathParam("id") id: UUID, request: AtualizarAulaRequest): AulaResponse {
        aulaMatrizService.editar(id, UUID.fromString(jwt.subject), request)
        return paraResponse(id)
    }

    @Operation(
        summary = "Exclui logicamente uma aula",
        description = "Bloqueada se houver algum aluno matriculado ativo. So o coordenador pode excluir.",
    )
    @APIResponses(
        APIResponse(responseCode = "204", description = "Aula excluida (soft delete)"),
        APIResponse(responseCode = "404", description = "Aula inexistente"),
        APIResponse(responseCode = "403", description = "Aula nao pertence a este coordenador"),
        APIResponse(responseCode = "422", description = "Aula possui alunos matriculados"),
    )
    @DELETE
    @Path("/{id}")
    @RolesAllowed("COORDENADOR")
    fun excluir(@PathParam("id") id: UUID): Response {
        aulaMatrizService.excluir(id, UUID.fromString(jwt.subject))
        return Response.noContent().build()
    }

    @Operation(
        summary = "Lista/pesquisa aulas ativas",
        description = "Coordenador ve so as proprias aulas; aluno ve todas as aulas ativas, sem filtro de dono. Filtros opcionais combinaveis.",
    )
    @APIResponse(responseCode = "200", description = "Lista de aulas")
    @GET
    @RolesAllowed("COORDENADOR", "ALUNO")
    fun pesquisar(
        @Parameter(description = "Filtra por periodo do dia", required = false)
        @QueryParam("periodoDia") periodoDia: PeriodoDia?,
        
        @Parameter(description = "Inicio do intervalo de horario (usar junto com horarioFim) ", required = false)
        @QueryParam("horarioInicio") horarioInicio: LocalTime?,
        
        @Parameter(description = "Fim do intervalo de horario (usar junto com horarioInicio) ", required = false)
        @QueryParam("horarioFim") horarioFim: LocalTime?,
        
        @Parameter(description = "Filtra por curso autorizado", required = false)
        @QueryParam("cursoId") cursoId: UUID?,
        
        @Parameter(description = "Filtra por quantidade de vagas maximas", required = false)
        @QueryParam("vagasMaximas") vagasMaximas: Int?,
        
        @Context securityContext: SecurityContext,
    ): List<AulaResponse>
    {
        val coordenadorId = if (securityContext.isUserInRole("COORDENADOR")) UUID.fromString(jwt.subject) else null
        return aulaMatrizService.pesquisar(periodoDia, horarioInicio, horarioFim, cursoId, vagasMaximas, coordenadorId)
            .map {
                AulaResponse(
                    id = it.id!!,
                    disciplinaId = it.disciplinaId,
                    professorId = it.professorId,
                    horarioId = it.horarioId,
                    coordenadorId = it.coordenadorId,
                    cursosAutorizados = aulaMatrizService.cursosAutorizadosDe(it.id!!),
                    vagasMaximas = it.vagasMaximas,
                    vagasOcupadas = it.vagasOcupadas,
                    ativo = it.ativo,
                )
            }
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
