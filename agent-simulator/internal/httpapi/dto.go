package httpapi

import "time"

type FrequencyRequest struct {
	Frequency *float64 `json:"frequency"`
}

type AgentResponse struct {
	ID              string     `json:"id"`
	CreatedAt       time.Time  `json:"createdAt"`
	Frequency       float64    `json:"frequency"`
	Running         bool       `json:"running"`
	Busy            bool       `json:"busy"`
	KnownLoginCount int        `json:"knownLoginCount"`
	LoggedInUserID  *string    `json:"loggedInUserId"`
	LastAction      *string    `json:"lastAction"`
	LastActionAt    *time.Time `json:"lastActionAt"`
}
