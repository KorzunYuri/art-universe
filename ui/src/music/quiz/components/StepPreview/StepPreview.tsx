import { useState } from 'react';
import { useStepPreview } from '@/music/quiz/hooks/useQuizData.ts';
import styles from './StepPreview.module.scss';

interface StepPreviewProps {
  stepId: number;
  previewData?: string;
}

export const StepPreview = ({ stepId, previewData }: StepPreviewProps) => {
  const [showLivePreview, setShowLivePreview] = useState(false);
  const { data: livePreviewData, isLoading, error } = useStepPreview(stepId, showLivePreview);

  const handlePreviewClick = () => {
    setShowLivePreview(true);
  };

  const displayData = showLivePreview ? livePreviewData : previewData;
  const displayText = error ? 'Failed to load preview' : (displayData || '{}');

  return (
    <div className={styles.preview}>
      <div className={styles.header}>
        <div className={styles.label}>Preview:</div>
        <button 
          className={styles.previewButton}
          onClick={handlePreviewClick}
          disabled={isLoading}
        >
          {isLoading ? 'Loading...' : 'Refresh'}
        </button>
      </div>
      <pre className={styles.content}>
        {typeof displayText === 'string' ? displayText : JSON.stringify(displayText, null, 2)}
      </pre>
    </div>
  );
};
