CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS club (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    call_number VARCHAR(255),
    average_rating DOUBLE
    );

CREATE TABLE IF NOT EXISTS review (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      comment VARCHAR(255) NOT NULL,
    rating INT NOT NULL,
    club_id BIGINT,
    user_id BIGINT,
    created_at TIMESTAMP,
    FOREIGN KEY (club_id) REFERENCES club(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
    );
