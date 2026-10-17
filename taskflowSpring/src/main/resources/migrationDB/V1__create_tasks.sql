CREATE TABLE tasks (
                       id           UUID          PRIMARY KEY,
                       titre             VARCHAR(100)  NOT NULL,
                       description       VARCHAR(2000),
                       priorite          VARCHAR(20)   NOT NULL,
                       date_echeance     DATE,
                       termine       BOOLEAN       NOT NULL DEFAULT FALSE,
                       owner_id          VARCHAR(64)   NOT NULL,
                       owner_username    VARCHAR(255)  NOT NULL,
                       date_creation     TIMESTAMP     NOT NULL,
                       date_derniere_modif  TIMESTAMP     NOT NULL
);

CREATE INDEX idx_tasks_owner         ON tasks (owner_id);
CREATE INDEX idx_tasks_owner_termine ON tasks (owner_id, termine);
CREATE INDEX idx_tasks_date_echeance ON tasks (date_echeance);