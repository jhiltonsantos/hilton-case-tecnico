package br.com.jhiltonsantos.matricula.domain.exception

open class RegraNegocioException(mensagem: String) : RuntimeException(mensagem)

class AulaComMatriculadosException(aulaDescricao: String) :
    RegraNegocioException(mensagem = "Aula possui alunos matriculados, exclusao nao permitida: $aulaDescricao")

class CursoNaoAutorizadoException(cursoNome: String, aulaDescricao: String) :
    RegraNegocioException(mensagem = "Curso do aluno ($cursoNome) nao autorizado para a aula: $aulaDescricao")

class ChoqueDeHorarioException(aulaDescricao: String, aulaConflitanteDescricao: String) :
    RegraNegocioException(mensagem = "Aula $aulaDescricao conflita com horario de outra matricula ativa: $aulaConflitanteDescricao")

class VagaIndisponivelException(aulaDescricao: String) :
    RegraNegocioException(mensagem = "Nao ha vaga disponivel na aula: $aulaDescricao")

class DisciplinaJaOfertadaNoHorarioException(disciplinaNome: String, horarioDescricao: String) :
    RegraNegocioException(mensagem = "Disciplina '$disciplinaNome' ja ofertada no horario: $horarioDescricao")
