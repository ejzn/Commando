
# --- !Ups


CREATE TABLE company (

    id int(10) NOT NULL AUTO_INCREMENT,
    name varchar(255),

    created datetime NOT NULL,
    updated datetime NOT NULL,
    deleted datetime NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE location (

    id int(10) NOT NULL AUTO_INCREMENT,
    name varchar(255),
    company_id int(10) NOT NULL,

    created datetime NOT NULL,
    updated datetime NOT NULL,
    deleted datetime NOT NULL,

    PRIMARY KEY (id),
    KEY location_company_id_foreign_id (company_id)
);


CREATE TABLE unit (

    id int(10) NOT NULL AUTO_INCREMENT,
    name varchar(255),

    company_id int(10) NOT NULL,
    location_id int(10) NOT NULL,

    created datetime NOT NULL,
    updated datetime NOT NULL,
    deleted datetime NOT NULL,

    PRIMARY KEY (id),
    KEY unit_company_id_foreign_id (company_id),
    KEY unit_location_id_foreign_id (location_id)
);

ALTER TABLE unit
  ADD CONSTRAINT unit_company_id_foreign_id FOREIGN KEY (company_id) REFERENCES company (id) ON DELETE CASCADE,
  ADD CONSTRAINT unit_location_id_foreign_id FOREIGN KEY (location_id) REFERENCES location (id);

ALTER TABLE location
  ADD CONSTRAINT location_company_id_foreign_id FOREIGN KEY (company_id) REFERENCES company (id);

# --- !Downs
DROP TABLE unit;
DROP TABLE location;
DROP TABLE company;
