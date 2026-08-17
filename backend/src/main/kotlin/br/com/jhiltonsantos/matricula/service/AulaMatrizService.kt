package br.com.jhiltonsantos.matricula.service

import br.com.jhiltonsantos.matricula.domain.AulaCursoAutorizado
import br.com.jhiltonsantos.matricula.domain.AulaMatriz
import br.com.jhiltonsantos.matricula.domain.PeriodoDia
import br.com.jhiltonsantos.matricula.domain.exception.AulaComMatriculadosException
import br.com.jhiltonsantos.matricula.domain.exception.AulaNaoEncontradaException
import br.com.jhiltonsantos.matricula.domain.exception.CursoNaoEncontradoException
import br.com.jhiltonsantos.matricula.domain.exception.DisciplinaJaOfertadaNoHorarioException
import br.com.jhiltonsantos.matricula.domain.exception.DisciplinaNaoEncontradaException
import br.com.jhiltonsantos.matricula.domain.exception.HorarioNaoEncontradoException
import br.com.jhiltonsantos.matricula.domain.exception.ProfessorJaAlocadoNoHorarioException
import br.com.jhiltonsantos.matricula.domain.exception.ProfessorNaoEncontradoException
import br.com.jhiltonsantos.matricula.domain.exception.AcessoNegadoException

import br.com.jhiltonsantos.matricula.dto.AtualizarAulaRequest
import br.com.jhiltonsantos.matricula.dto.CriarAulaRequest

import br.com.jhiltonsantos.matricula.repository.AulaCursoAutorizadoRepository
import br.com.jhiltonsantos.matricula.repository.AulaMatrizRepository
import br.com.jhiltonsantos.matricula.repository.CursoRepository
import br.com.jhiltonsantos.matricula.repository.DisciplinaRepository
import br.com.jhiltonsantos.matricula.repository.HorarioRepository
import br.com.jhiltonsantos.matricula.repository.ProfessorRepository

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.LocalTime
import java.util.UUID

@ApplicationScoped
class AulaMatrizService(
    private val aulaMatrizRepository: AulaMatrizRepository,
    private val aulaCursoAutorizadoRepository: AulaCursoAutorizadoRepository,
    private val disciplinaRepository: DisciplinaRepository,
    private val professorRepository: ProfessorRepository,
    private val horarioRepository: HorarioRepository,
    private val cursoRepository: CursoRepository,
) {
    @Transactional
    fun criar(request: CriarAulaRequest, coordenadorId: UUID): AulaMatriz {
        val disciplina = disciplinaRepository.findById(request.disciplinaId)
            ?: throw DisciplinaNaoEncontradaException(request.disciplinaId)
        val professor = professorRepository.findById(request.professorId)
            ?: throw ProfessorNaoEncontradoException(request.professorId)
        val horario = horarioRepository.findById(request.horarioId)
            ?: throw HorarioNaoEncontradoException(request.horarioId)
        request.cursosAutorizados.forEach { cursoId ->
            cursoRepository.findById(cursoId) ?: throw CursoNaoEncontradoException(cursoId)
        }
        if (aulaMatrizRepository.existeAtivaComDisciplinaEHorario(request.disciplinaId, request.horarioId)) {
            throw DisciplinaJaOfertadaNoHorarioException(disciplina.nome, horario.descricao())
        }
        if (aulaMatrizRepository.existeAtivaComProfessorEHorario(request.professorId, request.horarioId)) {
            throw ProfessorJaAlocadoNoHorarioException(professor.nome, horario.descricao())
        }

        val aula = AulaMatriz(
            disciplinaId = request.disciplinaId,
            professorId = request.professorId,
            horarioId = request.horarioId,
            coordenadorId = coordenadorId,
            vagasMaximas = request.vagasMaximas,
        )
        aulaMatrizRepository.persist(aula)
        vincularCursos(aula.id!!, request.cursosAutorizados)

        return aula
    }

    @Transactional
    fun editar(id: UUID, coordenadorId: UUID, request: AtualizarAulaRequest): AulaMatriz {
        val aula = aulaMatrizRepository.findByIdAtivo(id) ?: throw AulaNaoEncontradaException(id)
        if (aula.coordenadorId != coordenadorId) {
            throw AcessoNegadoException("Aula nao pertence a este coordenador")
        }

        val professor = professorRepository.findById(request.professorId)
            ?: throw ProfessorNaoEncontradoException(request.professorId)
        val horario = horarioRepository.findById(request.horarioId)
            ?: throw HorarioNaoEncontradoException(request.horarioId)
        request.cursosAutorizados.forEach { cursoId ->
            cursoRepository.findById(cursoId) ?: throw CursoNaoEncontradoException(cursoId)
        }
        if (aulaMatrizRepository.existeAtivaComDisciplinaEHorario(aula.disciplinaId, request.horarioId, excluirId = id)) {
            val disciplina = disciplinaRepository.findById(aula.disciplinaId)
            throw DisciplinaJaOfertadaNoHorarioException(disciplina?.nome ?: aula.disciplinaId.toString(), horario.descricao())
        }
        // Diferente da regra de disciplina (que le da aula, por nao ser editavel), aqui a
        // checagem usa o professor do request: professor e um dos campos editaveis.
        if (aulaMatrizRepository.existeAtivaComProfessorEHorario(request.professorId, request.horarioId, excluirId = id)) {
            throw ProfessorJaAlocadoNoHorarioException(professor.nome, horario.descricao())
        }

        aula.professorId = request.professorId
        aula.horarioId = request.horarioId

        aulaCursoAutorizadoRepository.delete("aulaMatrizId", id)
        vincularCursos(id, request.cursosAutorizados)

        return aula
    }

    @Transactional
    fun excluir(id: UUID, coordenadorId: UUID) {
        val aula = aulaMatrizRepository.findByIdAtivo(id) ?: throw AulaNaoEncontradaException(id)
        if (aula.coordenadorId != coordenadorId) {
            throw AcessoNegadoException("Aula nao pertence a este coordenador")
        }
        if (!aula.podeSerExcluida()) throw AulaComMatriculadosException(descricao(aula))
        aula.ativo = false
    }

    fun descricao(aula: AulaMatriz): String {
        val disciplina = disciplinaRepository.findById(aula.disciplinaId)
        val horario = horarioRepository.findById(aula.horarioId)
        val nomeDisciplina = disciplina?.nome ?: aula.disciplinaId.toString()
        val descricaoHorario = horario?.descricao() ?: aula.horarioId.toString()
        return "$nomeDisciplina ($descricaoHorario)"
    }

    fun pesquisar(
        periodoDia: PeriodoDia?,
        horarioInicio: LocalTime?,
        horarioFim: LocalTime?,
        cursoId: UUID?,
        vagasMaximas: Int?,
        coordenadorId: UUID?,
    ): List<AulaMatriz> {
        val horarioIds = when {
            periodoDia != null -> horarioRepository.idsPorPeriodo(periodoDia)
            horarioInicio != null && horarioFim != null -> horarioRepository.idsPorIntervalo(horarioInicio, horarioFim)
            else -> null
        }
        val aulaIds = cursoId?.let { aulaCursoAutorizadoRepository.aulaIdsPorCurso(it) }

        return aulaMatrizRepository.buscar(horarioIds, aulaIds, vagasMaximas, coordenadorId)
    }

    fun cursosAutorizadosDe(aulaId: UUID): List<UUID> =
        aulaCursoAutorizadoRepository.find("aulaMatrizId", aulaId).list().mapNotNull { it.cursoId }

    private fun vincularCursos(aulaId: UUID, cursoIds: List<UUID>) {
        cursoIds.forEach { cursoId ->
            aulaCursoAutorizadoRepository.persist(AulaCursoAutorizado(aulaMatrizId = aulaId, cursoId = cursoId))
        }
    }
}
