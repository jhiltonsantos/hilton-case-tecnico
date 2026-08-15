package br.com.jhiltonsantos.matricula.resource

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured.given
import org.junit.jupiter.api.Test
import org.hamcrest.Matchers.equalTo

@QuarkusTest
class AulaMatrizResourceTest {

    @Test
    fun `GET aulas sem token retorna 401`() {
        given().`when`().get("/aulas").then().statusCode(401)
    }

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    fun `POST aulas com role ALUNO retorna 403`() {
        given()
            .contentType("application/json")
            .body("{}")
            .`when`().post("/aulas")
            .then().statusCode(403)
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    fun `GET aulas sem nenhum filtro retorna 200, nao 400`() {
        given().`when`().get("/aulas").then().statusCode(200)
    }

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    fun `GET aulas com role ALUNO tambem retorna 200`() {
        given().`when`().get("/aulas").then().statusCode(200)
    }
}
