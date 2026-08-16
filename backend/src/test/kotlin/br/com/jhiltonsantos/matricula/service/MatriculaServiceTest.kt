package br.com.jhiltonsantos.matricula.service

import br.com.jhiltonsantos.matricula.domain.Aluno
import br.com.jhiltonsantos.matricula.domain.AulaMatriz
import br.com.jhiltonsantos.matricula.domain.Curso
import br.com.jhiltonsantos.matricula.domain.DiaSemana
import br.com.jhiltonsantos.matricula.domain.Horario
import br.com.jhiltonsantos.matricula.domain.Matricula
import br.com.jhiltonsantos.matricula.domain.exception.ChoqueDeHorarioException
import br.com.jhiltonsantos.matricula.domain.exception.CursoNaoAutorizadoException
import br.com.jhiltonsantos.matricula.domain.exception.VagaIndisponivelException
import br.com.jhiltonsantos.matricula.dto.MatricularRequest
import br.com.jhiltonsantos.matricula.repository.AlunoRepository
import br.com.jhiltonsantos.matricula.repository.AulaMatrizRepository
import br.com.jhiltonsantos.matricula.repository.CursoRepository
import br.com.jhiltonsantos.matricula.repository.HorarioRepository
import br.com.jhiltonsantos.matricula.repository.MatriculaRepository
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

class MatriculaServiceTest {

    private val matriculaRepository = mockk<MatriculaRepository>()
    private val aulaMatrizRepository = mockk<AulaMatrizRepository>()
    private val aulaMatrizService = mockk<AulaMatrizService>()
    private val alunoRepository = mockk<AlunoRepository>()
    private val horarioRepository = mockk<HorarioRepository>()
    private val cursoRepository = mockk<CursoRepository>()

    private val service = MatriculaService(
        matriculaRepository,
        aulaMatrizRepository,
        aulaMatrizService,
        alunoRepository,
        horarioRepository,
        cursoRepository,
    )

    private val alunoId = UUID.randomUUID()
    private val cursoDoAluno = UUID.randomUUID()
    private val aulaId = UUID.randomUUID()
    private val horarioId = UUID.randomUUID()

    private val horarioDaAula =
        Horario(DiaSemana.SEGUNDA, LocalTime.of(8, 0), LocalTime.of(10, 0)).apply { id = horarioId }
    private val aula = AulaMatriz(
        disciplinaId = UUID.randomUUID(),
        professorId = UUID.randomUUID(),
        horarioId = horarioId,
        coordenadorId = UUID.randomUUID(),
        vagasMaximas = 10,
    ).apply { id = aulaId }
    private val aluno = Aluno(nome = "Teste", cursoId = cursoDoAluno).apply { id = alunoId }

    @BeforeEach
    fun stubBasico() {
        every { aulaMatrizRepository.findByIdAtivo(aulaId) } returns aula
        every { alunoRepository.findById(alunoId) } returns aluno
        every { aulaMatrizService.cursosAutorizadosDe(aulaId) } returns listOf(cursoDoAluno)
        every { horarioRepository.findById(horarioId) } returns horarioDaAula
        every { matriculaRepository.ativasDoAluno(alunoId) } returns emptyList()
        every { cursoRepository.findById(cursoDoAluno) } returns Curso("Curso Teste").apply { id = cursoDoAluno }
        every { aulaMatrizService.descricao(aula) } returns "Disciplina Teste (Segunda 08:00-10:00)"
    }

    @Test
    fun `lanca CursoNaoAutorizadoException quando o curso do aluno nao esta entre os autorizados`() {
        every { aulaMatrizService.cursosAutorizadosDe(aulaId) } returns listOf(UUID.randomUUID())

        assertThrows(CursoNaoAutorizadoException::class.java) {
            service.matricular(alunoId, MatricularRequest(aulaId))
        }
    }

    @Test
    fun `lanca ChoqueDeHorarioException quando ha matricula ativa com horario conflitante`() {
        val outraAulaId = UUID.randomUUID()
        val outroHorarioId = UUID.randomUUID()
        val outraAula =
            AulaMatriz(UUID.randomUUID(), UUID.randomUUID(), outroHorarioId, UUID.randomUUID(), vagasMaximas = 10)
                .apply { id = outraAulaId }
        val outroHorario =
            Horario(DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(11, 0)).apply { id = outroHorarioId }
        val matriculaAtiva = Matricula(alunoId = alunoId, aulaMatrizId = outraAulaId)

        every { matriculaRepository.ativasDoAluno(alunoId) } returns listOf(matriculaAtiva)
        every { aulaMatrizRepository.findById(outraAulaId) } returns outraAula
        every { horarioRepository.findById(outroHorarioId) } returns outroHorario
        every { aulaMatrizService.descricao(outraAula) } returns "Outra Disciplina (Segunda 09:00-11:00)"

        assertThrows(ChoqueDeHorarioException::class.java) {
            service.matricular(alunoId, MatricularRequest(aulaId))
        }
    }

    @Test
    fun `lanca VagaIndisponivelException quando ocuparVaga retorna false`() {
        every { aulaMatrizRepository.ocuparVaga(aulaId) } returns false

        assertThrows(VagaIndisponivelException::class.java) {
            service.matricular(alunoId, MatricularRequest(aulaId))
        }
    }

    @Test
    fun `matricula com sucesso quando curso autorizado, sem choque de horario e com vaga`() {
        every { aulaMatrizRepository.ocuparVaga(aulaId) } returns true
        every { matriculaRepository.persist(any<Matricula>()) } just Runs

        val matricula = service.matricular(alunoId, MatricularRequest(aulaId))

        assertEquals(alunoId, matricula.alunoId)
        assertEquals(aulaId, matricula.aulaMatrizId)
        verify(exactly = 1) { matriculaRepository.persist(any<Matricula>()) }
    }
}
