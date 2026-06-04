package agent

import (
	"sync"

	"github.com/cnietsche/agent-simulator/internal/backend"
	"github.com/cnietsche/agent-simulator/internal/store"
)

type Registry struct {
	store   *store.Store
	backend *backend.Client
	config  Config

	mu      sync.Mutex
	runners map[string]*Runner
}

func NewRegistry(st *store.Store, bc *backend.Client, cfg Config) *Registry {
	return &Registry{
		store:   st,
		backend: bc,
		config:  cfg,
		runners: make(map[string]*Runner),
	}
}

func (reg *Registry) StartAllFromStore() error {
	agents, err := reg.store.ListAgents()
	if err != nil {
		return err
	}
	for _, a := range agents {
		reg.Start(a.ID, a.Frequency)
	}
	return nil
}

func (reg *Registry) Start(agentID string, frequency float64) {
	reg.mu.Lock()
	defer reg.mu.Unlock()
	if _, ok := reg.runners[agentID]; ok {
		return
	}
	r := NewRunner(agentID, reg.store, reg.backend, frequency, reg.config)
	reg.runners[agentID] = r
	r.Start()
}

func (reg *Registry) Stop(agentID string) {
	reg.mu.Lock()
	r, ok := reg.runners[agentID]
	if ok {
		delete(reg.runners, agentID)
	}
	reg.mu.Unlock()
	if ok {
		r.Stop()
	}
}

func (reg *Registry) UpdateFrequency(agentID string, frequency float64) {
	reg.mu.Lock()
	r, ok := reg.runners[agentID]
	reg.mu.Unlock()
	if ok {
		r.SetFrequency(frequency)
	}
}

func (reg *Registry) Status(agentID string) (Status, bool) {
	reg.mu.Lock()
	r, ok := reg.runners[agentID]
	reg.mu.Unlock()
	if !ok {
		return Status{Running: false}, false
	}
	return r.Status(), true
}

func (reg *Registry) IsRunning(agentID string) bool {
	reg.mu.Lock()
	_, ok := reg.runners[agentID]
	reg.mu.Unlock()
	return ok
}
