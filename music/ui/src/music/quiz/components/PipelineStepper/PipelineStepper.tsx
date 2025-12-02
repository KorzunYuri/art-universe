import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from '@dnd-kit/core';
import {
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import type { ReactElement, ReactNode } from 'react';
import { type PipelineStepDto, STEP_LABELS, STEP_POSITIONS } from '@/music/quiz/types/pipeline-steps.ts';
import { getStepConfigSummary } from '@/music/quiz/utils/stepConfigSummary.ts';
import styles from './PipelineStepper.module.scss';

interface PipelineStepperProps {
  steps: PipelineStepDto[];
  selectedStepIndex: number;
  onStepSelect: (index: number) => void;
  onReorder?: (fromIndex: number, toIndex: number) => void;
  onAddStep?: (position: number) => void;
  onRemoveStep?: (index: number) => void;
  onExecuteStep?: (index: number) => void;
  addingStepAt?: number | null;
  stepSelectorRenderer?: (position: number) => ReactNode;
  readonly?: boolean;
}

interface ResultStats {
  inputRecords?: number;
  outputRecords?: number;
  inputArtists?: number;
  outputArtists?: number;
  filteredRecords?: number;
  filteredArtists?: number;
  executionTimeMs?: number;
}

interface SortableStepProps {
  step: PipelineStepDto;
  index: number;
  selectedStepIndex: number;
  onStepSelect: (index: number) => void;
  onRemove?: () => void;
  onExecute?: () => void;
  readonly: boolean;
  renderStats: (step: PipelineStepDto) => ReactElement;
  isDraggable: boolean;
}

const SortableStep = ({
  step,
  index,
  selectedStepIndex,
  onStepSelect,
  onRemove,
  onExecute,
  readonly,
  renderStats,
  isDraggable,
}: SortableStepProps) => {
  const handleRemove = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onRemove && window.confirm('Are you sure you want to remove this step?')) {
      onRemove();
    }
  };

  const handleExecute = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onExecute) {
      onExecute();
    }
  };
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: step.id || `temp-${index}`,
    disabled: !isDraggable || readonly,
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  const getStepStatus = (): 'active' | 'completed' | 'pending' | 'warning' => {
    if (index === selectedStepIndex) return 'active';

    const position = STEP_POSITIONS[step.type];
    if (position === 'INITIAL' && index !== 0) {
      return 'warning';
    }

    if (step.resultTableName || step.resultStats) {
      return 'completed';
    }

    return 'pending';
  };

  const status = getStepStatus();
  const isActive = index === selectedStepIndex;
  const stepLabel = STEP_LABELS[step.type] || step.type;
  const configSummary = getStepConfigSummary(step);

  return (
    <div ref={setNodeRef} style={style} className={styles.stepContainer}>
      <button
        className={`${styles.step} ${styles[status]} ${isActive ? styles.active : ''} ${isDraggable && !readonly ? styles.draggable : ''}`}
        onClick={() => onStepSelect(index)}
        type="button"
        {...attributes}
        {...listeners}
      >
        {status === 'warning' && (
          <div className={styles.warningIndicator}>⚠</div>
        )}
        {!readonly && onExecute && step.id && (
          <button
            className={styles.executeButton}
            onClick={handleExecute}
            type="button"
            title="Execute step"
          >
            ▶
          </button>
        )}
        {!readonly && onRemove && (
          <button
            className={styles.removeButton}
            onClick={handleRemove}
            type="button"
            title="Remove step"
          >
            ×
          </button>
        )}
        <div className={styles.stepContent}>
          {/* Name Section */}
          <div className={styles.stepSection}>
            <div className={styles.stepTitle}>{stepLabel}</div>
          </div>

          <div className={styles.divider} />

          {/* Description Section */}
          <div className={styles.stepSection}>
            <div className={styles.stepDescription}>{configSummary}</div>
          </div>

          <div className={styles.divider} />

          {/* Stats Section */}
          <div className={styles.stepSection}>
            {renderStats(step)}
          </div>
        </div>
      </button>
    </div>
  );
};

export const PipelineStepper = ({
  steps,
  selectedStepIndex,
  onStepSelect,
  onReorder,
  onAddStep,
  onRemoveStep,
  onExecuteStep,
  addingStepAt = null,
  stepSelectorRenderer,
  readonly = false
}: PipelineStepperProps) => {
  const sortedSteps = [...steps].sort((a, b) => a.ord - b.ord);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8, // Require 8px movement before activating drag
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const parseResultStats = (step: PipelineStepDto): ResultStats | null => {
    if (!step.resultStats) return null;

    try {
      return JSON.parse(step.resultStats) as ResultStats;
    } catch {
      return null;
    }
  };

  const renderStats = (step: PipelineStepDto) => {
    const stats = parseResultStats(step);

    if (!stats) {
      return <div className={styles.statsPlaceholder}>Not executed</div>;
    }

    const tracksDelta = (stats.outputRecords ?? 0) - (stats.inputRecords ?? 0);
    const artistsDelta = (stats.outputArtists ?? 0) - (stats.inputArtists ?? 0);

    const formatDelta = (delta: number) => {
      if (delta === 0) return '0';
      if (delta > 0) return `+${delta}`;
      return `${delta}`;
    };

    const getDeltaClass = (delta: number) => {
      if (delta === 0) return styles.neutral;
      return delta > 0 ? styles.positive : styles.negative;
    };

    return (
      <table className={styles.statsTable}>
        <thead>
          <tr>
            <th></th>
            <th>Delta</th>
            <th>Output</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Artists</td>
            <td className={getDeltaClass(artistsDelta)}>
              {formatDelta(artistsDelta)}
            </td>
            <td>{stats.outputArtists ?? 0}</td>
          </tr>
          <tr>
            <td>Tracks</td>
            <td className={getDeltaClass(tracksDelta)}>
              {formatDelta(tracksDelta)}
            </td>
            <td>{stats.outputRecords ?? 0}</td>
          </tr>
        </tbody>
      </table>
    );
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (!over || active.id === over.id || !onReorder) {
      return;
    }

    const oldIndex = sortedSteps.findIndex(step => (step.id || `temp-${sortedSteps.indexOf(step)}`) === active.id);
    const newIndex = sortedSteps.findIndex(step => (step.id || `temp-${sortedSteps.indexOf(step)}`) === over.id);

    // Prevent dragging INITIAL step away from position 0
    const draggedStep = sortedSteps[oldIndex];
    const position = STEP_POSITIONS[draggedStep.type];
    if (position === 'INITIAL' && newIndex !== 0) {
      return; // Don't allow drag
    }

    // Prevent dragging other steps to position 0 (reserved for INITIAL)
    if (newIndex === 0 && position !== 'INITIAL') {
      return; // Don't allow drag
    }

    if (oldIndex !== -1 && newIndex !== -1 && oldIndex !== newIndex) {
      onReorder(oldIndex, newIndex);
    }
  };

  const stepIds = sortedSteps.map((step, index) => step.id || `temp-${index}`);

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragEnd={handleDragEnd}
    >
      <div className={styles.stepper}>
        <SortableContext items={stepIds} strategy={verticalListSortingStrategy}>
          <div className={styles.stepsList}>
            {sortedSteps.map((step, index) => {
              const position = STEP_POSITIONS[step.type];
              const isDraggable = !(position === 'INITIAL' && index === 0);

              return (
                <>
                  {/* Add button, step selector, or connector before each step */}
                  {!readonly && onAddStep && addingStepAt === index && stepSelectorRenderer ? (
                    <div key={`selector-${index}`} className={styles.stepSelectorContainer}>
                      {stepSelectorRenderer(index)}
                    </div>
                  ) : !readonly && onAddStep ? (
                    <div key={`add-${index}`} className={styles.addButtonContainer}>
                      <button
                        className={styles.addButton}
                        onClick={() => onAddStep(index)}
                        title="Add step here"
                        type="button"
                      >
                        +
                      </button>
                    </div>
                  ) : readonly && index > 0 ? (
                    <div key={`connector-${index}`} className={styles.connectorContainer}>
                      <div className={styles.connector} />
                    </div>
                  ) : null}

                  {/* Step card */}
                  <SortableStep
                    key={step.id || `step-${index}`}
                    step={step}
                    index={index}
                    selectedStepIndex={selectedStepIndex}
                    onStepSelect={onStepSelect}
                    onRemove={onRemoveStep ? () => onRemoveStep(index) : undefined}
                    onExecute={onExecuteStep ? () => onExecuteStep(index) : undefined}
                    readonly={readonly}
                    renderStats={renderStats}
                    isDraggable={isDraggable}
                  />
                </>
              );
            })}

            {/* Add button or step selector after the last step */}
            {!readonly && onAddStep && sortedSteps.length > 0 && addingStepAt === sortedSteps.length && stepSelectorRenderer ? (
              <div className={styles.stepSelectorContainer}>
                {stepSelectorRenderer(sortedSteps.length)}
              </div>
            ) : !readonly && onAddStep && sortedSteps.length > 0 ? (
              <div className={styles.addButtonContainer}>
                <button
                  className={styles.addButton}
                  onClick={() => onAddStep(sortedSteps.length)}
                  title="Add step here"
                  type="button"
                >
                  +
                </button>
              </div>
            ) : null}
          </div>
        </SortableContext>
      </div>
    </DndContext>
  );
};
