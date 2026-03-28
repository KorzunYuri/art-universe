package service

import (
	"context"
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"strings"

	"yurykorzun/art-universe/ticket-intake-service/internal/model"
	"yurykorzun/art-universe/ticket-intake-service/internal/repository"
)

type TicketService struct {
	repo *repository.TicketRepo
}

func NewTicketService(repo *repository.TicketRepo) *TicketService {
	return &TicketService{repo: repo}
}

type TicketResult struct {
	Accepted bool
	TicketID string
	// Rejection info
	Reason           string
	ExistingTicketID string
	Error            string
}

func (s *TicketService) ProcessTicket(ctx context.Context, req model.TicketRequest) *TicketResult {
	// Validate and resolve codes
	dataSourceCode, ok := model.DataSourceCodes[strings.ToLower(req.DataSource)]
	if !ok {
		return &TicketResult{Accepted: false, Reason: "validation_error", Error: fmt.Sprintf("unknown data_source: %s", req.DataSource)}
	}

	subjectTypeCode, ok := model.EntityTypeCodes[strings.ToLower(req.Subject.EntityType)]
	if !ok {
		return &TicketResult{Accepted: false, Reason: "validation_error", Error: fmt.Sprintf("unknown entity_type: %s", req.Subject.EntityType)}
	}

	// Serialize text samples
	textSamplesJSON, err := json.Marshal(req.TextSamples)
	if err != nil {
		return &TicketResult{Accepted: false, Reason: "validation_error", Error: "failed to serialize text_samples"}
	}

	// Check for duplicates
	contentHash := computeHash(string(textSamplesJSON))
	dup, err := s.repo.CheckDuplicate(ctx, dataSourceCode, subjectTypeCode, req.Subject.EntityID, contentHash)
	if err != nil {
		return &TicketResult{Accepted: false, Reason: "internal_error", Error: "failed to check duplicates"}
	}
	if dup.Exists {
		return &TicketResult{Accepted: false, Reason: "duplicate", ExistingTicketID: dup.TicketID}
	}

	// Save ticket
	saved, err := s.repo.SaveTicket(
		ctx,
		dataSourceCode,
		subjectTypeCode,
		req.Subject.EntityID,
		req.Subject.Name,
		string(textSamplesJSON),
		req.ExpectedProposalTypes,
		req.ExpectedEntityTypes,
	)
	if err != nil {
		return &TicketResult{Accepted: false, Reason: "internal_error", Error: "failed to save ticket"}
	}

	return &TicketResult{Accepted: true, TicketID: saved.ID}
}

func computeHash(content string) string {
	h := sha256.Sum256([]byte(content))
	return fmt.Sprintf("%x", h)
}
