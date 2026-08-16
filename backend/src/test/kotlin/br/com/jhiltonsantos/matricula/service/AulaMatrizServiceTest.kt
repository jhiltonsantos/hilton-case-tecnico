package br.com.jhiltonsantos.matricula.service

import br.com.jhiltonsantos.matricula.domain.AulaCursoAutorizado
import br.com.jhiltonsantos.matricula.domain.AulaMatriz
import br.com.jhiltonsantos.matricula.domain.Curso
import br.com.jhiltonsantos.matricula.domain.DiaSemana
import br.com.jhiltonsantos.matricula.domain.Disciplina
import br.com.jhiltonsantos.matricula.domain.Horario
import br.com.jhiltonsantos.matricula.domain.Professor
import br.com.jhiltonsantos.matricula.domain.exception.AcessoNegadoException
import br.com.jhiltonsantos.matricula.domain.exception.AulaComMatriculadosException
import br.com.jhiltonsantos.matricula.domain.exception.DisciplinaJaOfertadaNoHorarioException
import br.com.jhiltonsantos.matricula.domain.exception.DisciplinaNaoEncontradaException
import br.com.jhiltonsantos.matricula.dto.AtualizarAulaRequest
import br.com.jhiltonsantos.matricula.dto.CriarAulaRequest
import br.com.jhiltonsantos.matricula.repository.AulaCursoAutorizadoRepository
import br.com.jhiltonsantos.matricula.repository.AulaMatrizRepository
import br.com.jhiltonsantos.matricula.repository.CursoRepository
import br.com.jhiltonsantos.matricula.repository.DisciplinaRepository
import br.com.jhiltonsantos.matricula.repository.HorarioRepository
import br.com.jhiltonsantos.matricula.repository.ProfessorRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalTime
import java.util.UUID

class AulaMatrizServiceTest {

    private val aulaMatrizRepository = mockk<AulaMatrizRepository>()
    private val aulaCursoAutorizadoRepository = mockk<AulaCursoAutorizadoRepository>()
    private val disciplinaRepository = mockk<DisciplinaRepository>()
    private val professorRepository = mockk<ProfessorRepository>()
    private val horarioRepository = mockk<HorarioRepository>()
    private val cursoRepository = mockk<CursoRepository>()

    private val service = AulaMatrizService(
        aulaMatrizRepository,
        aulaCursoAutorizadoRepository,
        disciplinaRepository,
        professorRepository,
        horarioRepository,
        cursoRepository,
    )

    private val disciplinaId = UUID.randomUUID()
    private val professorId = UUID.randomUUID()
    private val horarioId = UUID.randomUUID()
    private val cursoId = UUID.randomUUID()
    private val coordenadorId = UUID.randomUUID()

    @BeforeEach
    fun stubEntidadesExistentes() {
        every { disciplinaRepository.findById(disciplinaId) } returns Disciplina("Disciplina Teste").apply { id = disciplinaId }
        every { professorRepository.findById(professorId) } returns Professor("Professor Teste").apply { id = professorId }
        every { horarioRepository.findById(horarioId) } returns
            Horario(DiaSemana.SEGUNDA, LocalTime.of(8, 0), LocalTime.of(10, 0)).apply { id = horarioId }
        every { cursoRepository.findById(cursoId) } returns Curso("Curso Teste").apply { id = cursoId }
        every { aulaCursoAutorizadoRepository.persist(any<AulaCursoAutorizado>()) } just Runs
    }

    @Test
    fun `criar lanca DisciplinaJaOfertadaNoHorarioException quando ja existe aula ativa com mesma disciplina e horario`() {
        every { aulaMatrizRepository.existeAtivaComDisciplinaEHorario(disciplinaId, horarioId, null) } returns true

        assertThrows(DisciplinaJaOfertadaNoHorarioException::class.java) {
            service.criar(
                CriarAulaRequest(disciplinaId, professorId, horarioId, listOf(cursoId), vagasMaximas = 10),
                coordenadorId,
            )
        }
        verify(exactly = 0) { aulaMatrizRepository.persist(any<AulaMatriz>()) }
    }

    @Test
    fun `criar persiste normalmente quando nao ha duplicidade de disciplina e horario`() {
        every { aulaMatrizRepository.existeAtivaComDisciplinaEHorario(disciplinaId, horarioId, null) } returns false
        every { aulaMatrizRepository.persist(any<AulaMatriz>()) } answers {
            firstArg<AulaMatriz>().id = UUID.randomUUID()
        }

        val aula = service.criar(
            CriarAulaRequest(disciplinaId, professorId, horarioId, listOf(cursoId), vagasMaximas = 10),
            coordenadorId,
        )

        assertEquals(disciplinaId, aula.disciplinaId)
        verify(exactly = 1) { aulaMatrizRepository.persist(any<AulaMatriz>()) }
    }

    @Test
    fun `editar lanca DisciplinaJaOfertadaNoHorarioException quando novo horario colide com outra aula ativa da mesma disciplina`() {
        val aulaId = UUID.randomUUID()
        val novoHorarioId = UUID.randomUUID()
        val aulaExistente =
            AulaMatriz(disciplinaId, professorId, horarioId, coordenadorId, vagasMaximas = 10).apply { id = aulaId }

        every { aulaMatrizRepository.findByIdAtivo(aulaId) } returns aulaExistente
        every { horarioRepository.findById(novoHorarioId) } returns
            Horario(DiaSemana.TERCA, LocalTime.of(14, 0), LocalTime.of(16, 0)).apply { id = novoHorarioId }
        every {
            aulaMatrizRepository.existeAtivaComDisciplinaEHorario(
                disciplinaId,
                novoHorarioId,
                aulaId
            )
        } returns true

        assertThrows(DisciplinaJaOfertadaNoHorarioException::class.java) {
            service.editar(aulaId, coordenadorId, AtualizarAulaRequest(professorId, novoHorarioId, listOf(cursoId)))
        }
        assertEquals(horarioId, aulaExistente.horarioId)
    }

    @Test
    fun `editar aplica alteracoes quando nao ha colisao de horario`() {
        val aulaId = UUID.randomUUID()
        val novoHorarioId = UUID.randomUUID()
        val novoProfessorId = UUID.randomUUID()
        val aulaExistente =
            AulaMatriz(disciplinaId, professorId, horarioId, coordenadorId, vagasMaximas = 10).apply { id = aulaId }

        every { aulaMatrizRepository.findByIdAtivo(aulaId) } returns aulaExistente
        every { professorRepository.findById(novoProfessorId) } returns mockk<Professor>()
        every { horarioRepository.findById(novoHorarioId) } returns mockk<Horario>()
        every {
            aulaMatrizRepository.existeAtivaComDisciplinaEHorario(
                disciplinaId,
                novoHorarioId,
                aulaId
            )
        } returns false
        every { aulaCursoAutorizadoRepository.delete("aulaMatrizId", aulaId) } returns 1L

        val aulaEditada =
            service.editar(aulaId, coordenadorId, AtualizarAulaRequest(novoProfessorId, novoHorarioId, listOf(cursoId)))

        assertEquals(novoHorarioId, aulaEditada.horarioId)
        assertEquals(novoProfessorId, aulaEditada.professorId)
    }

    @Test
    fun `criar lanca DisciplinaNaoEncontradaException quando disciplina nao existe`() {
        every { disciplinaRepository.findById(disciplinaId) } returns null

        assertThrows(DisciplinaNaoEncontradaException::class.java) {
            service.criar(
                CriarAulaRequest(disciplinaId, professorId, horarioId, listOf(cursoId), vagasMaximas = 10),
                coordenadorId,
            )
        }
    }

    @Test
    fun `excluir lanca AulaComMatriculadosException quando ha alunos matriculados`() {
        val aulaId = UUID.randomUUID()
        val aulaComMatriculados =
            AulaMatriz(disciplinaId, professorId, horarioId, coordenadorId, vagasMaximas = 10, vagasOcupadas = 3)
                .apply { id = aulaId }
        every { aulaMatrizRepository.findByIdAtivo(aulaId) } returns aulaComMatriculados

        assertThrows(AulaComMatriculadosException::class.java) {
            service.excluir(aulaId, coordenadorId)
        }
    }

    @Test
    fun `excluir lanca AcessoNegadoException quando a aula nao pertence ao coordenador`() {
        val aulaId = UUID.randomUUID()
        val donoReal = UUID.randomUUID()
        val aulaDeOutroCoordenador = AulaMatriz(disciplinaId, professorId, horarioId, donoReal, vagasMaximas = 10)
            .apply { id = aulaId }
        every { aulaMatrizRepository.findByIdAtivo(aulaId) } returns aulaDeOutroCoordenador

        assertThrows(AcessoNegadoException::class.java) {
            service.excluir(aulaId, coordenadorId)
        }
    }

}
