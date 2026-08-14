package br.com.jhiltonsantos.matricula.resource

import br.com.jhiltonsantos.matricula.dto.CursoResponse
import br.com.jhiltonsantos.matricula.dto.DisciplinaResponse
import br.com.jhiltonsantos.matricula.dto.HorarioResponse
import br.com.jhiltonsantos.matricula.dto.ProfessorResponse
import br.com.jhiltonsantos.matricula.repository.CursoRepository
import br.com.jhiltonsantos.matricula.repository.DisciplinaRepository
import br.com.jhiltonsantos.matricula.repository.HorarioRepository
import br.com.jhiltonsantos.matricula.repository.ProfessorRepository
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement
import org.eclipse.microprofile.openapi.annotations.tags.Tag

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Catalogo", description = "Cadastros fixos consultados para formularios (disciplina, professor, horario, curso)")
@SecurityRequirement(name = "bearerAuth")
@RolesAllowed("COORDENADOR", "ALUNO")
class CatalogoResource(
    private val disciplinaRepository: DisciplinaRepository,
    private val professorRepository: ProfessorRepository,
    private val horarioRepository: HorarioRepository,
    private val cursoRepository: CursoRepository,
) {

    @Operation(summary = "Lista todas as disciplinas cadastradas")
    @APIResponse(responseCode = "200", description = "Lista de disciplinas")
    @GET
    @Path("disciplinas")
    fun disciplinas(): List<DisciplinaResponse> =
        disciplinaRepository.listAll().map { DisciplinaResponse(it.id!!, it.nome) }

    @Operation(summary = "Lista todos os professores cadastrados")
    @APIResponse(responseCode = "200", description = "Lista de professores")
    @GET
    @Path("professores")
    fun professores(): List<ProfessorResponse> =
        professorRepository.listAll().map { ProfessorResponse(it.id!!, it.nome) }

    @Operation(summary = "Lista todos os horarios cadastrados")
    @APIResponse(responseCode = "200", description = "Lista de horarios")
    @GET
    @Path("horarios")
    fun horarios(): List<HorarioResponse> =
        horarioRepository.listAll().map { HorarioResponse(it.id!!, it.diaSemana, it.horarioInicio, it.horarioFim) }

    @Operation(summary = "Lista todos os cursos cadastrados")
    @APIResponse(responseCode = "200", description = "Lista de cursos")
    @GET
    @Path("cursos")
    fun cursos(): List<CursoResponse> =
        cursoRepository.listAll().map { CursoResponse(it.id!!, it.nome) }
}
