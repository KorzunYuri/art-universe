package model

type SingleTicketResponse struct {
	Status           string `json:"status"`
	TicketID         string `json:"ticket_id,omitempty"`
	Reason           string `json:"reason,omitempty"`
	ExistingTicketID string `json:"existing_ticket_id,omitempty"`
}

type AcceptedTicket struct {
	Index    int    `json:"index"`
	TicketID string `json:"ticket_id"`
}

type RejectedDuplicate struct {
	Index            int    `json:"index"`
	ExistingTicketID string `json:"existing_ticket_id"`
	SubjectName      string `json:"subject_name"`
}

type RejectedValidation struct {
	Index int    `json:"index"`
	Error string `json:"error"`
}

type BatchTicketResponse struct {
	Accepted []AcceptedTicket `json:"accepted"`
	Rejected struct {
		Duplicate       []RejectedDuplicate  `json:"duplicate,omitempty"`
		ValidationError []RejectedValidation `json:"validation_error,omitempty"`
	} `json:"rejected"`
}
