import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { MultiSelectModule } from 'primeng/multiselect';
import { InputNumberModule } from 'primeng/inputnumber';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Aula, AulaService, CatalogoService, Curso, Disciplina, formatarHorario, Horario, Professor } from '@frontend/data-access';
import { CabecalhoPagina } from '@frontend/ui';

@Component({
  selector: 'lib-aulas-coordenador',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    SelectModule,
    MultiSelectModule,
    InputNumberModule,
    ConfirmDialogModule,
    ToastModule,
    CabecalhoPagina,
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './aulas-coordenador.html',
  styleUrl: './aulas-coordenador.scss',
})
export class AulasCoordenador implements OnInit {
  private readonly aulaService = inject(AulaService);
  private readonly catalogoService = inject(CatalogoService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly messageService = inject(MessageService);
  private readonly fb = inject(FormBuilder);

  aulas = signal<Aula[]>([]);
  disciplinas = signal<Disciplina[]>([]);
  professores = signal<Professor[]>([]);
  horarios = signal<Horario[]>([]);
  horariosOptions = computed(() =>
    this.horarios().map((h) => ({ ...h, label: formatarHorario(h) })),
  );
  cursos = signal<Curso[]>([]);

  dialogAberto = signal(false);
  aulaEmEdicao = signal<Aula | null>(null);

  form = this.fb.nonNullable.group({
    disciplinaId: ['', Validators.required],
    professorId: ['', Validators.required],
    horarioId: ['', Validators.required],
    cursosAutorizados: [[] as string[], Validators.required],
    vagasMaximas: [1, [Validators.required, Validators.min(1)]],
  });

  ngOnInit(): void {
    this.catalogoService.disciplinas().subscribe((v) => this.disciplinas.set(v));
    this.catalogoService.professores().subscribe((v) => this.professores.set(v));
    this.catalogoService.horarios().subscribe((v) => this.horarios.set(v));
    this.catalogoService.cursos().subscribe((v) => this.cursos.set(v));
    this.carregarAulas();
  }

  carregarAulas(): void {
    this.aulaService.listar().subscribe((v) => this.aulas.set(v));
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

  abrirNovaAula(): void {
    this.aulaEmEdicao.set(null);
    this.form.reset({ disciplinaId: '', professorId: '', horarioId: '', cursosAutorizados: [], vagasMaximas: 1 });
    this.dialogAberto.set(true);
  }

  abrirEdicao(aula: Aula): void {
    this.aulaEmEdicao.set(aula);
    this.form.reset({
      disciplinaId: aula.disciplinaId,
      professorId: aula.professorId,
      horarioId: aula.horarioId,
      cursosAutorizados: aula.cursosAutorizados,
      vagasMaximas: aula.vagasMaximas,
    });
    this.dialogAberto.set(true);
  }

  fecharDialogo(): void {
    this.dialogAberto.set(false);
  }

  salvar(): void {
    if (this.form.invalid) return;
    const valor = this.form.getRawValue();
    const emEdicao = this.aulaEmEdicao();

    const acao = emEdicao
      ? this.aulaService.editar(emEdicao.id, {
          professorId: valor.professorId,
          horarioId: valor.horarioId,
          cursosAutorizados: valor.cursosAutorizados,
        })
      : this.aulaService.criar(valor);

    acao.subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: emEdicao ? 'Aula editada' : 'Aula criada' });
        this.dialogAberto.set(false);
        this.carregarAulas();
      },
      error: (erro) =>
        this.messageService.add({ severity: 'error', summary: 'Erro', detail: erro.error?.mensagem ?? 'Falha ao salvar' }),
    });
  }

  confirmarExclusao(aula: Aula): void {
    this.confirmationService.confirm({
      message: `Excluir a aula de ${this.nomeDisciplina(aula.disciplinaId)}?`,
      accept: () => this.excluir(aula.id),
    });
  }

  private excluir(id: string): void {
    this.aulaService.excluir(id).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Aula excluida' });
        this.carregarAulas();
      },
      error: (erro) =>
        this.messageService.add({ severity: 'error', summary: 'Erro', detail: erro.error?.mensagem ?? 'Falha ao excluir' }),
    });
  }
}
