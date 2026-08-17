package br.com.jhiltonsantos.matricula.resource

import br.com.jhiltonsantos.matricula.dto.CoordenadorPerfilResponse
import br.com.jhiltonsantos.matricula.repository.CoordenadorRepository
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

@Path("/coordenador")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Coordenador", description = "Perfil do coordenador autenticado")
@SecurityRequirement(name = "bearerAuth")
@RolesAllowed("COORDENADOR")
class CoordenadorResource(
    private val coordenadorRepository: CoordenadorRepository,
) {
    @Inject
    lateinit var jwt: JsonWebToken

    @Operation(summary = "Retorna o perfil do coordenador autenticado")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Perfil do coordenador (id, nome)"),
        APIResponse(responseCode = "401", description = "Requisicao sem token valido"),
        APIResponse(responseCode = "403", description = "Token sem role COORDENADOR"),
    )
    @GET
    @Path("perfil")
    fun perfil(): CoordenadorPerfilResponse {
        val coordenador = coordenadorRepository.findById(UUID.fromString(jwt.subject))!!
        return CoordenadorPerfilResponse(coordenador.id!!, coordenador.nome)
    }
}
