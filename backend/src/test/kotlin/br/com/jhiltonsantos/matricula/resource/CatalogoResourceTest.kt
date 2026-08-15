package br.com.jhiltonsantos.matricula.resource

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured.given
import org.junit.jupiter.api.Test

@QuarkusTest
class CatalogoResourceTest {
    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    fun `disciplinas professores horarios e cursos retornam 200 para ALUNO`() {
        listOf("/disciplinas", "/professores", "/horarios", "/cursos").forEach { caminho ->
            given().`when`().get(caminho).then().statusCode(200)
        }
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    fun `disciplinas professores horarios e cursos retornam 200 para COORDENADOR`() {
        listOf("/disciplinas", "/professores", "/horarios", "/cursos").forEach { caminho ->
            given().`when`().get(caminho).then().statusCode(200)
        }
    }
}
