## Testcontainers via Quarkus Dev Services

Os testes que usam o QuarkusTest (regras de concorrência e endpoints) não
configuram o quarkus.datasource.jdbc.url para os testes. Decidi dessa forma pois o Quarkus Dev Services detecta a falta de configuração e sobe um container Postgres via Testcontainers antes dos testes rodarem, e derruba ao final. Testes de segurança (TestSecurity / JwtSecurity) não dependem de um Keycloak real, então os testes rodam por inteiro sem nenhuma infraestrutura externa — só Docker disponível na máquina.

## Validação extra: professor não pode ter duas aulas no mesmo horário

O enunciado só exige que não tenha disciplina + horário na matriz curricular (a mesma disciplina pode se repetir, desde que em horários diferentes). Durante os testes manuais na aplicação percebi que era possível criar duas aulas diferentes no mesmo horário com o mesmo professor — cenário que o enunciado não cobre, mas que não faz sentido no domínio real (um professor não pode estar em duas salas ao mesmo tempo).

Decidi adicionar essa validação mesmo não sendo exigida, seguindo o mesmo padrão já usado para a regra de disciplina + horário (checagem antes de persistir, tanto na criação quanto na edição da aula). Fica registrado aqui como uma decisão extra, não como parte do escopo obrigatório do desafio.
