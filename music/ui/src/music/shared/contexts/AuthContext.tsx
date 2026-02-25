import { createContext } from 'react';
import type { MasterEntityType } from '@/music/shared/types/entities';
import type { DataSource } from '@/music/data/raw/shared/types/data-sources';

export interface Permissions {
    isAdmin(): boolean;
    canEditMasterEntity(entityType: MasterEntityType): boolean;
    canDeleteMasterEntity(entityType: MasterEntityType): boolean;
    canCreateMasterEntity(entityType: MasterEntityType): boolean;
    canBindRawEntity(dataSource: DataSource, entityType: MasterEntityType): boolean;
    canEditRawEntityApproval(dataSource: DataSource, entityType: MasterEntityType): boolean;
    canEditQuizBinding(entityType: MasterEntityType): boolean;
}

export const AuthContext = createContext<Permissions | null>(null);
