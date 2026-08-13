CREATE TABLE curso (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL
);

CREATE TABLE disciplina (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL
);

CREATE TABLE professor (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL
);

CREATE TABLE horario (
    id UUID PRIMARY KEY,
    dia_semana VARCHAR(20) NOT NULL,
    horario_inicio TIME NOT NULL,
    horario_fim TIME NOT NULL,
    CONSTRAINT horario_intervalo_valido CHECK (horario_inicio < horario_fim)
);

CREATE TABLE aluno (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    curso_id UUID NOT NULL REFERENCES curso(id)
);

CREATE TABLE coordenador (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL
);
