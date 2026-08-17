package br.com.jhiltonsantos.matricula.support

import br.com.jhiltonsantos.matricula.domain.AulaMatriz
import br.com.jhiltonsantos.matricula.domain.StatusMatricula
import br.com.jhiltonsantos.matricula.dto.CriarAulaRequest
import br.com.jhiltonsantos.matricula.dto.MatricularRequest
import br.com.jhiltonsantos.matricula.repository.AulaCursoAutorizadoRepository
import br.com.jhiltonsantos.matricula.repository.AulaMatrizRepository
import br.com.jhiltonsantos.matricula.repository.MatriculaRepository
import br.com.jhiltonsantos.matricula.service.AulaMatrizService
import br.com.jhiltonsantos.matricula.service.MatriculaService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID

/**
 * Apoio para os testes @QuarkusTest, que compartilham o mesmo Postgres (subido pelo Dev
 * Services) sem nenhum isolamento: o Flyway roda uma unica vez e dados gravados por um
 * teste sobrevivem para os proximos, o que torna asserções por contagem fragis e
 * dependentes da ordem de execucao.
 *
 * Alem da limpeza, expõe leituras e escritas em transacao propria. Isso importa porque
 * um teste que chama a API via RestAssured nao pode ser @Transactional (a requisicao HTTP
 * roda em outra transacao e nao enxergaria dados ainda nao commitados).
 */
@ApplicationScoped
class SuporteDominio(
    private val matriculaRepository: MatriculaRepository,
    private val aulaCursoAutorizadoRepository: AulaCursoAutorizadoRepository,
    private val aulaMatrizRepository: AulaMatrizRepository,
    private val aulaMatrizService: AulaMatrizService,
    private val matriculaService: MatriculaService,
) {

    /** Zera so as tabelas de dominio; as do seed (curso, disciplina, aluno, ...) nunca sao tocadas. */
    @Transactional
    fun limpar() {
        matriculaRepository.deleteAll()
        aulaCursoAutorizadoRepository.deleteAll()
        aulaMatrizRepository.deleteAll()
    }

    /** Cria uma aula ja commitada, para o cenario que o teste vai exercitar via HTTP. */
    @Transactional
    fun criarAula(
        coordenadorId: UUID,
        disciplinaId: UUID,
        professorId: UUID,
        horarioId: UUID,
        cursosAutorizados: List<UUID>,
        vagasMaximas: Int = 30,
    ): UUID = aulaMatrizService.criar(
        CriarAulaRequest(disciplinaId, professorId, horarioId, cursosAutorizados, vagasMaximas),
        coordenadorId,
    ).id!!

    @Transactional
    fun matricular(alunoId: UUID, aulaId: UUID) {
        matriculaService.matricular(alunoId, MatricularRequest(aulaId))
    }

    @Transactional
    fun buscarAula(aulaId: UUID): AulaMatriz? = aulaMatrizRepository.findById(aulaId)

    @Transactional
    fun contarMatriculasAtivas(alunoId: UUID): Int =
        matriculaRepository.ativasDoAluno(alunoId).count { it.status == StatusMatricula.ATIVA }
}
