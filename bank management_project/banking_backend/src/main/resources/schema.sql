CREATE TABLE IF NOT EXISTS accounts (
  account_id VARCHAR(255) NOT NULL PRIMARY KEY,
  balance DECIMAL(19,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
  user_id VARCHAR(255) NOT NULL PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  blocked BOOLEAN NOT NULL,
  account_id VARCHAR(255),
  FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS transactions (
  transaction_id VARCHAR(255) NOT NULL PRIMARY KEY,
  source_account_id VARCHAR(255),
  destination_account_id VARCHAR(255),
  amount DECIMAL(19,2) NOT NULL,
  type VARCHAR(50) NOT NULL,
  timestamp DATETIME NOT NULL,
  description TEXT,
  FOREIGN KEY (source_account_id) REFERENCES accounts(account_id) ON DELETE SET NULL,
  FOREIGN KEY (destination_account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS credit_requests (
  request_id VARCHAR(255) NOT NULL PRIMARY KEY,
  user_id VARCHAR(255) NOT NULL,
  account_id VARCHAR(255) NOT NULL,
  amount DECIMAL(19,2) NOT NULL,
  status VARCHAR(50) NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);
