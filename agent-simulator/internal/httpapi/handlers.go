package httpapi

import (
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"strings"

	"github.com/cnietsche/agent-simulator/internal/agent"
	"github.com/cnietsche/agent-simulator/internal/store"
)

type Handler struct {
	store    *store.Store
	registry *agent.Registry
}

func NewHandler(st *store.Store, reg *agent.Registry) *Handler {
	return &Handler{store: st, registry: reg}
}

func (h *Handler) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /agents", h.listAgents)
	mux.HandleFunc("POST /agents", h.createAgent)
	mux.HandleFunc("PUT /agents/{id}", h.updateAgent)
	mux.HandleFunc("DELETE /agents/{id}", h.deleteAgent)
}

func (h *Handler) listAgents(w http.ResponseWriter, r *http.Request) {
	agents, err := h.store.ListAgents()
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	out := make([]AgentResponse, 0, len(agents))
	for _, a := range agents {
		out = append(out, h.toResponse(a))
	}
	writeJSON(w, http.StatusOK, out)
}

func (h *Handler) createAgent(w http.ResponseWriter, r *http.Request) {
	frequency := 0.5
	var req FrequencyRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil && !errors.Is(err, io.EOF) {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}
	if req.Frequency != nil {
		frequency = *req.Frequency
	}
	if err := validateFrequency(frequency); err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
		return
	}
	a, err := h.store.CreateAgent(frequency)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	h.registry.Start(a.ID, a.Frequency)
	writeJSON(w, http.StatusCreated, h.toResponse(a))
}

func (h *Handler) updateAgent(w http.ResponseWriter, r *http.Request) {
	id := r.PathValue("id")
	var req FrequencyRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}
	if req.Frequency == nil {
		writeError(w, http.StatusBadRequest, "frequency is required")
		return
	}
	if err := validateFrequency(*req.Frequency); err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
		return
	}
	if err := h.store.UpdateAgentFrequency(id, *req.Frequency); err != nil {
		if strings.Contains(err.Error(), "not found") {
			writeError(w, http.StatusNotFound, "agent not found")
			return
		}
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	h.registry.UpdateFrequency(id, *req.Frequency)
	a, err := h.store.GetAgent(id)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	writeJSON(w, http.StatusOK, h.toResponse(a))
}

func (h *Handler) deleteAgent(w http.ResponseWriter, r *http.Request) {
	id := r.PathValue("id")
	h.registry.Stop(id)
	if err := h.store.DeleteAgent(id); err != nil {
		if strings.Contains(err.Error(), "not found") {
			writeError(w, http.StatusNotFound, "agent not found")
			return
		}
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	w.WriteHeader(http.StatusNoContent)
}

func (h *Handler) toResponse(a store.Agent) AgentResponse {
	resp := AgentResponse{
		ID:        a.ID,
		CreatedAt: a.CreatedAt,
		Frequency: a.Frequency,
		Running:   h.registry.IsRunning(a.ID),
	}
	if st, ok := h.registry.Status(a.ID); ok {
		resp.Busy = st.Busy
		resp.KnownLoginCount = st.KnownLoginCount
		if st.LoggedInUserID != "" {
			resp.LoggedInUserID = &st.LoggedInUserID
		}
		if st.LastAction != "" {
			resp.LastAction = &st.LastAction
		}
		if st.LastActionAt != nil {
			t := *st.LastActionAt
			resp.LastActionAt = &t
		}
	} else {
		count, _ := h.store.CountKnownLogins(a.ID)
		resp.KnownLoginCount = count
	}
	return resp
}

func validateFrequency(f float64) error {
	if f < 0 || f > 1 {
		return errors.New("frequency must be between 0 and 1")
	}
	return nil
}

func writeJSON(w http.ResponseWriter, status int, v any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(v)
}

func writeError(w http.ResponseWriter, status int, message string) {
	writeJSON(w, status, map[string]string{"message": message})
}
