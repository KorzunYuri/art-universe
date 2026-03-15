import { useMemo, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { type PropertyResponse, updateProperty } from '../../api/config-api';
import { useNotifications } from '@/shared/hooks/useNotifications';
import styles from './PropertiesTree.module.css';

// ── Tree model ────────────────────────────────────────────────────────────────

interface TreeNode {
    segment: string;
    fullPath: string;
    children: Map<string, TreeNode>;
    property?: PropertyResponse;
}

function buildTree(properties: PropertyResponse[]): TreeNode {
    const root: TreeNode = { segment: '', fullPath: '', children: new Map() };
    for (const prop of properties) {
        const segments = prop.key.split('.');
        let node = root;
        for (let i = 0; i < segments.length; i++) {
            const seg = segments[i];
            const path = segments.slice(0, i + 1).join('.');
            if (!node.children.has(seg)) {
                node.children.set(seg, { segment: seg, fullPath: path, children: new Map() });
            }
            node = node.children.get(seg)!;
        }
        node.property = prop;
    }
    return root;
}

function autoExpand(node: TreeNode): { skipped: TreeNode[]; landing: TreeNode } {
    const skipped: TreeNode[] = [];
    let current = node;
    while (current.children.size === 1) {
        const [child] = [...current.children.values()];
        if (child.property !== undefined) break;
        skipped.push(child);
        current = child;
    }
    return { skipped, landing: current };
}

// ── Column model ──────────────────────────────────────────────────────────────

interface ColumnData {
    header: string;
    items: TreeNode[];
}

function computeColumns(root: TreeNode, selectionPath: string[]): ColumnData[] {
    const columns: ColumnData[] = [];

    const { skipped: rootSkipped, landing: rootLanding } = autoExpand(root);
    const rootItems = [...rootLanding.children.values()];
    if (rootItems.length === 0) return columns;

    columns.push({
        header: rootSkipped.map(n => n.segment).join(' › '),
        items: rootItems,
    });

    for (let i = 0; i < selectionPath.length; i++) {
        const selectedNode = columns[i].items.find(n => n.fullPath === selectionPath[i]);
        if (!selectedNode || selectedNode.children.size === 0) break;

        const { skipped, landing } = autoExpand(selectedNode);
        const items = [...landing.children.values()];
        if (items.length === 0) break;

        columns.push({
            header: [selectedNode.segment, ...skipped.map(n => n.segment)].join(' › '),
            items,
        });
    }

    return columns;
}

// ── Inline value control ──────────────────────────────────────────────────────

interface InlineControlProps {
    property: PropertyResponse;
    value: string;
    onChange: (v: string) => void;
    onSave: () => void;
    isSaving: boolean;
    isDirty: boolean;
}

function sliderMax(property: PropertyResponse): number {
    const a = parseFloat(property.currentValue) || 0;
    const b = parseFloat(property.defaultValue) || 0;
    return Math.max(a * 3, b * 3, 10);
}

function InlineControl({ property, value, onChange, onSave, isSaving, isDirty }: InlineControlProps) {
    function renderControl() {
        switch (property.propertyType) {
            case 'BOOLEAN':
                return (
                    <label className={styles.toggle}>
                        <input
                            type="checkbox"
                            checked={value === 'true'}
                            onChange={e => onChange(e.target.checked ? 'true' : 'false')}
                        />
                        <span className={styles.toggleTrack} />
                    </label>
                );
            case 'INTEGER':
                return (
                    <input
                        type="number"
                        step={1}
                        value={value}
                        onChange={e => onChange(e.target.value)}
                        className={styles.inlineInput}
                    />
                );
            case 'DECIMAL': {
                const max = sliderMax(property);
                const num = parseFloat(value) || 0;
                return (
                    <div className={styles.sliderWrapper}>
                        <input
                            type="range"
                            min={0}
                            max={max}
                            step={0.01}
                            value={num}
                            onChange={e => onChange(e.target.value)}
                            className={styles.slider}
                        />
                        <span className={styles.sliderValue}>{num.toFixed(2)}</span>
                    </div>
                );
            }
            case 'STRING':
                return (
                    <input
                        type="text"
                        value={value}
                        onChange={e => onChange(e.target.value)}
                        className={styles.inlineInput}
                    />
                );
        }
    }

    return (
        <div className={styles.inlineControls} onClick={e => e.stopPropagation()}>
            {renderControl()}
            <button
                className={styles.inlineSaveBtn}
                disabled={!isDirty || isSaving}
                onClick={onSave}
                title="Save"
            >
                {isSaving ? '…' : '✓'}
            </button>
        </div>
    );
}

// ── Property editor (detail panel) ───────────────────────────────────────────

interface PropertyEditorProps {
    property: PropertyResponse;
    value: string;
    onChange: (v: string) => void;
    onSave: () => void;
    isSaving: boolean;
    isDirty: boolean;
    error: string | null;
}

function PropertyEditor({ property, value, onChange, onSave, isSaving, isDirty, error }: PropertyEditorProps) {
    function renderInput() {
        if (property.propertyType === 'BOOLEAN') {
            return (
                <select className={styles.input} value={value} onChange={e => onChange(e.target.value)}>
                    <option value="true">true</option>
                    <option value="false">false</option>
                </select>
            );
        }
        return (
            <input
                type={property.propertyType === 'INTEGER' || property.propertyType === 'DECIMAL' ? 'number' : 'text'}
                step={property.propertyType === 'DECIMAL' ? 'any' : undefined}
                className={styles.input}
                value={value}
                onChange={e => onChange(e.target.value)}
            />
        );
    }

    return (
        <div className={styles.editor}>
            <div className={styles.editorKey}>{property.key}</div>
            <span className={`${styles.typeBadge} ${styles[`type${property.propertyType}`]}`}>
                {property.propertyType}
            </span>
            {property.description && (
                <p className={styles.editorDescription}>{property.description}</p>
            )}
            <div className={styles.editorField}>
                <label className={styles.editorLabel}>Current value</label>
                {renderInput()}
            </div>
            <div className={styles.editorField}>
                <label className={styles.editorLabel}>Default value</label>
                <span className={styles.editorDefault}>{property.defaultValue}</span>
            </div>
            {error && <div className={styles.editorError}>{error}</div>}
            <button
                className={styles.saveButton}
                disabled={!isDirty || isSaving}
                onClick={onSave}
            >
                {isSaving ? 'Saving…' : 'Save'}
            </button>
        </div>
    );
}

// ── Main component ────────────────────────────────────────────────────────────

interface PropertiesTreeProps {
    properties: PropertyResponse[];
}

export function PropertiesTree({ properties }: PropertiesTreeProps) {
    const root = useMemo(() => buildTree([...properties].sort((a, b) => a.key.localeCompare(b.key))), [properties]);
    const [selectionPath, setSelectionPath] = useState<string[]>([]);
    const [selectedProperty, setSelectedProperty] = useState<PropertyResponse | null>(null);

    // Single source of truth for all in-flight edits, keyed by property key.
    // Falls back to property.currentValue when absent.
    const [editValues, setEditValues] = useState<Record<string, string>>({});
    const [saveErrors, setSaveErrors] = useState<Record<string, string>>({});

    const queryClient = useQueryClient();
    const { showNotification } = useNotifications();
    const mutation = useMutation({
        mutationFn: ({ key, value }: { key: string; value: string }) => updateProperty(key, value),
        onSuccess: (updated) => {
            queryClient.invalidateQueries({ queryKey: ['config-properties'] });
            setEditValues(prev => { const n = { ...prev }; delete n[updated.key]; return n; });
            setSaveErrors(prev => { const n = { ...prev }; delete n[updated.key]; return n; });
            if (selectedProperty?.key === updated.key) setSelectedProperty(updated);
            showNotification('success', `${updated.key} saved`);
        },
        onError: (err: unknown, { key }) => {
            const msg = err instanceof Error ? err.message : 'Failed to update';
            setSaveErrors(prev => ({ ...prev, [key]: msg }));
            showNotification('error', `Failed to save ${key}: ${msg}`);
        },
    });

    const columns = useMemo(() => computeColumns(root, selectionPath), [root, selectionPath]);

    function editValue(prop: PropertyResponse) {
        return editValues[prop.key] ?? prop.currentValue;
    }

    function handleChange(key: string, value: string) {
        setEditValues(prev => ({ ...prev, [key]: value }));
    }

    function handleSave(key: string, value: string) {
        mutation.mutate({ key, value });
    }

    function isSavingProp(key: string) {
        return mutation.isPending && mutation.variables?.key === key;
    }

    function handleItemClick(columnIndex: number, item: TreeNode) {
        setSelectionPath([...selectionPath.slice(0, columnIndex), item.fullPath]);
        setSelectedProperty(item.property ?? null);
    }

    if (properties.length === 0) {
        return <div className={styles.empty}>No properties registered.</div>;
    }

    return (
        <div className={styles.root}>
            <div className={styles.columnsArea}>
                {columns.map((column, colIdx) => (
                    <div key={colIdx} className={styles.column}>
                        <div className={styles.columnHeader}>{column.header || 'root'}</div>
                        <div className={styles.columnItems}>
                            {column.items.map(item => {
                                const isSelected = selectionPath[colIdx] === item.fullPath;
                                const isLeaf = !!item.property;
                                const prop = item.property;
                                const val = prop ? editValue(prop) : '';
                                const dirty = prop ? val !== prop.currentValue : false;
                                const saving = prop ? isSavingProp(prop.key) : false;

                                return (
                                    <div
                                        key={item.fullPath}
                                        className={`${styles.item} ${isSelected ? styles.itemSelected : ''}`}
                                    >
                                        <button
                                            className={styles.itemSelectArea}
                                            onClick={() => handleItemClick(colIdx, item)}
                                        >
                                            <span className={styles.itemLabel}>{item.segment}</span>
                                            {isLeaf ? (
                                                <span className={`${styles.typeBadge} ${styles[`type${prop!.propertyType}`]}`}>
                                                    {prop!.propertyType}
                                                </span>
                                            ) : (
                                                <span className={styles.itemArrow}>›</span>
                                            )}
                                        </button>
                                        {isLeaf && prop && (
                                            <InlineControl
                                                property={prop}
                                                value={val}
                                                onChange={v => handleChange(prop.key, v)}
                                                onSave={() => handleSave(prop.key, val)}
                                                isSaving={saving}
                                                isDirty={dirty}
                                            />
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                ))}
            </div>

            {selectedProperty && (
                <PropertyEditor
                    property={selectedProperty}
                    value={editValue(selectedProperty)}
                    onChange={v => handleChange(selectedProperty.key, v)}
                    onSave={() => handleSave(selectedProperty.key, editValue(selectedProperty))}
                    isSaving={isSavingProp(selectedProperty.key)}
                    isDirty={editValue(selectedProperty) !== selectedProperty.currentValue}
                    error={saveErrors[selectedProperty.key] ?? null}
                />
            )}
        </div>
    );
}
