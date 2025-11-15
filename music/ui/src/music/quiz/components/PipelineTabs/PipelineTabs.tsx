import styles from './PipelineTabs.module.scss';

export interface PipelineTab {
  id: string;
  label: string;
  type: 'game' | 'generation';
  pipelineId?: number;
  generationId?: number;
}

interface PipelineTabsProps {
  tabs: PipelineTab[];
  activeTabId: string;
  onTabSelect: (tabId: string) => void;
  onTabClose?: (tabId: string) => void;
}

export const PipelineTabs = ({
  tabs,
  activeTabId,
  onTabSelect,
  onTabClose,
}: PipelineTabsProps) => {
  return (
    <div className={styles.tabBar}>
      {tabs.map((tab) => {
        const isActive = tab.id === activeTabId;
        const canClose = tab.type === 'generation' && onTabClose;

        return (
          <div
            key={tab.id}
            className={`${styles.tab} ${isActive ? styles.active : ''}`}
          >
            <button
              className={styles.tabButton}
              onClick={() => onTabSelect(tab.id)}
              type="button"
            >
              <span className={styles.tabLabel}>{tab.label}</span>
              {tab.type === 'generation' && (
                <span className={styles.tabBadge}>Gen</span>
              )}
            </button>

            {canClose && (
              <button
                className={styles.closeButton}
                onClick={(e) => {
                  e.stopPropagation();
                  onTabClose(tab.id);
                }}
                title="Close tab"
                type="button"
              >
                ×
              </button>
            )}
          </div>
        );
      })}
    </div>
  );
};
