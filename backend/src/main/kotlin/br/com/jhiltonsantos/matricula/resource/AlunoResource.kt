package br.com.jhiltonsantos.matricula.resource

import br.com.jhiltonsantos.matricula.dto.AlunoPerfilResponse
import br.com.jhiltonsantos.matricula.repository.AlunoRepository
import br.com.jhiltonsantos.matricula.repository.CursoRepository
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.util.UUID

@Path("/aluno")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Aluno", description = "Perfil do aluno autenticado")
@SecurityRequirement(name = "bearerAuth")
@RolesAllowed("ALUNO")
class AlunoResource(
    private val alunoRepository: AlunoRepository,
    private val cursoRepository: CursoRepository
) {
    @Inject
    lateinit var jwt: JsonWebToken

    @Operation(summary = "Retorna o perfil do aluno autenticado")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Perfil do aluno (id, nome, curso)"),
        APIResponse(responseCode = "401", description = "Requisicao sem token valido"),
        APIResponse(responseCode = "403", description = "Token sem role ALUNO"),
    )
    @GET
    @Path("perfil")
    fun perfil(): AlunoPerfilResponse {
        val aluno = alunoRepository.findById(UUID.fromString(jwt.subject))!!
        val curso = cursoRepository.findById(aluno.cursoId)!!
        return AlunoPerfilResponse(aluno.id!!, aluno.nome, aluno.cursoId, curso.nome)
    }
}
