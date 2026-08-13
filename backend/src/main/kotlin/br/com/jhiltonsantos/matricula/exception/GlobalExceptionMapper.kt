package br.com.jhiltonsantos.matricula.exception

import br.com.jhiltonsantos.matricula.domain.exception.EntidadeNaoEncontradaException
import br.com.jhiltonsantos.matricula.domain.exception.RegraNegocioException
import br.com.jhiltonsantos.matricula.dto.ErroResponse
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class GlobalExceptionMapper : ExceptionMapper<Exception> {

    override fun toResponse(exception: Exception): Response = when (exception) {
        is EntidadeNaoEncontradaException ->
            Response.status(Response.Status.NOT_FOUND).entity(ErroResponse(exception.message)).build()

        is RegraNegocioException ->
            Response.status(Response.Status.UNPROCESSABLE_ENTITY).entity(ErroResponse(exception.message)).build()

        is IllegalArgumentException ->
            Response.status(Response.Status.BAD_REQUEST).entity(ErroResponse(exception.message)).build()

        else ->
            Response.serverError().entity(ErroResponse("Erro interno")).build()
    }
}
