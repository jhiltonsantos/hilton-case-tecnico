package br.com.jhiltonsantos.matricula.resource

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.oidc.Claim
import io.quarkus.test.security.oidc.OidcSecurity
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

@QuarkusTest
class CoordenadorResourceTest {

    companion object {
        // UUID e nome de coordenador1 no seed
        private const val COORDENADOR1 = "60000000-0000-0000-0000-000000000001"
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `GET coordenador perfil retorna os dados do proprio coordenador logado`() {
        given()
            .`when`().get("/coordenador/perfil")
            .then()
            .statusCode(200)
            .body("id", equalTo(COORDENADOR1))
            .body("nome", equalTo("Fernando Alves"))
    }

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    fun `GET coordenador perfil com role ALUNO retorna 403`() {
        given().`when`().get("/coordenador/perfil").then().statusCode(403)
    }
}
