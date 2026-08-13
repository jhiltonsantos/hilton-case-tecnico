import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatureAluno } from './feature-aluno';

describe('FeatureAluno', () => {
  let component: FeatureAluno;
  let fixture: ComponentFixture<FeatureAluno>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatureAluno],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatureAluno);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
