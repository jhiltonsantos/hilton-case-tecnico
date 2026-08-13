import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatureCoordenador } from './feature-coordenador';

describe('FeatureCoordenador', () => {
  let component: FeatureCoordenador;
  let fixture: ComponentFixture<FeatureCoordenador>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatureCoordenador],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatureCoordenador);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
