import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import Keycloak from 'keycloak-js';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';
import {
  AlunoService,
  API_BASE_URL,
  Aula,
  AulaService,
  CatalogoService,
  formatarHorario,
  Horario,
  Matricula,
  MatriculaService,
} from '@frontend/data-access';
import { MatriculaAluno } from './matricula-aluno';

describe('MatriculaAluno', () => {
  let component: MatriculaAluno;
  let fixture: ComponentFixture<MatriculaAluno>;
  let alunoService: { perfil: ReturnType<typeof vi.fn> };
  let aulaService: { listar: ReturnType<typeof vi.fn> };
  let matriculaService: { minhasMatriculas: ReturnType<typeof vi.fn>; matricular: ReturnType<typeof vi.fn> };
  let catalogoService: {
    disciplinas: ReturnType<typeof vi.fn>;
    professores: ReturnType<typeof vi.fn>;
    horarios: ReturnType<typeof vi.fn>;
    cursos: ReturnType<typeof vi.fn>;
  };

  const perfilMock = { id: 'aluno-1', nome: 'Ana Silva', cursoId: 'curso-cc', cursoNome: 'Ciencia da Computacao' };

  const aulaMock: Aula = {
    id: 'aula-1',
    disciplinaId: 'disc-1',
    professorId: 'prof-1',
    horarioId: 'hor-1',
    coordenadorId: 'coord-1',
    cursosAutorizados: ['curso-cc'],
    vagasMaximas: 30,
    vagasOcupadas: 0,
    ativo: true,
  };

  const matriculaAtivaMock: Matricula = {
    id: 'mat-1',
    aulaMatrizId: 'aula-1',
    disciplinaId: 'disc-1',
    professorId: 'prof-1',
    horarioId: 'hor-1',
    status: 'ATIVA',
    criadoEm: '2026-01-01T00:00:00Z',
  };

  // ngOnInit já é disparado automaticamente pelo TestBed ao criar o componente (Angular
  // 21) — por isso os mocks precisam estar prontos ANTES de chamar isso, nunca depois
  // (chamar ngOnInit() de novo manualmente duplicaria a inscrição no pipeline do
  // recarregar$, inflando a contagem de chamadas dos services).
  async function criarComponente(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [MatriculaAluno],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: API_BASE_URL, useValue: 'http://localhost:8080' },
        { provide: Keycloak, useValue: {} },
        { provide: AlunoService, useValue: alunoService },
        { provide: AulaService, useValue: aulaService },
        { provide: MatriculaService, useValue: matriculaService },
        { provide: CatalogoService, useValue: catalogoService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MatriculaAluno);
    component = fixture.componentInstance;
    await fixture.whenStable();
  }

  beforeEach(() => {
    alunoService = { perfil: vi.fn().mockReturnValue(of(perfilMock)) };
    aulaService = { listar: vi.fn().mockReturnValue(of([])) };
    matriculaService = {
      minhasMatriculas: vi.fn().mockReturnValue(of([])),
      matricular: vi.fn().mockReturnValue(of(matriculaAtivaMock)),
    };
    catalogoService = {
      disciplinas: vi.fn().mockReturnValue(of([])),
      professores: vi.fn().mockReturnValue(of([])),
      horarios: vi.fn().mockReturnValue(of([])),
      cursos: vi.fn().mockReturnValue(of([])),
    };
  });

  it('should create', async () => {
    await criarComponente();
    expect(component).toBeTruthy();
  });

  it('deve carregar aulas disponiveis do curso do aluno e minhas matriculas ao iniciar', async () => {
    aulaService.listar.mockReturnValue(of([aulaMock]));
    matriculaService.minhasMatriculas.mockReturnValue(of([matriculaAtivaMock]));

    await criarComponente();

    // A busca de aulas disponiveis tem que ser restrita ao curso do aluno logado —
    // é a regra que garante que ele só vê aulas em que pode se matricular.
    expect(aulaService.listar).toHaveBeenCalledWith({ cursoId: perfilMock.cursoId });
    expect(component.aulasDisponiveis()).toEqual([aulaMock]);
    expect(component.minhasMatriculas()).toEqual([matriculaAtivaMock]);
  });

  it('jaMatriculado deve retornar true somente para aula com matricula ativa', async () => {
    await criarComponente();
    component.minhasMatriculas.set([
      matriculaAtivaMock,
      { ...matriculaAtivaMock, id: 'mat-2', aulaMatrizId: 'aula-2', status: 'CANCELADA' },
    ]);

    expect(component.jaMatriculado('aula-1')).toBe(true);
    expect(component.jaMatriculado('aula-2')).toBe(false);
    expect(component.jaMatriculado('aula-desconhecida')).toBe(false);
  });

  it('matricular deve chamar matriculaService com o aulaMatrizId da aula selecionada', async () => {
    await criarComponente();

    component.matricular(aulaMock);

    expect(matriculaService.matricular).toHaveBeenCalledWith({ aulaMatrizId: aulaMock.id });
  });

  it('matricular com sucesso deve mostrar mensagem de sucesso e recarregar as listas', async () => {
    await criarComponente();
    const addSpy = vi.spyOn(fixture.debugElement.injector.get(MessageService), 'add');
    expect(alunoService.perfil).toHaveBeenCalledTimes(1);

    component.matricular(aulaMock);

    expect(addSpy).toHaveBeenCalledWith(expect.objectContaining({ severity: 'success' }));
    // O recarregar$ dispara o mesmo pipeline de novo — prova que a lista some/reaparece
    // atualizada em vez de precisar de um F5.
    expect(alunoService.perfil).toHaveBeenCalledTimes(2);
  });

  it('matricular com erro deve mostrar a mensagem de erro retornada pela api', async () => {
    matriculaService.matricular.mockReturnValue(throwError(() => ({ error: { mensagem: 'Sem vaga disponivel' } })));
    await criarComponente();
    const addSpy = vi.spyOn(fixture.debugElement.injector.get(MessageService), 'add');

    component.matricular(aulaMock);

    expect(addSpy).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'error', detail: 'Sem vaga disponivel' }),
    );
    expect(component.matriculando()).toBeNull();
  });

  it('horarioLabel deve retornar o horario formatado ou o id quando o horario nao for encontrado', async () => {
    await criarComponente();
    const horarioMock: Horario = { id: 'hor-1', diaSemana: 'SEGUNDA', horarioInicio: '08:00:00', horarioFim: '10:00:00' };
    component.horarios.set([horarioMock]);

    expect(component.horarioLabel('hor-1')).toBe(formatarHorario(horarioMock));
    expect(component.horarioLabel('hor-desconhecido')).toBe('hor-desconhecido');
  });
});
