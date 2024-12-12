CREATE TABLE contacts (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255)
);

CREATE TABLE contact_attributes (
    contact_id VARCHAR(255) REFERENCES contacts(id),
    attr_key VARCHAR(255) NOT NULL,
    attr_value TEXT,
    PRIMARY KEY (contact_id, attr_key)
);

CREATE INDEX idx_contacts_user_id ON contacts(user_id);