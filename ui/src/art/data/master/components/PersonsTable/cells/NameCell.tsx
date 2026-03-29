import { useState, useEffect } from 'react';
import { EditableText } from '@/shared/components';
import { useNotifications } from '@/shared/hooks';
import { savePerson, type PersonDto, type PersonSaveRequest } from '@/art/data/master/api/art-data-persons';
import rowStyles from '../../PersonsTableRow/PersonsTableRow.module.css';

interface NameCellProps {
    person: PersonDto;
    onSaved: () => void;
    readOnly?: boolean;
}

export const NameCell = ({ person, onSaved, readOnly = false }: NameCellProps) => {
    const { showNotification } = useNotifications();
    const [editedName, setEditedName] = useState(person.name);
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        setEditedName(person.name);
        setIsDirty(false);
    }, [person.name]);

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
        setIsDirty(newName !== person.name);
    };

    const handleSave = async (e: React.MouseEvent) => {
        e.stopPropagation();
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: PersonSaveRequest = { id: person.id, name: editedName.trim() };
            await savePerson(saveRequest);
            setIsDirty(false);
            onSaved();
        } catch (error: any) {
            showNotification('error', error?.response?.data?.message || error?.message || 'Failed to save person');
            setEditedName(person.name);
            setIsDirty(false);
        } finally {
            setIsSaving(false);
        }
    };

    if (readOnly) {
        return <span>{person.name}</span>;
    }

    return (
        <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }} onClick={(e) => e.stopPropagation()}>
            <EditableText
                value={editedName}
                onChange={handleNameChange}
                placeholder="Person name"
                disabled={isSaving}
            />
            {isDirty && (
                <button
                    onClick={handleSave}
                    disabled={isSaving || !editedName.trim()}
                    className={rowStyles.saveButton}
                >
                    {isSaving ? '...' : 'Save'}
                </button>
            )}
        </span>
    );
};
