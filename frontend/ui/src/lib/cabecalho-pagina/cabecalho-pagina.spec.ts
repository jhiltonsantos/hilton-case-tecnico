import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import Keycloak from 'keycloak-js';
import { CabecalhoPagina } from './cabecalho-pagina';

describe('CabecalhoPagina', () => {
  let component: CabecalhoPagina;
  let fixture: ComponentFixture<CabecalhoPagina>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CabecalhoPagina],
      providers: [provideRouter([]), { provide: Keycloak, useValue: {} }],
    }).compileComponents();

    fixture = TestBed.createComponent(CabecalhoPagina);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('eyebrow', 'Matrícula');
    fixture.componentRef.setInput('titulo', 'Título de teste');
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
