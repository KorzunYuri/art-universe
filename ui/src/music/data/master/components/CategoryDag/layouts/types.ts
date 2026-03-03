import { type Node, type Edge } from '@xyflow/react';

export interface CategoryNodeData extends Record<string, unknown> {
    id: number;
    name: string;
    isRoot: boolean;
    childrenCount: number;
    artistsCount: number;
    tracksCount: number;
    rank?: number;
    value?: number;
}

export type CategoryNode = Node<CategoryNodeData>;

export interface LayoutResult {
    nodes: CategoryNode[];
    edges: Edge[];
}

export interface LayoutEngine {
    name: string;
    layout: (nodes: CategoryNode[], edges: Edge[]) => LayoutResult;
}
