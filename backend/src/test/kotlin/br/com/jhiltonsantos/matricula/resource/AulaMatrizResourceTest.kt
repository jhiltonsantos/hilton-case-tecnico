package br.com.jhiltonsantos.matricula.resource

import br.com.jhiltonsantos.matricula.support.SuporteDominio
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.test.security.oidc.Claim
import io.quarkus.test.security.oidc.OidcSecurity
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class AulaMatrizResourceTest {

    companion object {
        // UUIDs do seed (V3__seed_cadastros_fixos.sql)
        private const val COORDENADOR1 = "60000000-0000-0000-0000-000000000001"
        private const val COORDENADOR2 = "60000000-0000-0000-0000-000000000002"
        private const val ALUNO1 = "50000000-0000-0000-0000-000000000001"

        private const val CALCULO_I = "10000000-0000-0000-0000-000000000001"
        private const val ALGEBRA_LINEAR = "10000000-0000-0000-0000-000000000003"
        private const val PROFESSOR_ANA = "20000000-0000-0000-0000-000000000001"
        private const val PROFESSOR_BRUNO = "20000000-0000-0000-0000-000000000002"
        private const val SEGUNDA_MANHA = "30000000-0000-0000-0000-000000000001"
        private const val TERCA_MANHA = "30000000-0000-0000-0000-000000000003"
        private const val CIENCIA_COMPUTACAO = "40000000-0000-0000-0000-000000000001"

        private const val ID_INEXISTENTE = "99999999-0000-0000-0000-000000000099"
    }

    @Inject
    lateinit var suporte: SuporteDominio

    @BeforeEach
    fun limparDominio() {
        suporte.limpar()
    }

    private fun corpoCriarAula(
        disciplinaId: String = CALCULO_I,
        professorId: String = PROFESSOR_ANA,
        horarioId: String = SEGUNDA_MANHA,
        vagasMaximas: Int = 30,
    ) = """
        {
          "disciplinaId": "$disciplinaId",
          "professorId": "$professorId",
          "horarioId": "$horarioId",
          "cursosAutorizados": ["$CIENCIA_COMPUTACAO"],
          "vagasMaximas": $vagasMaximas
        }
    """.trimIndent()

    private fun corpoAtualizarAula(
        professorId: String = PROFESSOR_BRUNO,
        horarioId: String = TERCA_MANHA,
    ) = """
        {
          "professorId": "$professorId",
          "horarioId": "$horarioId",
          "cursosAutorizados": ["$CIENCIA_COMPUTACAO"]
        }
    """.trimIndent()

    // --- Autenticacao e autorizacao ---

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
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    fun `PUT aulas com role ALUNO retorna 403`() {
        given()
            .contentType("application/json")
            .body(corpoAtualizarAula())
            .`when`().put("/aulas/$ID_INEXISTENTE")
            .then().statusCode(403)
    }

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    fun `DELETE aulas com role ALUNO retorna 403`() {
        given().`when`().delete("/aulas/$ID_INEXISTENTE").then().statusCode(403)
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `GET aulas sem nenhum filtro retorna 200, nao 400`() {
        given().`when`().get("/aulas").then().statusCode(200)
    }

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    fun `GET aulas com role ALUNO tambem retorna 200`() {
        given().`when`().get("/aulas").then().statusCode(200)
    }

    // --- Criacao ---

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `POST aulas retorna 201 com o corpo da aula criada`() {
        given()
            .contentType("application/json")
            .body(corpoCriarAula())
            .`when`().post("/aulas")
            .then()
            .statusCode(201)
            .body("disciplinaId", equalTo(CALCULO_I))
            .body("professorId", equalTo(PROFESSOR_ANA))
            .body("horarioId", equalTo(SEGUNDA_MANHA))
            .body("coordenadorId", equalTo(COORDENADOR1))
            .body("cursosAutorizados", contains(CIENCIA_COMPUTACAO))
            .body("vagasMaximas", equalTo(30))
            .body("vagasOcupadas", equalTo(0))
            .body("ativo", equalTo(true))
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `POST aulas com disciplina inexistente retorna 404`() {
        given()
            .contentType("application/json")
            .body(corpoCriarAula(disciplinaId = ID_INEXISTENTE))
            .`when`().post("/aulas")
            .then().statusCode(404)
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `POST aulas com mesma disciplina e horario retorna 422`() {
        suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )

        given()
            .contentType("application/json")
            // Professor diferente, para isolar a regra de disciplina da regra de professor.
            .body(corpoCriarAula(professorId = PROFESSOR_BRUNO))
            .`when`().post("/aulas")
            .then().statusCode(422)
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `POST aulas com professor ja alocado no mesmo horario retorna 422`() {
        suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )

        given()
            .contentType("application/json")
            // Disciplina diferente, para isolar a regra de professor da regra de disciplina.
            .body(corpoCriarAula(disciplinaId = ALGEBRA_LINEAR))
            .`when`().post("/aulas")
            .then().statusCode(422)
    }

    // --- Edicao ---

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `PUT aulas retorna 200 com professor e horario atualizados`() {
        val aulaId = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )

        given()
            .contentType("application/json")
            .body(corpoAtualizarAula())
            .`when`().put("/aulas/$aulaId")
            .then()
            .statusCode(200)
            .body("professorId", equalTo(PROFESSOR_BRUNO))
            .body("horarioId", equalTo(TERCA_MANHA))
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `PUT aulas ignora disciplina e vagas maximas enviadas no corpo`() {
        val aulaId = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
            vagasMaximas = 30,
        )

        val corpoComCamposNaoEditaveis = """
            {
              "disciplinaId": "$ALGEBRA_LINEAR",
              "vagasMaximas": 999,
              "professorId": "$PROFESSOR_BRUNO",
              "horarioId": "$TERCA_MANHA",
              "cursosAutorizados": ["$CIENCIA_COMPUTACAO"]
            }
        """.trimIndent()

        given()
            .contentType("application/json")
            .body(corpoComCamposNaoEditaveis)
            .`when`().put("/aulas/$aulaId")
            .then()
            .statusCode(200)
            .body("disciplinaId", equalTo(CALCULO_I))
            .body("vagasMaximas", equalTo(30))
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `PUT aulas de aula inexistente retorna 404`() {
        given()
            .contentType("application/json")
            .body(corpoAtualizarAula())
            .`when`().put("/aulas/$ID_INEXISTENTE")
            .then().statusCode(404)
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `PUT aulas de outro coordenador retorna 403`() {
        val aulaDeOutroCoordenador = suporte.criarAula(
            UUID.fromString(COORDENADOR2),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )

        given()
            .contentType("application/json")
            .body(corpoAtualizarAula())
            .`when`().put("/aulas/$aulaDeOutroCoordenador")
            .then().statusCode(403)
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `PUT aulas nao remove alunos ja matriculados`() {
        val aulaId = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )
        suporte.matricular(UUID.fromString(ALUNO1), aulaId)
        assertEquals(1, suporte.contarMatriculasAtivas(UUID.fromString(ALUNO1)))

        given()
            .contentType("application/json")
            .body(corpoAtualizarAula())
            .`when`().put("/aulas/$aulaId")
            .then().statusCode(200)

        assertEquals(
            1,
            suporte.contarMatriculasAtivas(UUID.fromString(ALUNO1)),
            "editar a aula nao pode cancelar/remover matricula existente",
        )
    }

    // --- Exclusao logica ---

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `DELETE aulas retorna 204 e mantem o registro no banco com ativo false`() {
        val aulaId = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )

        given().`when`().delete("/aulas/$aulaId").then().statusCode(204)

        // Some da listagem...
        given()
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("id", not(contains(aulaId.toString())))

        // ...mas continua no banco, so que inativa (exclusao logica).
        val aulaNoBanco = suporte.buscarAula(aulaId)
        assertNotNull(aulaNoBanco, "exclusao deve ser logica, o registro nao pode sumir da tabela")
        assertFalse(aulaNoBanco!!.ativo)
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `DELETE aulas de aula inexistente retorna 404`() {
        given().`when`().delete("/aulas/$ID_INEXISTENTE").then().statusCode(404)
    }

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `DELETE aulas com aluno matriculado retorna 422`() {
        val aulaId = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )
        suporte.matricular(UUID.fromString(ALUNO1), aulaId)

        given().`when`().delete("/aulas/$aulaId").then().statusCode(422)

        assertEquals(true, suporte.buscarAula(aulaId)?.ativo, "aula bloqueada nao pode ter sido inativada")
    }

    // --- Escopo por dono ---

    @Test
    @TestSecurity(user = "coordenador-teste", roles = ["COORDENADOR"])
    @OidcSecurity(claims = [Claim(key = "sub", value = COORDENADOR1)])
    fun `GET aulas retorna apenas as aulas do proprio coordenador`() {
        val aulaPropria = suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )
        suporte.criarAula(
            UUID.fromString(COORDENADOR2),
            UUID.fromString(ALGEBRA_LINEAR),
            UUID.fromString(PROFESSOR_BRUNO),
            UUID.fromString(TERCA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )

        given()
            .`when`().get("/aulas")
            .then()
            .statusCode(200)
            .body("", hasSize<Any>(1))
            .body("id", contains(aulaPropria.toString()))
            .body("coordenadorId", contains(COORDENADOR1))
    }

    @Test
    @TestSecurity(user = "aluno-teste", roles = ["ALUNO"])
    fun `GET aulas para aluno retorna aulas de todos os coordenadores`() {
        suporte.criarAula(
            UUID.fromString(COORDENADOR1),
            UUID.fromString(CALCULO_I),
            UUID.fromString(PROFESSOR_ANA),
            UUID.fromString(SEGUNDA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )
        suporte.criarAula(
            UUID.fromString(COORDENADOR2),
            UUID.fromString(ALGEBRA_LINEAR),
            UUID.fromString(PROFESSOR_BRUNO),
            UUID.fromString(TERCA_MANHA),
            listOf(UUID.fromString(CIENCIA_COMPUTACAO)),
        )

        given().`when`().get("/aulas").then().statusCode(200).body("", hasSize<Any>(2))
    }
}
