CREATE TABLE accounts (
    account_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_name VARCHAR(30) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance NUMERIC(15, 2) DEFAULT 0 NOT NULL,
    currency VARCHAR(3) DEFAULT 'BDT' NOT NULL,
    status VARCHAR(20) DEFAULT 'active' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_user_account FOREIGN KEY (user_id) REFERENCES tbl_users (id)
);