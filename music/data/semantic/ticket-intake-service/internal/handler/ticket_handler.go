package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"yurykorzun/art-universe/ticket-intake-service/internal/model"
	"yurykorzun/art-universe/ticket-intake-service/internal/service"
)

type TicketHandler struct {
	service *service.TicketService
}

func NewTicketHandler(service *service.TicketService) *TicketHandler {
	return &TicketHandler{service: service}
}

func (h *TicketHandler) CreateTicket(c *gin.Context) {
	var req model.TicketRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, model.SingleTicketResponse{
			Status: "rejected",
			Reason: err.Error(),
		})
		return
	}

	result := h.service.ProcessTicket(c.Request.Context(), req)

	if result.Accepted {
		c.JSON(http.StatusCreated, model.SingleTicketResponse{
			Status:   "accepted",
			TicketID: result.TicketID,
		})
	} else {
		status := http.StatusConflict
		if result.Reason == "validation_error" {
			status = http.StatusBadRequest
		} else if result.Reason == "internal_error" {
			status = http.StatusInternalServerError
		}
		c.JSON(status, model.SingleTicketResponse{
			Status:           "rejected",
			Reason:           result.Reason,
			ExistingTicketID: result.ExistingTicketID,
		})
	}
}

func (h *TicketHandler) CreateTicketBatch(c *gin.Context) {
	var requests []model.TicketRequest
	if err := c.ShouldBindJSON(&requests); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	response := model.BatchTicketResponse{
		Accepted: make([]model.AcceptedTicket, 0),
	}

	for i, req := range requests {
		result := h.service.ProcessTicket(c.Request.Context(), req)

		if result.Accepted {
			response.Accepted = append(response.Accepted, model.AcceptedTicket{
				Index:    i,
				TicketID: result.TicketID,
			})
		} else if result.Reason == "duplicate" {
			response.Rejected.Duplicate = append(response.Rejected.Duplicate, model.RejectedDuplicate{
				Index:            i,
				ExistingTicketID: result.ExistingTicketID,
				SubjectName:      req.Subject.Name,
			})
		} else {
			errorMsg := result.Error
			if errorMsg == "" {
				errorMsg = result.Reason
			}
			response.Rejected.ValidationError = append(response.Rejected.ValidationError, model.RejectedValidation{
				Index: i,
				Error: errorMsg,
			})
		}
	}

	c.JSON(http.StatusOK, response)
}
