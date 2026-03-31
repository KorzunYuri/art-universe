package model

import (
	"time"
)

type TextSample struct {
	Content string `json:"content" binding:"required"`
	Comment string `json:"comment"`
}

type Subject struct {
	EntityType string `json:"entity_type" binding:"required"`
	EntityID   *int64 `json:"entity_id"`
	Name       string `json:"name" binding:"required"`
}

type TicketRequest struct {
	DataSource            string       `json:"data_source" binding:"required"`
	AnalysisMode          string       `json:"analysis_mode" binding:"required"`
	Subject               Subject      `json:"subject" binding:"required"`
	TextSamples           []TextSample `json:"text_samples" binding:"required,min=1"`
	ExpectedProposalTypes []int        `json:"expected_proposal_types"`
	ExpectedEntityTypes   []int        `json:"expected_entity_types"`
}

type AnalysisTicket struct {
	ID                    string     `json:"id"`
	DataSource            int        `json:"data_source"`
	SubjectType           int        `json:"subject_type"`
	SubjectID             *int64     `json:"subject_id"`
	SubjectName           string     `json:"subject_name"`
	TextSamples           string     `json:"text_samples"`
	ExpectedProposalTypes []int      `json:"expected_proposal_types"`
	ExpectedEntityTypes   []int      `json:"expected_entity_types"`
	AnalysisMode          int        `json:"analysis_mode"`
	Status                int        `json:"status"`
	CreatedAt             time.Time  `json:"created_at"`
}

// Entity type code mapping
var EntityTypeCodes = map[string]int{
	"artist":   1,
	"album":    2,
	"track":    3,
	"category": 4,
	"person":   101,
}

// Analysis mode code mapping
var AnalysisModeCodes = map[string]int{
	"full_extraction":          1,
	"creative_categorization":  2,
}

// Data source code mapping
var DataSourceCodes = map[string]int{
	"lastfm":      1,
	"spotify":     2,
	"musicbrainz": 3,
	"master":      4,
}
