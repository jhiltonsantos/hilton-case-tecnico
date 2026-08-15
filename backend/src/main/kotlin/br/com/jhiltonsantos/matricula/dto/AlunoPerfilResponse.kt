package br.com.jhiltonsantos.matricula.dto

import java.util.UUID

data class AlunoPerfilResponse(val id: UUID, val nome: String, val cursoId: UUID)
