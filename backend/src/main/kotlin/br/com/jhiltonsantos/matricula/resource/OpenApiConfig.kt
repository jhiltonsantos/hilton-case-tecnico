package br.com.jhiltonsantos.matricula.resource

import jakarta.ws.rs.core.Application
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType
import org.eclipse.microprofile.openapi.annotations.info.Info
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme

@OpenAPIDefinition(
    info = Info(
        title = "API de Matricula e Matriz Curricular",
        version = "1.0.0",
        description = "Gestao da matriz curricular por coordenadores e matricula de alunos em aulas, com auth via Keycloak (OIDC).",
    ),
)
@SecurityScheme(
    securitySchemeName = "bearerAuth",
    type = SecuritySchemeType.OAUTH2,
    flows = OAuthFlows(
        password = OAuthFlow(
            tokenUrl =
                "http://localhost:8081/realms/matricula/protocol/openid-connect/token",
            scopes = [],
        ),
    ),
)
class OpenApiConfig : Application()
