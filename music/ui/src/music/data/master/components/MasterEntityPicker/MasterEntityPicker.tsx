import { EntityPicker } from '@/music/shared/components/EntityPicker/EntityPicker.tsx';
import type { LookupEntity } from '@/music/shared/types/lookup.ts';
import type { MasterEntityType } from '@/music/shared/types/entities.ts';

export interface MasterEntityPickerProps {
    entityType: MasterEntityType;
    buttonLabel: string;
    onEntitySelected?: (entity: LookupEntity) => void;
    disabled?: boolean;
}

export const MasterEntityPicker = (props: MasterEntityPickerProps) => {
    return <EntityPicker dataSource="master" {...props} />;
};
