package action

import (
	"math/rand"
	"testing"

	"github.com/cnietsche/agent-simulator/internal/store"
)

func TestAllowedActions_emptyState(t *testing.T) {
	allowed := AllowedActions(State{})
	if len(allowed) != 2 {
		t.Fatalf("expected CREATE_USER and LOGIN_FAIL, got %v", allowed)
	}
}

func TestAllowedActions_withLogin(t *testing.T) {
	state := State{
		KnownLogins: []store.KnownLogin{{Username: "u", Password: "p"}},
	}
	allowed := AllowedActions(state)
	foundLoginSuccess := false
	for _, a := range allowed {
		if a == LoginSuccess {
			foundLoginSuccess = true
		}
	}
	if !foundLoginSuccess {
		t.Fatal("expected LOGIN_SUCCESS in allowed actions")
	}
}

func TestAllowedActions_withSession(t *testing.T) {
	state := State{
		KnownLogins:    []store.KnownLogin{{Username: "u", Password: "p"}},
		LoggedInUserID: "user-id",
	}
	allowed := AllowedActions(state)
	if len(allowed) < 6 {
		t.Fatalf("expected overload actions, got %v", allowed)
	}
}

func TestPickRandom_onlyAllowed(t *testing.T) {
	rng := rand.New(rand.NewSource(1))
	state := State{LoggedInUserID: "id", KnownLogins: []store.KnownLogin{{}}}
	for i := 0; i < 50; i++ {
		a := PickRandom(rng, state)
		allowed := AllowedActions(state)
		ok := false
		for _, x := range allowed {
			if x == a {
				ok = true
				break
			}
		}
		if !ok {
			t.Fatalf("picked disallowed action %s", a)
		}
	}
}
