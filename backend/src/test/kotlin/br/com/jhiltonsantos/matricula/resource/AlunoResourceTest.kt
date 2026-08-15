package br.com.jhiltonsantos.matricula.resource

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.jwt.Claim
import io.quarkus.test.security.jwt.JwtSecurity
import io.restassured.RestAssured.given
import org.junit.jupiter.api.Test
import org.hamcrest.Matchers.equalTo

@QuarkusTest
class AlunoResourceTest {
    // UUID e nome de aluno1 no seed
    private val aluno1 = "50000000-0000-0000-0000-000000000001"

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    @JwtSecurity(claims = [Claim(key = "sub", value = aluno1)])
    fun `GET aluno perfil retorna os dados do proprio aluno logado`() {
        given()
            .`when`().get("/aluno/perfil")
            .then()
            .statusCode(200)
            .body("id", equalTo(aluno1))
            .body("nome", equalTo("Joao Souza"))
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    fun `GET aluno perfil com role COORDENADOR retorna 403`() {
        given().`when`().get("/aluno/perfil").then().statusCode(403)
    }
}
