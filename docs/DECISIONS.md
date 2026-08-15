## Testcontainers via Quarkus Dev Services

Os testes que usam o QuarkusTest (regras de concorrência e endpoints) não
configuram o quarkus.datasource.jdbc.url para os testes. Decidi dessa forma pois o Quarkus Dev Services detecta a falta de configuração e sobe um container Postgres via Testcontainers antes dos testes rodarem, e derruba ao final. Testes de segurança (TestSecurity / JwtSecurity) não dependem de um Keycloak real, então os testes rodam por inteiro sem nenhuma infraestrutura externa — só Docker disponível na máquina.
