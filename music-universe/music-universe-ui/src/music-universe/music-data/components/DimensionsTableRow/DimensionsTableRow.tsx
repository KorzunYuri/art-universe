// hooks
import { useState, useEffect } from "react";
// components
import { EditableText } from "@/music-universe/shared/components";
// types
import type { Dimension, DimensionSaveRequest } from "@/music-universe/music-data/api/music-data-dimensions";
// api
import { saveDimension } from "@/music-universe/music-data/api/music-data-dimensions";
// styles
import sharedStyles from "@/music-universe/shared/components/EntityTable/EntityTableStyles.module.scss";
import styles from "./DimensionsTableRow.module.css";

interface DimensionsTableRowProps {
    dimension: Dimension;
    onChange: (dimension: Dimension) => void;
}

export const DimensionsTableRow = ({ dimension, onChange }: DimensionsTableRowProps) => {
    const [editedName, setEditedName] = useState(dimension.name);
    const [isDirty, setIsDirty] = useState(false);
    const [isSaving, setIsSaving] = useState(false);

    // Sync with external dimension changes
    useEffect(() => {
        setEditedName(dimension.name);
        setIsDirty(false);
    }, [dimension.name]);

    const handleSave = async () => {
        console.log('💾 DimensionsTableRow handleSave called:', { isDirty, editedName: editedName.trim() });
        if (!isDirty || !editedName.trim()) return;

        setIsSaving(true);
        try {
            const saveRequest: DimensionSaveRequest = {
                id: dimension.id,
                name: editedName.trim()
            };

            const savedDimension = await saveDimension(saveRequest);
            if (savedDimension) {
                console.log('✅ Dimension saved successfully:', savedDimension.id);
                onChange(savedDimension);
            } else {
                console.error('❌ Failed to save dimension');
                // Reset to original value on failure
                setEditedName(dimension.name);
            }
        } catch (error) {
            console.error('❌ Error saving dimension:', error);
            // Reset to original value on error
            setEditedName(dimension.name);
        } finally {
            setIsSaving(false);
        }
    };

    const handleNameChange = (newName: string) => {
        setEditedName(newName);
    };

    const handleDirtyChange = (newIsDirty: boolean) => {
        setIsDirty(newIsDirty);
    };

    return (
        <div className={sharedStyles.row}>
            <div className={`${sharedStyles.cell} ${styles.name}`}>
                <EditableText
                    value={dimension.name}
                    onChange={handleNameChange}
                    onDirtyChange={handleDirtyChange}
                    placeholder="Dimension name"
                    disabled={isSaving}
                />
            </div>
            
            <div className={`${sharedStyles.cell} ${styles.actions}`}>
                <button
                    onClick={handleSave}
                    disabled={!isDirty || isSaving || !editedName.trim()}
                    className={styles.saveButton}
                >
                    {isSaving ? "..." : "Save"}
                </button>
            </div>
        </div>
    );
};
