import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AulasCoordenador } from './aulas-coordenador';

describe('AulasCoordenador', () => {
  let component: AulasCoordenador;
  let fixture: ComponentFixture<AulasCoordenador>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AulasCoordenador],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(AulasCoordenador);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
