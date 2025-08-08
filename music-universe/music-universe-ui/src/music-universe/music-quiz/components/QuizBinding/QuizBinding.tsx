import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useQuizBinding, quizBindingKeys } from '@/music-universe/music-quiz/hooks/useQuizBinding';
import {
    bindMasterEntityToQuiz,
    type QuizBindingDto,
    unbindMasterEntityFromQuiz
} from '@/music-universe/music-quiz/api/music-quiz-common-binding';
import type { MusicQuizSupportedEntityType } from '@/music-universe/music-quiz/types/music-quiz-entity';
import styles from './QuizBinding.module.scss';

interface QuizBindingProps {
    entityType: MusicQuizSupportedEntityType;
    masterId: number | null;
    onBeforeBind?: () => Promise<boolean>;
    onAfterBind?: (result: QuizBindingDto) => void;
    onBeforeUnbind?: () => Promise<boolean>;
    onAfterUnbind?: (result: QuizBindingDto) => void;
    disabled?: boolean;
}

interface QuizBindingFlow {
    onBefore: () => Promise<boolean>;
    action: (entityType: MusicQuizSupportedEntityType, masterId: number) => Promise<QuizBindingDto>;
    onAfter: (result: QuizBindingDto) => void;
}

export const QuizBinding = ({
    entityType,
    masterId,
    onBeforeBind = async () => true,
    onAfterBind = () => {},
    onBeforeUnbind = async () => true,
    onAfterUnbind = () => {},
    disabled = false
}: QuizBindingProps) => {

    const queryClient = useQueryClient();
    const [isProcessing, setIsProcessing] = useState(false);
    
    const { isBound, isLoading } = useQuizBinding(entityType, masterId);

    // If no master entity, component is disabled
    const isDisabled = disabled || isLoading || isProcessing || !masterId;

    const handleToggle = async () => {
        if (isDisabled) return;

        setIsProcessing(true);
        const queryKey = quizBindingKeys.detail(entityType, masterId);
        try {
            const flow: QuizBindingFlow = isBound
                ? {
                    onBefore:   onBeforeUnbind,
                    action:     unbindMasterEntityFromQuiz,
                    onAfter:    onAfterUnbind
                }
                : {
                    onBefore:   onBeforeBind,
                    action:     bindMasterEntityToQuiz,
                    onAfter:    onAfterBind
                };

            const canProceed = await flow.onBefore();
            if (!canProceed) return;

            const result = await flow.action(entityType, masterId);

            // Update cache
            queryClient.setQueryData(queryKey, result);

            flow.onAfter(result);
        } catch (error) {
            console.error('Failed to toggle quiz binding:', error);
        } finally {
            setIsProcessing(false);
        }
    };

    return (
        <div className={styles.toggleContainer}>
            <button
                type="button"
                className={`${styles.button} ${isBound ? styles.bound : styles.unbound} ${
                    isBound ? styles.active : ''
                }`}
                disabled={isDisabled}
                onClick={handleToggle}
                title={!masterId ? 'Entity must be bound to master first' : undefined}
            >
                {isProcessing ? '...' : 'quiz'}
            </button>
        </div>
    );
};
