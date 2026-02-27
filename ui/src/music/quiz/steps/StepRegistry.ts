import type { PipelineStepType } from '../types/pipeline-steps';
import type { Step } from './Step';
import { ApprovedFilterStep } from './ApprovedFilterStep';
import { ArtistDiversityStep } from './ArtistDiversityStep';
import { ArtistRecencyPenaltyStep } from './ArtistRecencyPenaltyStep';
import { BlacklistFilterStep } from './BlacklistFilterStep';
import { FinalCategoriesBalancerStep } from './FinalCategoriesBalancerStep';
import { FinalLimiterStep } from './FinalLimiterStep';
import { StartDatasourceStep } from './StartDatasourceStep';
import { TrackRecencyPenaltyStep } from './TrackRecencyPenaltyStep';
import { WhitelistFilterStep } from './WhitelistFilterStep';

/**
 * Registry for all pipeline step implementations.
 * Provides centralized access to step instances using the singleton pattern.
 */
class StepRegistry {
  private static instance: StepRegistry;
  private steps: Map<PipelineStepType, Step>;

  private constructor() {
    this.steps = new Map();
    this.registerAllSteps();
  }

  /**
   * Gets the singleton instance of the registry
   */
  static getInstance(): StepRegistry {
    if (!StepRegistry.instance) {
      StepRegistry.instance = new StepRegistry();
    }
    return StepRegistry.instance;
  }

  /**
   * Registers all step implementations
   */
  private registerAllSteps() {
    this.register(new ApprovedFilterStep());
    this.register(new ArtistDiversityStep());
    this.register(new ArtistRecencyPenaltyStep());
    this.register(new BlacklistFilterStep());
    this.register(new FinalCategoriesBalancerStep());
    this.register(new FinalLimiterStep());
    this.register(new StartDatasourceStep());
    this.register(new TrackRecencyPenaltyStep());
    this.register(new WhitelistFilterStep());
  }

  /**
   * Registers a step implementation
   */
  private register(step: Step) {
    this.steps.set(step.getType(), step);
  }

  /**
   * Gets a step implementation by type
   * @throws Error if step type is not registered
   */
  get(type: PipelineStepType): Step {
    const step = this.steps.get(type);
    if (!step) {
      throw new Error(`Step type ${type} not registered in StepRegistry`);
    }
    return step;
  }

  /**
   * Gets all registered step implementations
   */
  getAll(): Step[] {
    return Array.from(this.steps.values());
  }
}

// Export singleton instance
export const stepRegistry = StepRegistry.getInstance();
