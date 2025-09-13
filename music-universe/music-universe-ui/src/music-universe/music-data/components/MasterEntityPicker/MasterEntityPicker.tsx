import { EntityPicker } from '@/music-universe/shared/components/EntityPicker/EntityPicker';
import type { LookupEntity } from '@/music-universe/shared/types/lookup';
import type { MasterEntityType } from '@/music-universe/shared/types/entities';

export interface MasterEntityPickerProps {
    entityType: MasterEntityType;
    buttonLabel: string;
    onEntitySelected?: (entity: LookupEntity) => void;
    disabled?: boolean;
}

export const MasterEntityPicker = (props: MasterEntityPickerProps) => {
    return <EntityPicker dataSource="master" {...props} />;
};
