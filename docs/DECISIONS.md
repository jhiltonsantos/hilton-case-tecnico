## Testcontainers via Quarkus Dev Services

Os testes que usam o QuarkusTest (regras de concorrência e endpoints) não
configuram o quarkus.datasource.jdbc.url para os testes. Decidi dessa forma pois o Quarkus Dev Services detecta a falta de configuração e sobe um container Postgres via Testcontainers antes dos testes rodarem, e derruba ao final. Testes de segurança (TestSecurity / JwtSecurity) não dependem de um Keycloak real, então os testes rodam por inteiro sem nenhuma infraestrutura externa — só Docker disponível na máquina.

## Concorrência no controle de vagas

O controle de vagas da matrícula usa um `UPDATE` condicional
(`UPDATE aula_matriz SET vagas_ocupadas = vagas_ocupadas + 1 WHERE id = ? AND
vagas_ocupadas < vagas_maximas`), checando o número de linhas afetadas, em vez de
"checar se tem vaga e depois gravar" em dois passos separados ou de lock no nível da aplicação. É o próprio banco quem garante a
atomicidade da operação, onde se duas requisições concorrentes tentam ocupar a última
vaga ao mesmo tempo, o banco serializa as duas operações e só uma tem `vagas_ocupadas
< vagas_maximas` verdadeiro no momento da execução; a outra afeta zero linhas e recebe erro de vaga indisponível. Coberto por teste de concorrência real (MatriculaConcorrenciaTest).

## Documentação de payloads no Swagger utilizando a forma automática

Os payloads de request (CriarAulaRequest, AtualizarAulaRequest,
MatricularRequest) e a maioria das respostas são documentados no Swagger/OpenAPI
sem anotação `@Schema` manual em cada campo, o OpenAPI gera o schema
automaticamente a partir das data classes Kotlin.

Optei por não anotar campo a campo com descrições e
exemplos pois o nome dos campos já é autoexplicativo (`disciplinaId`,
`vagasMaximas`) e o ganho de anotar cada um não compensava o tempo dado o prazo do
desafio. Os pontos onde a forma automática não bastava (`POST /aulas` e `POST /matriculas`, que retornam o tipo genérico Response) tiveram `@Schema` adicionado.

## Validação extra: professor não pode ter duas aulas no mesmo horário

O enunciado só exige que não tenha disciplina + horário na matriz curricular (a mesma disciplina pode se repetir, desde que em horários diferentes). Durante os testes manuais na aplicação percebi que era possível criar duas aulas diferentes no mesmo horário com o mesmo professor — cenário que o enunciado não cobre, mas que não faz sentido no domínio real (um professor não pode estar em duas salas ao mesmo tempo).

Decidi adicionar essa validação mesmo não sendo exigida, seguindo o mesmo padrão já usado para a regra de disciplina + horário (checagem antes de persistir, tanto na criação quanto na edição da aula). Fica registrado aqui como uma decisão extra, não como parte do escopo obrigatório do desafio.

## Testes de frontend
Escrevi testes de componente na lógica de negócio das duas telas principais
(carregamento de dados, validação de formulário, exclusividade de filtros na tela
do coordenador, escopo de aulas disponíveis por curso, cálculo de matrícula ativa e
tratamento de erro na tela do aluno).

Não escrevi testes de integração HTTP
completos nem testes end-to-end, priorizando cobrir as regras de negócio que o
desafio realmente avalia dentro do tempo disponível.
