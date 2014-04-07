# Add Deficiency

# --- !Ups
CREATE TABLE deficiency (
    id int(20) NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    content text NOT NULL,

    unit_id int(10) NOT NULL,

    created datetime NOT NULL,
    updated datetime NOT NULL,
    deleted datetime NOT NULL,

    PRIMARY KEY (id),
    KEY deficiency_unit_id_foreign_id (unit_id)
);

ALTER TABLE deficiency
  ADD CONSTRAINT deficiency_unit_id_foreign_id FOREIGN KEY (unit_id) REFERENCES unit (id);

# --- !Downs
DROP TABLE deficiency;
