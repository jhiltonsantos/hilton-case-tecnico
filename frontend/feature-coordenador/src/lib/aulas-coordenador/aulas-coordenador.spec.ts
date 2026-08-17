import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import Keycloak from 'keycloak-js';
import { of } from 'rxjs';
import { API_BASE_URL, Aula, AulaService, CatalogoService } from '@frontend/data-access';
import { AulasCoordenador } from './aulas-coordenador';

describe('AulasCoordenador', () => {
  let component: AulasCoordenador;
  let fixture: ComponentFixture<AulasCoordenador>;
  let aulaService: {
    listar: ReturnType<typeof vi.fn>;
    criar: ReturnType<typeof vi.fn>;
    editar: ReturnType<typeof vi.fn>;
    excluir: ReturnType<typeof vi.fn>;
  };
  let catalogoService: {
    disciplinas: ReturnType<typeof vi.fn>;
    professores: ReturnType<typeof vi.fn>;
    horarios: ReturnType<typeof vi.fn>;
    cursos: ReturnType<typeof vi.fn>;
  };

  const aulaMock: Aula = {
    id: 'aula-1',
    disciplinaId: 'disc-1',
    professorId: 'prof-1',
    horarioId: 'hor-1',
    coordenadorId: 'coord-1',
    cursosAutorizados: ['curso-1'],
    vagasMaximas: 30,
    vagasOcupadas: 0,
    ativo: true,
  };

  // ngOnInit já é disparado automaticamente pelo TestBed ao criar o componente (Angular
  // 21) — por isso os mocks precisam estar prontos ANTES de chamar isso, nunca depois
  // (chamar ngOnInit() de novo manualmente duplicaria a inscrição nos observables).
  async function criarComponente(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [AulasCoordenador],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: API_BASE_URL, useValue: 'http://localhost:8080' },
        { provide: Keycloak, useValue: {} },
        { provide: AulaService, useValue: aulaService },
        { provide: CatalogoService, useValue: catalogoService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AulasCoordenador);
    component = fixture.componentInstance;
    await fixture.whenStable();
  }

  beforeEach(() => {
    aulaService = {
      listar: vi.fn().mockReturnValue(of([])),
      criar: vi.fn().mockReturnValue(of(aulaMock)),
      editar: vi.fn().mockReturnValue(of(aulaMock)),
      excluir: vi.fn().mockReturnValue(of(undefined)),
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

  it('deve carregar aulas, disciplinas, professores, horarios e cursos ao iniciar', async () => {
    const disciplinasMock = [{ id: 'disc-1', nome: 'Calculo I' }];
    catalogoService.disciplinas.mockReturnValue(of(disciplinasMock));
    aulaService.listar.mockReturnValue(of([aulaMock]));

    await criarComponente();

    expect(component.disciplinas()).toEqual(disciplinasMock);
    expect(component.aulas()).toEqual([aulaMock]);
  });

  it('abrirNovaAula deve resetar o formulario para os valores padrao e abrir o dialogo', async () => {
    await criarComponente();
    component.abrirEdicao(aulaMock); // deixa o form preenchido antes, pra provar que reseta de verdade

    component.abrirNovaAula();

    expect(component.aulaEmEdicao()).toBeNull();
    expect(component.form.value).toEqual({
      disciplinaId: '',
      professorId: '',
      horarioId: '',
      cursosAutorizados: [],
      vagasMaximas: 1,
    });
    expect(component.dialogAberto()).toBe(true);
  });

  it('abrirEdicao deve preencher o formulario com os dados da aula selecionada', async () => {
    await criarComponente();

    component.abrirEdicao(aulaMock);

    expect(component.aulaEmEdicao()).toEqual(aulaMock);
    expect(component.form.value).toEqual({
      disciplinaId: aulaMock.disciplinaId,
      professorId: aulaMock.professorId,
      horarioId: aulaMock.horarioId,
      cursosAutorizados: aulaMock.cursosAutorizados,
      vagasMaximas: aulaMock.vagasMaximas,
    });
    expect(component.dialogAberto()).toBe(true);
  });

  it('salvar nao deve chamar aulaService quando o formulario esta invalido', async () => {
    await criarComponente();
    expect(component.form.invalid).toBe(true);

    component.salvar();

    expect(aulaService.criar).not.toHaveBeenCalled();
    expect(aulaService.editar).not.toHaveBeenCalled();
  });

  it('salvar em modo edicao deve chamar aulaService.editar com o payload sem disciplina e vagas maximas', async () => {
    await criarComponente();
    component.abrirEdicao(aulaMock);
    expect(component.form.valid).toBe(true);

    component.salvar();

    expect(aulaService.editar).toHaveBeenCalledWith(aulaMock.id, {
      professorId: aulaMock.professorId,
      horarioId: aulaMock.horarioId,
      cursosAutorizados: aulaMock.cursosAutorizados,
    });
    expect(aulaService.criar).not.toHaveBeenCalled();
  });

  it('aplicarFiltros deve dar precedencia ao periodo do dia sobre o intervalo de horario', async () => {
    await criarComponente();
    // Simula estado "sujo": periodo do dia e intervalo preenchidos ao mesmo tempo — a
    // exclusividade tem que valer mesmo se os handlers de UI que normalmente limpam um
    // ao escolher o outro não rodarem primeiro.
    component.filtroForm.setValue({
      periodoDia: 'MANHA',
      horarioInicio: '08:00',
      horarioFim: '10:00',
      cursoId: '',
      vagasMaximas: null,
    });

    component.aplicarFiltros();

    expect(aulaService.listar).toHaveBeenCalledWith({ periodoDia: 'MANHA' });
  });
});
