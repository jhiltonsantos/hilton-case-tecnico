package br.com.jhiltonsantos.matricula.resource

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.oidc.Claim
import io.quarkus.test.security.oidc.OidcSecurity
import io.restassured.RestAssured.given
import org.junit.jupiter.api.Test
import org.hamcrest.Matchers.equalTo

@QuarkusTest
class AlunoResourceTest {
    companion object {
        // UUID e nome de aluno1 no seed
        private const val ALUNO1 = "50000000-0000-0000-0000-000000000001"
    }

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    @OidcSecurity(claims = [Claim(key = "sub", value = ALUNO1)])
    fun `GET aluno perfil retorna os dados do proprio aluno logado`() {
        given()
            .`when`().get("/aluno/perfil")
            .then()
            .statusCode(200)
            .body("id", equalTo(ALUNO1))
            .body("nome", equalTo("Joao Souza"))
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    fun `GET aluno perfil com role COORDENADOR retorna 403`() {
        given().`when`().get("/aluno/perfil").then().statusCode(403)
    }
}
