import type { ReactNode } from 'react';
import type { AccessLevel } from '@/shared/types/access';

interface AccessGateProps {
    level: AccessLevel;
    children: ReactNode;
    readOnlyFallback?: ReactNode;
}

export const AccessGate = ({ level, children, readOnlyFallback = null }: AccessGateProps) => {
    switch (level) {
        case 'full':
            return <>{children}</>;
        case 'readOnly':
            return <>{readOnlyFallback}</>;
        case 'hidden':
            return null;
    }
};
