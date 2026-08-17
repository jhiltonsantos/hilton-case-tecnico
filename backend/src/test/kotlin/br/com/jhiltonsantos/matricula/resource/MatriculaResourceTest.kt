package br.com.jhiltonsantos.matricula.resource

import br.com.jhiltonsantos.matricula.support.SuporteDominio
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.oidc.Claim
import io.quarkus.test.security.oidc.OidcSecurity
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class MatriculaResourceTest {

    companion object {
        private const val ALUNO1 = "50000000-0000-0000-0000-000000000001" // curso: Ciencia da Computacao
        private const val ALUNO2 = "50000000-0000-0000-0000-000000000002" // curso: Ciencia da Computacao
        private const val COORDENADOR1 = "60000000-0000-0000-0000-000000000001"

        private const val CALCULO_I = "10000000-0000-0000-0000-000000000001"
        private const val ALGEBRA_LINEAR = "10000000-0000-0000-0000-000000000003"
        private const val PROFESSOR_ANA = "20000000-0000-0000-0000-000000000001"
        private const val PROFESSOR_BRUNO = "20000000-0000-0000-0000-000000000002"
        private const val SEGUNDA_MANHA = "30000000-0000-0000-0000-000000000001"

        private const val CIENCIA_COMPUTACAO = "40000000-0000-0000-0000-000000000001"
        private const val ENGENHARIA_SOFTWARE = "40000000-0000-0000-0000-000000000002"
    }

    @Inject
    lateinit var suporte: SuporteDominio

    @BeforeEach
    fun limparDominio() {
        suporte.limpar()
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

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    @OidcSecurity(claims = [Claim(key = "sub", value = ALUNO1)])
    fun `POST matriculas retorna 201 quando curso autorizado, com vaga e sem choque de horario`() {
        val aulaId = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )

        given()
            .contentType("application/json")
            .body("""{"aulaMatrizId":"$aulaId"}""")
            .`when`().post("/matriculas")
            .then().statusCode(201)
    }

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    @OidcSecurity(claims = [Claim(key = "sub", value = ALUNO1)])
    fun `POST matriculas com curso nao autorizado retorna 422`() {
        val aulaId = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(ENGENHARIA_SOFTWARE)), // aluno1 e de Ciencia da Computacao
        )

        given()
            .contentType("application/json")
            .body("""{"aulaMatrizId":"$aulaId"}""")
            .`when`().post("/matriculas")
            .then().statusCode(422)
    }

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    @OidcSecurity(claims = [Claim(key = "sub", value = ALUNO1)])
    fun `POST matriculas com vaga indisponivel retorna 422`() {
        val aulaId = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
            vagasMaximas = 1,
        )
        suporte.matricular(UUID.fromString(ALUNO2), aulaId) // ocupa a unica vaga

        given()
            .contentType("application/json")
            .body("""{"aulaMatrizId":"$aulaId"}""")
            .`when`().post("/matriculas")
            .then().statusCode(422)
    }

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    @OidcSecurity(claims = [Claim(key = "sub", value = ALUNO1)])
    fun `POST matriculas com choque de horario retorna 422`() {
        val aulaJaMatriculada = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )
        suporte.matricular(UUID.fromString(ALUNO1), aulaJaMatriculada)

        // Mesmo horario (SEGUNDA_MANHA), disciplina e professor diferentes.
        val aulaConflitante = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(ALGEBRA_LINEAR),
            UUID.fromString(PROFESSOR_BRUNO),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )

        given()
            .contentType("application/json")
            .body("""{"aulaMatrizId":"$aulaConflitante"}""")
            .`when`().post("/matriculas")
            .then().statusCode(422)
    }
}
