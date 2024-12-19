CREATE TABLE Customer (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100)
);
INSERT INTO Customer (first_name, last_name, email) VALUES ('Initial', 'User', 'initial.user@example.com');