package br.com.jhiltonsantos.matricula.service

import br.com.jhiltonsantos.matricula.domain.Matricula
import br.com.jhiltonsantos.matricula.domain.exception.AlunoNaoEncontradoException
import br.com.jhiltonsantos.matricula.domain.exception.AulaNaoEncontradaException
import br.com.jhiltonsantos.matricula.domain.exception.ChoqueDeHorarioException
import br.com.jhiltonsantos.matricula.domain.exception.CursoNaoAutorizadoException
import br.com.jhiltonsantos.matricula.domain.exception.VagaIndisponivelException
import br.com.jhiltonsantos.matricula.dto.MatricularRequest
import br.com.jhiltonsantos.matricula.repository.AlunoRepository
import br.com.jhiltonsantos.matricula.repository.AulaMatrizRepository
import br.com.jhiltonsantos.matricula.repository.CursoRepository
import br.com.jhiltonsantos.matricula.repository.HorarioRepository
import br.com.jhiltonsantos.matricula.repository.MatriculaRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID

@ApplicationScoped
class MatriculaService(
    private val matriculaRepository: MatriculaRepository,
    private val aulaMatrizRepository: AulaMatrizRepository,
    private val aulaMatrizService: AulaMatrizService,
    private val alunoRepository: AlunoRepository,
    private val horarioRepository: HorarioRepository,
    private val cursoRepository: CursoRepository,
) {

    @Transactional
    fun matricular(alunoId: UUID, request: MatricularRequest): Matricula {
        val aula = aulaMatrizRepository.findByIdAtivo(request.aulaMatrizId)
            ?: throw AulaNaoEncontradaException(request.aulaMatrizId)
        val aluno = alunoRepository.findById(alunoId) ?: throw AlunoNaoEncontradoException(alunoId)

        val cursosAutorizados = aulaMatrizService.cursosAutorizadosDe(aula.id!!)
        if (aluno.cursoId !in cursosAutorizados) {
            val cursoNome = cursoRepository.findById(aluno.cursoId)?.nome ?: aluno.cursoId.toString()
            throw CursoNaoAutorizadoException(cursoNome, aulaMatrizService.descricao(aula))
        }

        val horarioNovo = horarioRepository.findById(aula.horarioId)!!.paraIntervalo()
        matriculaRepository.ativasDoAluno(alunoId).forEach { matriculaAtiva ->
            val aulaAtiva = aulaMatrizRepository.findById(matriculaAtiva.aulaMatrizId)!!
            val horarioAtivo = horarioRepository.findById(aulaAtiva.horarioId)!!.paraIntervalo()
            if (horarioNovo.conflitaCom(horarioAtivo)) {
                throw ChoqueDeHorarioException(aulaMatrizService.descricao(aula), aulaMatrizService.descricao(aulaAtiva))
            }
        }

        if (!aulaMatrizRepository.ocuparVaga(aula.id!!)) throw VagaIndisponivelException(aulaMatrizService.descricao(aula))

        val matricula = Matricula(alunoId = alunoId, aulaMatrizId = aula.id!!)
        matriculaRepository.persist(matricula)
        return matricula
    }

    fun minhasMatriculas(alunoId: UUID): List<Matricula> = matriculaRepository.ativasDoAluno(alunoId)
}
