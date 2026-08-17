import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { Subject, finalize, forkJoin, startWith, switchMap } from 'rxjs';
import {
  AlunoService,
  AulaService,
  CatalogoService,
  MatriculaService,
  Aula,
  Curso,
  Disciplina,
  formatarHorario,
  Horario,
  Matricula,
  ordenarPorHorario,
  Professor,
} from '@frontend/data-access';
import { CabecalhoPagina } from '@frontend/ui';

@Component({
  selector: 'lib-matricula-aluno',
  standalone: true,
  imports: [CommonModule, TableModule, ButtonModule, TagModule, ToastModule, CabecalhoPagina],
  providers: [MessageService],
  templateUrl: './matricula-aluno.html',
  styleUrl: './matricula-aluno.scss',
})
export class MatriculaAluno implements OnInit {
  private readonly alunoService = inject(AlunoService);
  private readonly aulaService = inject(AulaService);
  private readonly matriculaService = inject(MatriculaService);
  private readonly catalogoService = inject(CatalogoService);
  private readonly messageService = inject(MessageService);

  private readonly recarregar$ = new Subject<void>();

  disciplinas = signal<Disciplina[]>([]);
  professores = signal<Professor[]>([]);
  horarios = signal<Horario[]>([]);
  cursos = signal<Curso[]>([]);

  aulasDisponiveis = signal<Aula[]>([]);
  minhasMatriculas = signal<Matricula[]>([]);
  matriculando = signal<string | null>(null);

  aulasDisponiveisOrdenadas = computed(() => ordenarPorHorario(this.aulasDisponiveis(), this.horarios()));
  minhasMatriculasOrdenadas = computed(() => ordenarPorHorario(this.minhasMatriculas(), this.horarios()));

  ngOnInit(): void {
    this.catalogoService.disciplinas().subscribe((v) => this.disciplinas.set(v));
    this.catalogoService.professores().subscribe((v) => this.professores.set(v));
    this.catalogoService.horarios().subscribe((v) => this.horarios.set(v));

    this.recarregar$
      .pipe(
        startWith(undefined),
        switchMap(() => this.alunoService.perfil()),
        switchMap((perfil) =>
          forkJoin({
            aulas: this.aulaService.listar({ cursoId: perfil.cursoId }),
            matriculas: this.matriculaService.minhasMatriculas(),
          }),
        ),
      )
      .subscribe(({ aulas, matriculas }) => {
        this.aulasDisponiveis.set(aulas);
        this.minhasMatriculas.set(matriculas);
      });
  }

  jaMatriculado(aulaId: string): boolean {
    return this.minhasMatriculas().some((m) => m.aulaMatrizId === aulaId && m.status === 'ATIVA');
  }

  matricular(aula: Aula): void {
    this.matriculando.set(aula.id);
    this.matriculaService
      .matricular({ aulaMatrizId: aula.id })
      .pipe(finalize(() => this.matriculando.set(null)))
      .subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Matrícula realizada' });
          this.recarregar$.next();
        },
        error: (erro) =>
          this.messageService.add({
            severity: 'error',
            summary: 'Não foi possível matricular',
            detail: erro.error?.mensagem ?? 'Tente novamente',
          }),
      });
  }

  nomeDisciplina(id: string): string {
    return this.disciplinas().find((d) => d.id === id)?.nome ?? id;
  }

  nomeProfessor(id: string): string {
    return this.professores().find((p) => p.id === id)?.nome ?? id;
  }

  horarioLabel(id: string): string {
    const horario = this.horarios().find((h) => h.id === id);
    return horario ? formatarHorario(horario) : id;
  }
}
