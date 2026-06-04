package action

import (
	"math/rand"

	"github.com/cnietsche/agent-simulator/internal/store"
)

type AgentAction string

const (
	CreateUser           AgentAction = "CREATE_USER"
	LoginFail            AgentAction = "LOGIN_FAIL"
	LoginSuccess         AgentAction = "LOGIN_SUCCESS"
	GenerateOverload50   AgentAction = "GENERATE_OVERLOAD_50"
	GenerateOverload100  AgentAction = "GENERATE_OVERLOAD_100"
	GenerateOverload400  AgentAction = "GENERATE_OVERLOAD_400"
	GenerateOverload1000 AgentAction = "GENERATE_OVERLOAD_1000"
)

type State struct {
	KnownLogins     []store.KnownLogin
	LoggedInUserID  string
}

func AllowedActions(state State) []AgentAction {
	var allowed []AgentAction
	allowed = append(allowed, CreateUser, LoginFail)
	if len(state.KnownLogins) > 0 {
		allowed = append(allowed, LoginSuccess)
	}
	if state.LoggedInUserID != "" {
		allowed = append(allowed,
			GenerateOverload50,
			GenerateOverload100,
			GenerateOverload400,
			GenerateOverload1000,
		)
	}
	return allowed
}

func PickRandom(rng *rand.Rand, state State) AgentAction {
	allowed := AllowedActions(state)
	if len(allowed) == 0 {
		return CreateUser
	}
	return allowed[rng.Intn(len(allowed))]
}

func OverloadCount(a AgentAction) (int, bool) {
	switch a {
	case GenerateOverload50:
		return 50, true
	case GenerateOverload100:
		return 100, true
	case GenerateOverload400:
		return 400, true
	case GenerateOverload1000:
		return 1000, true
	default:
		return 0, false
	}
}
