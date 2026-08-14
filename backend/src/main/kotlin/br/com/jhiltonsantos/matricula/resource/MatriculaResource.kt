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
@RolesAllowed("ALUNO")
class MatriculaResource(
    private val matriculaService: MatriculaService,
    private val aulaMatrizRepository: AulaMatrizRepository,
) {

    @Inject
    lateinit var jwt: JsonWebToken

    @POST
    fun matricular(request: MatricularRequest): Response {
        val matricula = matriculaService.matricular(UUID.fromString(jwt.subject), request)
        return Response.status(Response.Status.CREATED).entity(paraResponse(matricula)).build()
    }

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
