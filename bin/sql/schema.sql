-- database.sql
CREATE DATABASE elibrary;
\c elibrary;

-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) CHECK (role IN ('ADMIN', 'STAFF', 'GUEST')) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Books table
CREATE TABLE books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    category VARCHAR(100),
    total_copies INTEGER DEFAULT 1,
    available_copies INTEGER DEFAULT 1,
    file_path VARCHAR(500),
    added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Book issues table
CREATE TABLE book_issues (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    book_id INTEGER REFERENCES books(id),
    issue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP,
    return_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ISSUED' CHECK (status IN ('ISSUED', 'RETURNED', 'OVERDUE'))
);


CREATE TABLE book_category (
    category_id   SERIAL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    status        BOOLEAN NOT NULL DEFAULT true,  -- ✅ true=Active, false=Inactive
    created_on    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ✅ Auto-update updated_on on changes
CREATE OR REPLACE FUNCTION set_book_category_updated_on()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_on = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_book_category_updated_on
    BEFORE UPDATE ON book_category
    FOR EACH ROW
    EXECUTE FUNCTION set_book_category_updated_on();

-- ✅ Indexes for performance
CREATE INDEX idx_book_category_status ON book_category(status);
CREATE INDEX idx_book_category_name ON book_category(category_name);

-- ✅ Add foreign key to books table
ALTER TABLE books 
ADD COLUMN category_id INTEGER REFERENCES book_category(category_id);



-- ✅ Drop old category column (after migration)
ALTER TABLE books DROP COLUMN category;


-- Insert admin user (password: admin123)
INSERT INTO users (username, password, email, role) 
VALUES ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@elibrary.com', 'ADMIN');
