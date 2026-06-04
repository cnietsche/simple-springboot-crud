package backend

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"
)

type Client struct {
	baseURL    string
	httpClient *http.Client
}

func NewClient(baseURL string) *Client {
	return &Client{
		baseURL: baseURL,
		httpClient: &http.Client{
			Timeout: 60 * time.Second,
		},
	}
}

type createUserRequest struct {
	Name     string `json:"name"`
	Email    string `json:"email"`
	Username string `json:"username"`
	Password string `json:"password"`
	Type     string `json:"type,omitempty"`
}

type createUserResponse struct {
	ID    string `json:"id"`
	Name  string `json:"name"`
	Email string `json:"email"`
}

type loginRequest struct {
	Identifier string `json:"identifier"`
	Password   string `json:"password"`
}

type loginResponse struct {
	ID    string `json:"id"`
	Name  string `json:"name"`
	Email string `json:"email"`
}

type generateOverloadRequest struct {
	UserID string `json:"userId"`
	Count  int    `json:"count"`
}

type errorBody struct {
	Message string `json:"message"`
}

func (c *Client) CreateUser(name, email, username, password string) (createUserResponse, error) {
	body, _ := json.Marshal(createUserRequest{
		Name: name, Email: email, Username: username, Password: password, Type: "USER",
	})
	return postJSON[createUserResponse](c, "/api/users", body, http.StatusCreated)
}

func (c *Client) Login(identifier, password string) (loginResponse, int, error) {
	body, _ := json.Marshal(loginRequest{Identifier: identifier, Password: password})
	req, err := http.NewRequest(http.MethodPost, c.baseURL+"/api/auth/login", bytes.NewReader(body))
	if err != nil {
		return loginResponse{}, 0, err
	}
	req.Header.Set("Content-Type", "application/json")
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return loginResponse{}, 0, err
	}
	defer resp.Body.Close()
	data, err := io.ReadAll(resp.Body)
	if err != nil {
		return loginResponse{}, resp.StatusCode, err
	}
	if resp.StatusCode == http.StatusOK {
		var lr loginResponse
		if err := json.Unmarshal(data, &lr); err != nil {
			return loginResponse{}, resp.StatusCode, err
		}
		return lr, resp.StatusCode, nil
	}
	return loginResponse{}, resp.StatusCode, parseError(data, resp.StatusCode)
}

func (c *Client) GenerateOverload(userID string, count int) error {
	body, _ := json.Marshal(generateOverloadRequest{UserID: userID, Count: count})
	_, err := postJSON[struct{}](c, "/api/overloads/generate", body, http.StatusCreated)
	return err
}

func postJSON[T any](c *Client, path string, body []byte, expectStatus int) (T, error) {
	var zero T
	req, err := http.NewRequest(http.MethodPost, c.baseURL+path, bytes.NewReader(body))
	if err != nil {
		return zero, err
	}
	req.Header.Set("Content-Type", "application/json")
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return zero, err
	}
	defer resp.Body.Close()
	data, err := io.ReadAll(resp.Body)
	if err != nil {
		return zero, err
	}
	if resp.StatusCode != expectStatus {
		return zero, parseError(data, resp.StatusCode)
	}
	if len(data) == 0 {
		return zero, nil
	}
	var out T
	if err := json.Unmarshal(data, &out); err != nil {
		return zero, err
	}
	return out, nil
}

func parseError(data []byte, status int) error {
	var eb errorBody
	if json.Unmarshal(data, &eb) == nil && eb.Message != "" {
		return fmt.Errorf("backend %d: %s", status, eb.Message)
	}
	return fmt.Errorf("backend %d: %s", status, string(data))
}
