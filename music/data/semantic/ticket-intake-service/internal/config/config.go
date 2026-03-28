package config

import (
	"fmt"
	"os"
)

type Config struct {
	DBHost     string
	DBPort     string
	DBName     string
	DBUser     string
	DBPassword string
	ServerPort string
}

func Load() *Config {
	return &Config{
		DBHost:     requireEnv("AU_DB_MASTER_HOST"),
		DBPort:     getEnv("AU_DB_MASTER_PORT", "5432"),
		DBName:     requireEnv("AU_DB_NAME"),
		DBUser:     requireEnv("MU_SA_DB_USERNAME"),
		DBPassword: requireEnv("MU_SA_DB_PASSWORD_DM"),
		ServerPort: getEnv("TICKET_SERVICE_PORT", "8080"),
	}
}

func (c *Config) DatabaseURL() string {
	return fmt.Sprintf("postgres://%s:%s@%s:%s/%s?search_path=mu_semantic_analysis",
		c.DBUser, c.DBPassword, c.DBHost, c.DBPort, c.DBName)
}

func requireEnv(key string) string {
	value := os.Getenv(key)
	if value == "" {
		panic(fmt.Sprintf("required environment variable %s is not set", key))
	}
	return value
}

func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}
