package br.com.jhiltonsantos.matricula.resource

import br.com.jhiltonsantos.matricula.domain.Matricula
import br.com.jhiltonsantos.matricula.dto.MatricularRequest
import br.com.jhiltonsantos.matricula.dto.MatriculaResponse
import br.com.jhiltonsantos.matricula.repository.AulaMatrizRepository
import br.com.jhiltonsantos.matricula.service.MatriculaService
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import java.util.UUID

@Path("/matriculas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Matriculas", description = "Matricula do aluno em aulas da matriz curricular")
@SecurityRequirement(name = "bearerAuth")
@RolesAllowed("ALUNO")
class MatriculaResource(
    private val matriculaService: MatriculaService,
    private val aulaMatrizRepository: AulaMatrizRepository,
) {

    @Inject
    lateinit var jwt: JsonWebToken

    @Operation(
        summary = "Matricula o aluno logado em uma aula",
        description = "Valida curso, ausencia de problema de horario com outras matriculas ativas do aluno, e vaga disponivel.",
    )
    @APIResponses(
        APIResponse(responseCode = "201", description = "Matricula criada"),
        APIResponse(responseCode = "404", description = "Aula ou aluno inexistente"),
        APIResponse(responseCode = "422", description = "Curso nao autorizado, choque de horario ou vaga indisponivel"),
        APIResponse(responseCode = "401", description = "Requisicao sem token valido"),
        APIResponse(responseCode = "403", description = "Token sem role ALUNO"),
    )
    @POST
    fun matricular(request: MatricularRequest): Response {
        val matricula = matriculaService.matricular(UUID.fromString(jwt.subject), request)
        return Response.status(Response.Status.CREATED).entity(paraResponse(matricula)).build()
    }

    @Operation(
        summary = "Lista as matriculas ativas do aluno logado",
        description = "Retorna disciplina, professor e horario de cada aula em que o aluno esta matriculado.",
    )
    @APIResponse(responseCode = "200", description = "Lista de matriculas ativas")
    @GET
    fun minhasMatriculas(): List<MatriculaResponse> =
        matriculaService.minhasMatriculas(UUID.fromString(jwt.subject)).map { paraResponse(it) }

    private fun paraResponse(matricula: Matricula): MatriculaResponse {
        val aula = aulaMatrizRepository.findById(matricula.aulaMatrizId)!!
        return MatriculaResponse(
            id = matricula.id!!,
            aulaMatrizId = matricula.aulaMatrizId,
            disciplinaId = aula.disciplinaId,
            professorId = aula.professorId,
            horarioId = aula.horarioId,
            status = matricula.status,
            criadoEm = matricula.criadoEm,
        )
    }
}
