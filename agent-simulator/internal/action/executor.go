package action

import (
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"math/big"
	mathrand "math/rand"
	"strings"
	"time"

	"github.com/cnietsche/agent-simulator/internal/backend"
	"github.com/cnietsche/agent-simulator/internal/store"
)

type Executor struct {
	Store   *store.Store
	Backend *backend.Client
	AgentID string
	RNG     *mathrand.Rand
}

type Result struct {
	Action         AgentAction
	LoggedInUserID string
	KnownLogins    []store.KnownLogin
}

func (e *Executor) Execute(act AgentAction, state State) (Result, error) {
	result := Result{Action: act, KnownLogins: state.KnownLogins, LoggedInUserID: state.LoggedInUserID}

	switch act {
	case CreateUser:
		name := randomName(e.RNG)
		username := randomUsername(e.RNG)
		email := strings.ToLower(username) + "@sim.local"
		password := randomPassword(e.RNG)
		resp, err := e.Backend.CreateUser(name, email, username, password)
		if err != nil {
			return result, err
		}
		kl, err := e.Store.InsertKnownLogin(e.AgentID, username, password)
		if err != nil {
			return result, err
		}
		result.KnownLogins = append(state.KnownLogins, kl)
		_ = resp
		return result, nil

	case LoginFail:
		if len(state.KnownLogins) > 0 {
			kl := state.KnownLogins[e.RNG.Intn(len(state.KnownLogins))]
			_, status, err := e.Backend.Login(kl.Username, kl.Password+"_wrong")
			if err != nil {
				return result, err
			}
			if status != 401 {
				return result, fmt.Errorf("expected 401, got %d", status)
			}
			return result, nil
		}
		_, status, err := e.Backend.Login(randomIdentifier(e.RNG), randomPassword(e.RNG))
		if err != nil {
			return result, err
		}
		if status != 401 {
			return result, fmt.Errorf("expected 401, got %d", status)
		}
		return result, nil

	case LoginSuccess:
		if len(state.KnownLogins) == 0 {
			return result, fmt.Errorf("no known logins")
		}
		kl := state.KnownLogins[e.RNG.Intn(len(state.KnownLogins))]
		resp, status, err := e.Backend.Login(kl.Username, kl.Password)
		if err != nil {
			return result, err
		}
		if status != 200 {
			return result, fmt.Errorf("login failed with status %d", status)
		}
		result.LoggedInUserID = resp.ID
		return result, nil

	default:
		count, ok := OverloadCount(act)
		if !ok {
			return result, fmt.Errorf("unknown action %s", act)
		}
		if state.LoggedInUserID == "" {
			return result, fmt.Errorf("not logged in")
		}
		if err := e.Backend.GenerateOverload(state.LoggedInUserID, count); err != nil {
			return result, err
		}
		return result, nil
	}
}

func randomName(rng *mathrand.Rand) string {
	return "SimUser" + randomSuffix(rng, 6)
}

func randomUsername(rng *mathrand.Rand) string {
	return "sim_" + randomSuffix(rng, 8)
}

func randomIdentifier(rng *mathrand.Rand) string {
	return "unknown_" + randomSuffix(rng, 8) + "@sim.local"
}

func randomPassword(rng *mathrand.Rand) string {
	return "Pass" + randomSuffix(rng, 10) + "1"
}

func randomSuffix(rng *mathrand.Rand, n int) string {
	const chars = "abcdefghijklmnopqrstuvwxyz0123456789"
	b := make([]byte, n)
	for i := range b {
		b[i] = chars[rng.Intn(len(chars))]
	}
	return string(b)
}

func secureSeed() int64 {
	var b [8]byte
	if _, err := rand.Read(b[:]); err != nil {
		return time.Now().UnixNano()
	}
	n, _ := new(big.Int).SetString(hex.EncodeToString(b[:]), 16)
	return n.Int64()
}

func NewRNG() *mathrand.Rand {
	return mathrand.New(mathrand.NewSource(secureSeed()))
}
