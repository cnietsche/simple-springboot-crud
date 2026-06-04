package store

import (
	"database/sql"
	"fmt"
	"time"

	"github.com/google/uuid"
	_ "modernc.org/sqlite"
)

type Agent struct {
	ID        string
	CreatedAt time.Time
	Frequency float64
}

type KnownLogin struct {
	ID       string
	AgentID  string
	Username string
	Password string
}

type Store struct {
	db *sql.DB
}

func Open(path string) (*Store, error) {
	db, err := sql.Open("sqlite", path)
	if err != nil {
		return nil, err
	}
	if _, err := db.Exec(`PRAGMA foreign_keys = ON`); err != nil {
		_ = db.Close()
		return nil, err
	}
	s := &Store{db: db}
	if err := s.migrate(); err != nil {
		_ = db.Close()
		return nil, err
	}
	return s, nil
}

func (s *Store) Close() error {
	return s.db.Close()
}

func (s *Store) migrate() error {
	schema := `
CREATE TABLE IF NOT EXISTS agent (
  id TEXT PRIMARY KEY,
  created_at TEXT NOT NULL,
  frequency REAL NOT NULL CHECK (frequency >= 0 AND frequency <= 1)
);
CREATE TABLE IF NOT EXISTS known_login (
  id TEXT PRIMARY KEY,
  agent_id TEXT NOT NULL REFERENCES agent(id) ON DELETE CASCADE,
  username TEXT NOT NULL,
  password TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_known_login_agent_id ON known_login(agent_id);
`
	_, err := s.db.Exec(schema)
	return err
}

func (s *Store) ListAgents() ([]Agent, error) {
	rows, err := s.db.Query(`SELECT id, created_at, frequency FROM agent ORDER BY created_at ASC`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var agents []Agent
	for rows.Next() {
		var a Agent
		var created string
		if err := rows.Scan(&a.ID, &created, &a.Frequency); err != nil {
			return nil, err
		}
		t, err := time.Parse(time.RFC3339Nano, created)
		if err != nil {
			t, err = time.Parse(time.RFC3339, created)
			if err != nil {
				return nil, fmt.Errorf("parse created_at: %w", err)
			}
		}
		a.CreatedAt = t.UTC()
		agents = append(agents, a)
	}
	return agents, rows.Err()
}

func (s *Store) GetAgent(id string) (Agent, error) {
	var a Agent
	var created string
	err := s.db.QueryRow(
		`SELECT id, created_at, frequency FROM agent WHERE id = ?`, id,
	).Scan(&a.ID, &created, &a.Frequency)
	if err == sql.ErrNoRows {
		return Agent{}, fmt.Errorf("agent not found")
	}
	if err != nil {
		return Agent{}, err
	}
	t, err := time.Parse(time.RFC3339Nano, created)
	if err != nil {
		t, err = time.Parse(time.RFC3339, created)
		if err != nil {
			return Agent{}, err
		}
	}
	a.CreatedAt = t.UTC()
	return a, nil
}

func (s *Store) CreateAgent(frequency float64) (Agent, error) {
	id := uuid.New().String()
	now := time.Now().UTC()
	_, err := s.db.Exec(
		`INSERT INTO agent (id, created_at, frequency) VALUES (?, ?, ?)`,
		id, now.Format(time.RFC3339Nano), frequency,
	)
	if err != nil {
		return Agent{}, err
	}
	return Agent{ID: id, CreatedAt: now, Frequency: frequency}, nil
}

func (s *Store) UpdateAgentFrequency(id string, frequency float64) error {
	res, err := s.db.Exec(`UPDATE agent SET frequency = ? WHERE id = ?`, frequency, id)
	if err != nil {
		return err
	}
	n, err := res.RowsAffected()
	if err != nil {
		return err
	}
	if n == 0 {
		return fmt.Errorf("agent not found")
	}
	return nil
}

func (s *Store) DeleteAgent(id string) error {
	res, err := s.db.Exec(`DELETE FROM agent WHERE id = ?`, id)
	if err != nil {
		return err
	}
	n, err := res.RowsAffected()
	if err != nil {
		return err
	}
	if n == 0 {
		return fmt.Errorf("agent not found")
	}
	return nil
}

func (s *Store) CountKnownLogins(agentID string) (int, error) {
	var n int
	err := s.db.QueryRow(`SELECT COUNT(*) FROM known_login WHERE agent_id = ?`, agentID).Scan(&n)
	return n, err
}

func (s *Store) ListKnownLogins(agentID string) ([]KnownLogin, error) {
	rows, err := s.db.Query(
		`SELECT id, agent_id, username, password FROM known_login WHERE agent_id = ?`, agentID,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var logins []KnownLogin
	for rows.Next() {
		var kl KnownLogin
		if err := rows.Scan(&kl.ID, &kl.AgentID, &kl.Username, &kl.Password); err != nil {
			return nil, err
		}
		logins = append(logins, kl)
	}
	return logins, rows.Err()
}

func (s *Store) InsertKnownLogin(agentID, username, password string) (KnownLogin, error) {
	id := uuid.New().String()
	_, err := s.db.Exec(
		`INSERT INTO known_login (id, agent_id, username, password) VALUES (?, ?, ?, ?)`,
		id, agentID, username, password,
	)
	if err != nil {
		return KnownLogin{}, err
	}
	return KnownLogin{ID: id, AgentID: agentID, Username: username, Password: password}, nil
}
