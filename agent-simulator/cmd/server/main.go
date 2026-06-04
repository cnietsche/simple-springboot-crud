package main

import (
	"log"
	"net/http"
	"os"
	"strconv"

	"github.com/cnietsche/agent-simulator/internal/agent"
	"github.com/cnietsche/agent-simulator/internal/backend"
	"github.com/cnietsche/agent-simulator/internal/httpapi"
	"github.com/cnietsche/agent-simulator/internal/store"
)

func main() {
	sqlitePath := envOr("SQLITE_PATH", "simulator.db")
	backendURL := envOr("BACKEND_BASE_URL", "http://localhost:8080")
	addr := envOr("HTTP_ADDR", ":8081")
	minDelay := envFloatOr("SIM_MIN_DELAY_SEC", 1)
	maxDelay := envFloatOr("SIM_MAX_DELAY_SEC", 120)

	st, err := store.Open(sqlitePath)
	if err != nil {
		log.Fatalf("open store: %v", err)
	}
	defer st.Close()

	bc := backend.NewClient(backendURL)
	cfg := agent.Config{MinDelaySec: minDelay, MaxDelaySec: maxDelay}
	reg := agent.NewRegistry(st, bc, cfg)
	if err := reg.StartAllFromStore(); err != nil {
		log.Fatalf("start runners: %v", err)
	}

	mux := http.NewServeMux()
	httpapi.NewHandler(st, reg).Register(mux)

	mux.HandleFunc("GET /health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	})

	log.Printf("agent-simulator listening on %s, backend=%s", addr, backendURL)
	if err := http.ListenAndServe(addr, mux); err != nil {
		log.Fatalf("server: %v", err)
	}
}

func envOr(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

func envFloatOr(key string, fallback float64) float64 {
	v := os.Getenv(key)
	if v == "" {
		return fallback
	}
	f, err := strconv.ParseFloat(v, 64)
	if err != nil {
		return fallback
	}
	return f
}
