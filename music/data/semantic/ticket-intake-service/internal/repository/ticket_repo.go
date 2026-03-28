package repository

import (
	"context"
	"fmt"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type TicketRepo struct {
	pool *pgxpool.Pool
}

func NewTicketRepo(pool *pgxpool.Pool) *TicketRepo {
	return &TicketRepo{pool: pool}
}

type SavedTicket struct {
	ID string
}

func (r *TicketRepo) SaveTicket(
	ctx context.Context,
	dataSource int,
	subjectType int,
	subjectID *int64,
	subjectName string,
	textSamplesJSON string,
	expectedProposalTypes []int,
	expectedEntityTypes []int,
	analysisMode int,
) (*SavedTicket, error) {
	var id string
	err := r.pool.QueryRow(ctx, `
		INSERT INTO mu_semantic_analysis.analysis_ticket
			(data_source, subject_type, subject_id, subject_name, text_samples, expected_proposal_types, expected_entity_types, analysis_mode)
		VALUES ($1, $2, $3, $4, $5::jsonb, $6, $7, $8)
		RETURNING id
	`, dataSource, subjectType, subjectID, subjectName, textSamplesJSON, expectedProposalTypes, expectedEntityTypes, analysisMode).Scan(&id)

	if err != nil {
		return nil, fmt.Errorf("failed to insert ticket: %w", err)
	}

	return &SavedTicket{ID: id}, nil
}

type DuplicateCheck struct {
	Exists   bool
	TicketID string
}

func (r *TicketRepo) CheckDuplicate(
	ctx context.Context,
	dataSource int,
	subjectType int,
	subjectID *int64,
	analysisMode int,
	contentHash string,
) (*DuplicateCheck, error) {
	var ticketID string
	err := r.pool.QueryRow(ctx, `
		SELECT id FROM mu_semantic_analysis.analysis_ticket
		WHERE data_source = $1
		  AND subject_type = $2
		  AND ($3::bigint IS NULL AND subject_id IS NULL OR subject_id = $3)
		  AND analysis_mode = $4
		  AND status IN (1, 2)
		  AND md5(text_samples::text) = $5
		LIMIT 1
	`, dataSource, subjectType, subjectID, analysisMode, contentHash).Scan(&ticketID)

	if err == pgx.ErrNoRows {
		return &DuplicateCheck{Exists: false}, nil
	}
	if err != nil {
		return nil, fmt.Errorf("failed to check duplicate: %w", err)
	}

	return &DuplicateCheck{Exists: true, TicketID: ticketID}, nil
}
