import { useLastfmEntity } from '@/music/data/raw/lastfm/hooks/useLastfmEntity';
import { QuizBinding } from '@/music/quiz/components';
import type { MusicQuizSupportedEntityType } from '@/music/quiz/types/music-quiz-entity';
import type { LastfmSupportedEntityType } from '@/music/data/raw/lastfm/types/lastfm-entity';

interface LastfmQuizCellProps {
    entityType: LastfmSupportedEntityType & MusicQuizSupportedEntityType;
    entityId: number;
}

export const LastfmQuizCell = ({ entityType, entityId }: LastfmQuizCellProps) => {
    const { entity } = useLastfmEntity(entityType, entityId);

    return (
        <span onClick={(e) => e.stopPropagation()}>
            <QuizBinding
                entityType={entityType}
                masterId={entity?.getMasterEntity()?.id ?? null}
            />
        </span>
    );
};
