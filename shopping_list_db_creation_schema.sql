CREATE TABLE amount_type(
    amount_type_id BIGINT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(255),
    type_name VARCHAR(255),
    saved_time DATETIME,
    deleted TINYINT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (amount_type_id)
);

CREATE TABLE category(
    category_id BIGINT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(255),
    category_name VARCHAR(255),
    saved_time DATETIME,
    deleted TINYINT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (category_id)
);

CREATE TABLE shopping_item(
    shopping_item_id BIGINT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(255),
    amount_type_id BIGINT,
    category_id BIGINT,
    item_name VARCHAR(255),
    amount DOUBLE,
    bought TINYINT(1) NOT NULL DEFAULT b'0',
    saved_time DATETIME,
    deleted TINYINT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (shopping_item_id),
    CONSTRAINT fk_shoppingitem_amounttype FOREIGN KEY (amount_type_id)
        REFERENCES amount_type(amount_type_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_shoppingitem_category FOREIGN KEY (category_id)
        REFERENCES category(category_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
