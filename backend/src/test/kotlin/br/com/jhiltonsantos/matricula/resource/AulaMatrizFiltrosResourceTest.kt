package br.com.jhiltonsantos.matricula.resource

import br.com.jhiltonsantos.matricula.support.SuporteDominio
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.oidc.Claim
import io.quarkus.test.security.oidc.OidcSecurity
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Cobre os filtros de pesquisa da matriz curricular exigidos pelo desafio (intervalo de
 * horario, periodo do dia, curso autorizado e quantidade de vagas).
 *
 * Fica separado do AulaMatrizResourceTest porque precisa de um cenario fixo de varias
 * aulas montado antes de cada teste, e porque as asserções sao por contagem — o que so
 * e confiavel com o banco limpo (ver SuporteDominio).
 */
@QuarkusTest
class AulaMatrizFiltrosResourceTest {

    companion object {
        private const val COORDENADOR1 = "60000000-0000-0000-0000-000000000001"

        // Horarios do seed, escolhidos para cobrir periodos e dias diferentes:
        // SEGUNDA 08:00-10:00 (manha), TERCA 14:00-16:00 (tarde), QUARTA 19:00-21:00 (noite)
        private const val SEGUNDA_08_10 = "30000000-0000-0000-0000-000000000001"
        private const val TERCA_14_16 = "30000000-0000-0000-0000-000000000004"
        private const val QUARTA_19_21 = "30000000-0000-0000-0000-000000000006"

        private const val CALCULO_I = "10000000-0000-0000-0000-000000000001"
        private const val CALCULO_II = "10000000-0000-0000-0000-000000000002"
        private const val ALGEBRA_LINEAR = "10000000-0000-0000-0000-000000000003"

        private const val PROFESSOR_ANA = "20000000-0000-0000-0000-000000000001"
        private const val PROFESSOR_BRUNO = "20000000-0000-0000-0000-000000000002"
        private const val PROFESSOR_CARLA = "20000000-0000-0000-0000-000000000003"

        private const val CIENCIA_COMPUTACAO = "40000000-0000-0000-0000-000000000001"
        private const val ENGENHARIA_SOFTWARE = "40000000-0000-0000-0000-000000000002"
    }

    @Inject
    lateinit var suporte: SuporteDominio

    private lateinit var aulaManhaId: UUID
    private lateinit var aulaTardeId: UUID
    private lateinit var aulaNoiteId: UUID

    @BeforeEach
    fun montarCenario() {
        suporte.limpar()

        // Manha, 15 vagas, so Ciencia da Computacao
        aulaManhaId = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_08_10),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
            vagasMaximas = 15,
        )
        // Tarde, 30 vagas, so Engenharia de Software
        aulaTardeId = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_II),
            UUID.fromString(PROFESSOR_BRUNO),
            UUID.fromString(TERCA_14_16),
            listOf(UUID.fromString(ENGENHARIA_SOFTWARE)),
            vagasMaximas = 30,
        )
        // Noite, 30 vagas, os dois cursos
        aulaNoiteId = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(ALGEBRA_LINEAR),
            UUID.fromString(PROFESSOR_CARLA),
            UUID.fromString(QUARTA_19_21),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO), UUID.fromString(ENGENHARIA_SOFTWARE)),
            vagasMaximas = 30,
        )
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `sem filtro retorna todas as aulas do coordenador`() {
        given()
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("", hasSize<Any>(3))
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `filtro por periodo do dia retorna so as aulas daquele periodo`() {
        given()
            .queryParam("periodoDia", "MANHA")
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("", hasSize<Any>(1))
            .body("id", contains(aulaManhaId.toString()))

        given()
            .queryParam("periodoDia", "TARDE")
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("id", contains(aulaTardeId.toString()))

        given()
            .queryParam("periodoDia", "NOITE")
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("id", contains(aulaNoiteId.toString()))
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `filtro por intervalo de horario retorna so as aulas contidas no intervalo`() {
        given()
            .queryParam("horarioInicio", "08:00")
            .queryParam("horarioFim", "10:00")
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("", hasSize<Any>(1))
            .body("id", contains(aulaManhaId.toString()))

        // Intervalo largo o suficiente para pegar a aula da manha e a da tarde, mas nao a da noite.
        given()
            .queryParam("horarioInicio", "08:00")
            .queryParam("horarioFim", "16:00")
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("id", containsInAnyOrder(aulaManhaId.toString(), aulaTardeId.toString()))
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `filtro por curso autorizado retorna so as aulas abertas para o curso`() {
        given()
            .queryParam("cursoId", CIENCIA_COMPUTACAO)
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("id", containsInAnyOrder(aulaManhaId.toString(), aulaNoiteId.toString()))

        given()
            .queryParam("cursoId", ENGENHARIA_SOFTWARE)
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("id", containsInAnyOrder(aulaTardeId.toString(), aulaNoiteId.toString()))
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `filtro por vagas maximas retorna so as aulas com aquela quantidade`() {
        given()
            .queryParam("vagasMaximas", 15)
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("", hasSize<Any>(1))
            .body("id", contains(aulaManhaId.toString()))

        given()
            .queryParam("vagasMaximas", 30)
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("id", containsInAnyOrder(aulaTardeId.toString(), aulaNoiteId.toString()))
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `filtros combinados se acumulam`() {
        given()
            .queryParam("cursoId", CIENCIA_COMPUTACAO)
            .queryParam("vagasMaximas", 30)
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("", hasSize<Any>(1))
            .body("id", contains(aulaNoiteId.toString()))
    }
}
