import type { GenerationStepUI, StepType } from './generation-steps.ts';

export interface StepComponentProps {
  step: GenerationStepUI;
  onUpdate: (step: GenerationStepUI) => void;
  onRemove: () => void;
  readonly?: boolean;
}

export interface StepDefinition {
  type: StepType;
  label: string;
  isFinal: boolean;
  createDefault: () => GenerationStepUI;
  component: React.ComponentType<StepComponentProps>;
}

class StepRegistryClass {
  private steps = new Map<StepType, StepDefinition>();

  register(definition: StepDefinition) {
    this.steps.set(definition.type, definition);
  }

  getAll(): StepDefinition[] {
    return Array.from(this.steps.values());
  }

  get(type: StepType): StepDefinition | undefined {
    return this.steps.get(type);
  }

  getAvailableSteps(existingSteps: GenerationStepUI[]): StepDefinition[] {
    const hasFinalStep = existingSteps.some(step => {
      const def = this.get(step.type);
      return def?.isFinal;
    });

    return this.getAll().filter(def => !hasFinalStep || !def.isFinal);
  }
}

export const StepRegistry = new StepRegistryClass();
