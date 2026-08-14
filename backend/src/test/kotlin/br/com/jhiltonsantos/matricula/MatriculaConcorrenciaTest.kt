package br.com.jhiltonsantos.matricula

import br.com.jhiltonsantos.matricula.dto.CriarAulaRequest
import br.com.jhiltonsantos.matricula.dto.MatricularRequest
import br.com.jhiltonsantos.matricula.repository.AlunoRepository
import br.com.jhiltonsantos.matricula.repository.DisciplinaRepository
import br.com.jhiltonsantos.matricula.repository.HorarioRepository
import br.com.jhiltonsantos.matricula.repository.ProfessorRepository
import br.com.jhiltonsantos.matricula.service.AulaMatrizService
import br.com.jhiltonsantos.matricula.service.MatriculaService
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@QuarkusTest
class MatriculaConcorrenciaTest {

    @Inject
    lateinit var aulaMatrizService: AulaMatrizService

    @Inject
    lateinit var matriculaService: MatriculaService

    @Inject
    lateinit var disciplinaRepository: DisciplinaRepository

    @Inject
    lateinit var professorRepository: ProfessorRepository

    @Inject
    lateinit var horarioRepository: HorarioRepository

    @Inject
    lateinit var alunoRepository: AlunoRepository

    // UUIDs do seed (coordenador1, aluno1, aluno2)
    private val coordenadorId = UUID.fromString("60000000-0000-0000-0000-000000000001")
    private val aluno1Id = UUID.fromString("50000000-0000-0000-0000-000000000001")
    private val aluno2Id = UUID.fromString("50000000-0000-0000-0000-000000000002")

    private lateinit var aulaComUmaVagaId: UUID

    @BeforeEach
    @Transactional
    fun criarAulaComUmaVaga() {
        val cursoAluno1 = alunoRepository.findById(aluno1Id)!!.cursoId
        val cursoAluno2 = alunoRepository.findById(aluno2Id)!!.cursoId

        val aula = aulaMatrizService.criar(
            CriarAulaRequest(
                disciplinaId = disciplinaRepository.listAll().first().id!!,
                professorId = professorRepository.listAll().first().id!!,
                horarioId = horarioRepository.listAll().first().id!!,
                cursosAutorizados = listOf(cursoAluno1, cursoAluno2).distinct(),
                vagasMaximas = 1,
            ),
            coordenadorId,
        )
        aulaComUmaVagaId = aula.id!!
    }

    @Test
    fun `duas matriculas simultaneas na ultima vaga so uma e aceita`() {
        val sucesso = AtomicInteger(0)
        val falha = AtomicInteger(0)
        val partida = CountDownLatch(1)
        val pronto = CountDownLatch(2)
        val pool = Executors.newFixedThreadPool(2)

        listOf(aluno1Id, aluno2Id).forEach { alunoId ->
            pool.submit {
                partida.await()
                try {
                    matriculaService.matricular(alunoId, MatricularRequest(aulaComUmaVagaId))
                    sucesso.incrementAndGet()
                } catch (e: Exception) {
                    falha.incrementAndGet()
                } finally {
                    pronto.countDown()
                }
            }
        }

        partida.countDown()
        pronto.await(10, TimeUnit.SECONDS)
        pool.shutdown()

        assertEquals(1, sucesso.get())
        assertEquals(1, falha.get())
    }
}
