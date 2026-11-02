CREATE TABLE parts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    sku VARCHAR(255),
    price DECIMAL(19, 2),
    quantity INTEGER
);