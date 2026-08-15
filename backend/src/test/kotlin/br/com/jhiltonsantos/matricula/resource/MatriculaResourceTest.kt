package br.com.jhiltonsantos.matricula.resource

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.oidc.Claim
import io.quarkus.test.security.oidc.OidcSecurity
import io.restassured.RestAssured.given
import org.junit.jupiter.api.Test

@QuarkusTest
class MatriculaResourceTest {

    companion object {
        // UUID de aluno1 no seed
        private const val ALUNO1 = "50000000-0000-0000-0000-000000000001"
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    fun `GET matriculas com role COORDENADOR retorna 403`() {
        given().`when`().get("/matriculas").then().statusCode(403)
    }

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    @OidcSecurity(claims = [Claim(key = "sub", value = ALUNO1)])
    fun `GET matriculas do aluno logado retorna 200`() {
        given().`when`().get("/matriculas").then().statusCode(200)
    }

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    @OidcSecurity(claims = [Claim(key = "sub", value = ALUNO1)])
    fun `POST matriculas com aula inexistente retorna 404`() {
        given()
            .contentType("application/json")
            .body("""{"aulaMatrizId":"99999999-0000-0000-0000-000000000099"}""")
            .`when`().post("/matriculas")
            .then().statusCode(404)
    }
}
