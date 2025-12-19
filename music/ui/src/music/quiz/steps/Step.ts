import type { PipelineStepDto, PipelineStepType, StepConfig, StepPosition } from '../types/pipeline-steps';

/**
 * Interface representing a pipeline step with all its associated metadata and behavior.
 * Each step type should implement this interface to provide step-specific logic.
 */
export interface Step {
  /**
   * Returns the unique type identifier for this step
   */
  getType(): PipelineStepType;

  /**
   * Returns the human-readable label for this step
   */
  getLabel(): string;

  /**
   * Returns a detailed description of what this step does
   */
  getDescription(): string;

  /**
   * Returns the position category (INITIAL or TRANSFORM) for this step
   */
  getPosition(): StepPosition;

  /**
   * Returns whether this step requires user configuration
   */
  isConfigurable(): boolean;

  /**
   * Returns the default configuration for this step when it's created
   */
  getDefaultConfig(): StepConfig;

  /**
   * Returns a human-readable summary of the step's current configuration
   */
  getConfigSummary(step: PipelineStepDto): string;

  /**
   * Returns a shortened summary of the step's configuration for limited space
   */
  getConfigShortSummary(step: PipelineStepDto): string;
}

/**
 * Abstract base class providing default implementations for Step interface.
 * Concrete step classes should extend this class and implement abstract methods.
 */
export abstract class BaseStep implements Step {
  abstract getType(): PipelineStepType;
  abstract getLabel(): string;
  abstract getDescription(): string;
  abstract getPosition(): StepPosition;
  abstract isConfigurable(): boolean;
  abstract getDefaultConfig(): StepConfig;

  /**
   * Default implementation returns the step label.
   * Override this method to provide a config-specific summary.
   */
  getConfigSummary(step: PipelineStepDto): string {
    return this.getLabel();
  }

  /**
   * Default implementation returns the step label.
   * Override this method to provide a config-specific short summary.
   */
  getConfigShortSummary(step: PipelineStepDto): string {
    return this.getLabel();
  }
}
