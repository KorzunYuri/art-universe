import { useState } from 'react';
import styles from './Pagination.module.scss';
import commonStyles from '@/music/shared/styles/common.module.scss';

export interface PaginationProps {
    currentPage: number;
    totalPages: number;
    hasNextPage: boolean;
    hasPrevPage: boolean;
    onNextPage: () => void;
    onPrevPage: () => void;
    onGoToPage: (page: number) => void;
    disabled?: boolean;
    showGoToPage?: boolean;
    maxVisiblePages?: number;
}

export const Pagination = ({
    currentPage,
    totalPages,
    hasNextPage,
    hasPrevPage,
    onNextPage,
    onPrevPage,
    onGoToPage,
    disabled = false,
    showGoToPage = true,
    maxVisiblePages = 5
}: PaginationProps) => {
    const [goToPageInput, setGoToPageInput] = useState('');

    const handleGoToPage = () => {
        const pageNumber = parseInt(goToPageInput, 10);
        if (pageNumber >= 1 && pageNumber <= totalPages) {
            onGoToPage(pageNumber - 1); // Convert to 0-based index
            setGoToPageInput('');
        }
    };

    const handleGoToPageKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            handleGoToPage();
        }
    };

    // Generate visible page numbers
    const getVisiblePages = (): number[] => {
        if (totalPages <= maxVisiblePages) {
            return Array.from({ length: totalPages }, (_, i) => i);
        }

        const half = Math.floor(maxVisiblePages / 2);
        let start = Math.max(0, currentPage - half);
        const end = Math.min(totalPages - 1, start + maxVisiblePages - 1);

        // Adjust start if we're near the end
        if (end - start < maxVisiblePages - 1) {
            start = Math.max(0, end - maxVisiblePages + 1);
        }

        return Array.from({ length: end - start + 1 }, (_, i) => start + i);
    };

    const visiblePages = getVisiblePages();

    if (totalPages <= 1) {
        return null;
    }

    return (
        <div className={styles.pagination}>
            {/* Previous button */}
            <button
                onClick={onPrevPage}
                disabled={!hasPrevPage || disabled}
                className={styles.paginationButton}
            >
                Previous
            </button>

            {/* First page if not visible */}
            {visiblePages[0] > 0 && (
                <>
                    <button
                        onClick={() => onGoToPage(0)}
                        disabled={disabled}
                        className={`${styles.pageButton} ${currentPage === 0 ? styles.active : ''}`}
                    >
                        1
                    </button>
                    {visiblePages[0] > 1 && <span className={styles.ellipsis}>...</span>}
                </>
            )}

            {/* Visible page numbers */}
            {visiblePages.map(pageIndex => (
                <button
                    key={pageIndex}
                    onClick={() => onGoToPage(pageIndex)}
                    disabled={disabled}
                    className={`${styles.pageButton} ${currentPage === pageIndex ? styles.active : ''}`}
                >
                    {pageIndex + 1}
                </button>
            ))}

            {/* Last page if not visible */}
            {visiblePages[visiblePages.length - 1] < totalPages - 1 && (
                <>
                    {visiblePages[visiblePages.length - 1] < totalPages - 2 && (
                        <span className={styles.ellipsis}>...</span>
                    )}
                    <button
                        onClick={() => onGoToPage(totalPages - 1)}
                        disabled={disabled}
                        className={`${styles.pageButton} ${currentPage === totalPages - 1 ? styles.active : ''}`}
                    >
                        {totalPages}
                    </button>
                </>
            )}

            {/* Next button */}
            <button
                onClick={onNextPage}
                disabled={!hasNextPage || disabled}
                className={styles.paginationButton}
            >
                Next
            </button>

            {/* Go to page input */}
            {showGoToPage && totalPages > maxVisiblePages && (
                <div className={styles.goToPage}>
                    <span className={styles.pageInfo}>
                        Page {currentPage + 1} of {totalPages}
                    </span>
                    <input
                        type="number"
                        min="1"
                        max={totalPages}
                        value={goToPageInput}
                        onChange={(e) => setGoToPageInput(e.target.value)}
                        onKeyDown={handleGoToPageKeyDown}
                        placeholder="Go to..."
                        className={`${commonStyles.muInput} ${styles.goToPageInput}`}
                        disabled={disabled}
                    />
                    <button
                        onClick={handleGoToPage}
                        disabled={disabled || !goToPageInput}
                        className={styles.goToPageButton}
                    >
                        Go
                    </button>
                </div>
            )}
        </div>
    );
};
