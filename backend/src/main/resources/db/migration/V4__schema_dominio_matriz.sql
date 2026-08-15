CREATE TABLE aula_matriz (
    id UUID PRIMARY KEY,
    disciplina_id UUID NOT NULL REFERENCES disciplina(id),
    professor_id UUID NOT NULL REFERENCES professor(id),
    horario_id UUID NOT NULL REFERENCES horario(id),
    coordenador_id UUID NOT NULL REFERENCES coordenador(id),
    vagas_maximas INTEGER NOT NULL CHECK (vagas_maximas > 0),
    vagas_ocupadas INTEGER NOT NULL DEFAULT 0 CHECK (vagas_ocupadas >= 0),
    ativo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE aula_curso_autorizado (
    id UUID PRIMARY KEY,
    aula_matriz_id UUID NOT NULL REFERENCES aula_matriz(id),
    curso_id UUID NOT NULL REFERENCES curso(id),
    CONSTRAINT aula_curso_autorizado_unico UNIQUE (aula_matriz_id, curso_id)
);

CREATE TABLE matricula (
    id UUID PRIMARY KEY,
    aluno_id UUID NOT NULL REFERENCES aluno(id),
    aula_matriz_id UUID NOT NULL REFERENCES aula_matriz(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_matricula_aluno_ativa ON matricula (aluno_id) WHERE status = 'ATIVA';
