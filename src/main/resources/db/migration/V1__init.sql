CREATE TABLE catalog_terms (
    version VARCHAR(50) PRIMARY KEY,
    text    TEXT NOT NULL
);

CREATE TABLE catalog_acceptances (
    org_id           VARCHAR(255) PRIMARY KEY,
    accepted_version VARCHAR(50) NOT NULL,
    acceptor_name    VARCHAR(500) NOT NULL,
    accept_date      DATE NOT NULL
);
