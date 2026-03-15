import { useMemo, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { type PropertyResponse, updateProperty } from '../../api/config-api';
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

/**
 * Follow single non-leaf children until we reach a node with multiple children
 * or a node whose only child is a leaf. Returns the skipped intermediates and the landing node.
 */
function autoExpand(node: TreeNode): { skipped: TreeNode[]; landing: TreeNode } {
    const skipped: TreeNode[] = [];
    let current = node;
    while (current.children.size === 1) {
        const [child] = [...current.children.values()];
        if (child.property !== undefined) break; // single leaf child — stop, show it
        skipped.push(child);
        current = child;
    }
    return { skipped, landing: current };
}

// ── Column model ──────────────────────────────────────────────────────────────

interface ColumnData {
    /** Slash-separated label for intermediate auto-expanded nodes above this column */
    header: string;
    items: TreeNode[];
}

function computeColumns(root: TreeNode, selectionPath: string[]): ColumnData[] {
    const columns: ColumnData[] = [];

    // Initial column: auto-expand from root
    const { skipped: rootSkipped, landing: rootLanding } = autoExpand(root);
    const rootItems = [...rootLanding.children.values()];
    if (rootItems.length === 0) return columns;

    columns.push({
        header: rootSkipped.map(n => n.segment).join(' › '),
        items: rootItems,
    });

    // Each selected item spawns the next column
    for (let i = 0; i < selectionPath.length; i++) {
        const selectedFullPath = selectionPath[i];
        const selectedNode = columns[i].items.find(n => n.fullPath === selectedFullPath);
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

// ── Property editor ───────────────────────────────────────────────────────────

interface PropertyEditorProps {
    property: PropertyResponse;
    onSaved: (updated: PropertyResponse) => void;
}

function PropertyEditor({ property, onSaved }: PropertyEditorProps) {
    const queryClient = useQueryClient();
    const [value, setValue] = useState(property.currentValue);
    const [error, setError] = useState<string | null>(null);

    const mutation = useMutation({
        mutationFn: () => updateProperty(property.key, value),
        onSuccess: (updated) => {
            queryClient.invalidateQueries({ queryKey: ['config-properties'] });
            onSaved(updated);
            setError(null);
        },
        onError: (err: unknown) => {
            const msg = err instanceof Error ? err.message : 'Failed to update';
            setError(msg);
        },
    });

    const isDirty = value !== property.currentValue;

    function renderInput() {
        if (property.propertyType === 'BOOLEAN') {
            return (
                <select
                    className={styles.input}
                    value={value}
                    onChange={e => setValue(e.target.value)}
                >
                    <option value="true">true</option>
                    <option value="false">false</option>
                </select>
            );
        }
        return (
            <input
                type={property.propertyType === 'INTEGER' || property.propertyType === 'DECIMAL' ? 'number' : 'text'}
                className={styles.input}
                value={value}
                onChange={e => setValue(e.target.value)}
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
                disabled={!isDirty || mutation.isPending}
                onClick={() => mutation.mutate()}
            >
                {mutation.isPending ? 'Saving…' : 'Save'}
            </button>
        </div>
    );
}

// ── Main component ────────────────────────────────────────────────────────────

interface PropertiesTreeProps {
    properties: PropertyResponse[];
}

export function PropertiesTree({ properties }: PropertiesTreeProps) {
    const root = useMemo(() => buildTree(properties), [properties]);
    const [selectionPath, setSelectionPath] = useState<string[]>([]);
    const [selectedProperty, setSelectedProperty] = useState<PropertyResponse | null>(null);

    const columns = useMemo(() => computeColumns(root, selectionPath), [root, selectionPath]);

    function handleItemClick(columnIndex: number, item: TreeNode) {
        // Truncate any deeper selections and set this as the selection at this column level
        const newPath = [...selectionPath.slice(0, columnIndex), item.fullPath];
        setSelectionPath(newPath);
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
                                return (
                                    <button
                                        key={item.fullPath}
                                        className={`${styles.item} ${isSelected ? styles.itemSelected : ''} ${isLeaf ? styles.itemLeaf : styles.itemBranch}`}
                                        onClick={() => handleItemClick(colIdx, item)}
                                    >
                                        <span className={styles.itemLabel}>{item.segment}</span>
                                        {isLeaf ? (
                                            <span className={`${styles.typeBadge} ${styles[`type${item.property!.propertyType}`]}`}>
                                                {item.property!.propertyType}
                                            </span>
                                        ) : (
                                            <span className={styles.itemArrow}>›</span>
                                        )}
                                    </button>
                                );
                            })}
                        </div>
                    </div>
                ))}
            </div>

            {selectedProperty && (
                <PropertyEditor
                    key={selectedProperty.key}
                    property={selectedProperty}
                    onSaved={(updated) => setSelectedProperty(updated)}
                />
            )}
        </div>
    );
}
