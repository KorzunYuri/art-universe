package main

import (
	"context"
	"fmt"
	"log"

	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"

	"yurykorzun/art-universe/ticket-intake-service/internal/config"
	"yurykorzun/art-universe/ticket-intake-service/internal/handler"
	"yurykorzun/art-universe/ticket-intake-service/internal/repository"
	"yurykorzun/art-universe/ticket-intake-service/internal/service"
)

func main() {
	cfg := config.Load()

	// Connect to database
	pool, err := pgxpool.New(context.Background(), cfg.DatabaseURL())
	if err != nil {
		log.Fatalf("Unable to connect to database: %v", err)
	}
	defer pool.Close()

	// Verify connection
	if err := pool.Ping(context.Background()); err != nil {
		log.Fatalf("Unable to ping database: %v", err)
	}
	log.Println("Connected to database")

	// Setup dependencies
	repo := repository.NewTicketRepo(pool)
	svc := service.NewTicketService(repo)
	ticketHandler := handler.NewTicketHandler(svc)

	// Setup router
	r := gin.Default()

	api := r.Group("/api/v1")
	{
		api.POST("/tickets", ticketHandler.CreateTicket)
		api.POST("/tickets/batch", ticketHandler.CreateTicketBatch)
	}

	// Health check
	r.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{"status": "ok"})
	})

	addr := fmt.Sprintf(":%s", cfg.ServerPort)
	log.Printf("Starting ticket intake service on %s", addr)
	if err := r.Run(addr); err != nil {
		log.Fatalf("Failed to start server: %v", err)
	}
}
