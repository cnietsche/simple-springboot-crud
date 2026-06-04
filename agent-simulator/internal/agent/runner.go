package agent

import (
	"math"
	"math/rand"
	"sync"
	"time"

	"github.com/cnietsche/agent-simulator/internal/action"
	"github.com/cnietsche/agent-simulator/internal/backend"
	"github.com/cnietsche/agent-simulator/internal/store"
)

type Config struct {
	MinDelaySec float64
	MaxDelaySec float64
}

type Status struct {
	Running        bool
	Busy           bool
	KnownLoginCount int
	LoggedInUserID string
	LastAction     string
	LastActionAt   *time.Time
}

type Runner struct {
	agentID  string
	store    *store.Store
	backend  *backend.Client
	config   Config
	rng      *rand.Rand

	mu             sync.RWMutex
	frequency      float64
	busy           bool
	loggedInUserID string
	lastAction     string
	lastActionAt   *time.Time

	stopCh chan struct{}
	doneCh chan struct{}
}

func NewRunner(agentID string, st *store.Store, bc *backend.Client, frequency float64, cfg Config) *Runner {
	return &Runner{
		agentID:   agentID,
		store:     st,
		backend:   bc,
		config:    cfg,
		rng:       rand.New(rand.NewSource(time.Now().UnixNano())),
		frequency: frequency,
		stopCh:    make(chan struct{}),
		doneCh:    make(chan struct{}),
	}
}

func (r *Runner) Start() {
	go r.loop()
}

func (r *Runner) Stop() {
	close(r.stopCh)
	<-r.doneCh
}

func (r *Runner) SetFrequency(f float64) {
	r.mu.Lock()
	r.frequency = f
	r.mu.Unlock()
}

func (r *Runner) GetFrequency() float64 {
	r.mu.RLock()
	defer r.mu.RUnlock()
	return r.frequency
}

func (r *Runner) Status() Status {
	r.mu.RLock()
	defer r.mu.RUnlock()
	count, _ := r.store.CountKnownLogins(r.agentID)
	var lastAt *time.Time
	if r.lastActionAt != nil {
		t := *r.lastActionAt
		lastAt = &t
	}
	return Status{
		Running:         true,
		Busy:            r.busy,
		KnownLoginCount: count,
		LoggedInUserID:  r.loggedInUserID,
		LastAction:      r.lastAction,
		LastActionAt:    lastAt,
	}
}

func (r *Runner) loop() {
	defer close(r.doneCh)
	for {
		select {
		case <-r.stopCh:
			return
		default:
		}

		freq := r.GetFrequency()
		if freq <= 0 {
			select {
			case <-r.stopCh:
				return
			case <-time.After(1 * time.Second):
				continue
			}
		}

		delay := r.nextDelay(freq)
		select {
		case <-r.stopCh:
			return
		case <-time.After(delay):
		}

		if r.GetFrequency() <= 0 {
			continue
		}

		r.mu.RLock()
		isBusy := r.busy
		r.mu.RUnlock()
		if isBusy {
			continue
		}

		r.runTick()
	}
}

func (r *Runner) nextDelay(frequency float64) time.Duration {
	minD := r.config.MinDelaySec
	maxD := r.config.MaxDelaySec
	if minD <= 0 {
		minD = 1
	}
	if maxD < minD {
		maxD = minD
	}
	mean := maxD*(1-frequency) + minD
	u := r.rng.Float64()
	if u <= 0 {
		u = 1e-9
	}
	seconds := -math.Log(u) * mean
	return time.Duration(seconds * float64(time.Second))
}

func (r *Runner) runTick() {
	logins, err := r.store.ListKnownLogins(r.agentID)
	if err != nil {
		return
	}

	r.mu.RLock()
	state := action.State{
		KnownLogins:    logins,
		LoggedInUserID: r.loggedInUserID,
	}
	r.mu.RUnlock()

	act := action.PickRandom(r.rng, state)
	exec := &action.Executor{
		Store:   r.store,
		Backend: r.backend,
		AgentID: r.agentID,
		RNG:     action.NewRNG(),
	}

	r.mu.Lock()
	r.busy = true
	r.mu.Unlock()

	defer func() {
		r.mu.Lock()
		r.busy = false
		r.mu.Unlock()
	}()

	result, err := exec.Execute(act, state)
	now := time.Now().UTC()

	r.mu.Lock()
	if err == nil {
		r.lastAction = string(result.Action)
		r.lastActionAt = &now
		if result.LoggedInUserID != "" {
			r.loggedInUserID = result.LoggedInUserID
		}
	}
	r.mu.Unlock()
}
