# Add Deficiency

# --- !Ups
CREATE TABLE deficiency (
    id int(20) NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    content text NOT NULL,

    unit_id int(10) NOT NULL,

    created datetime NOT NULL,
    updated datetime NOT NULL,
    deleted datetime,

    PRIMARY KEY (id),
    KEY deficiency_unit_id_foreign_id (unit_id)
);

CREATE TABLE building (
    id int(20) NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,

    location_id int(10) NOT NULL,

    created datetime NOT NULL,
    updated datetime NOT NULL,
    deleted datetime,

    PRIMARY KEY (id),
    KEY building_location_id_foreign_id (location_id)
);




ALTER TABLE deficiency
  ADD CONSTRAINT deficiency_unit_id_foreign_id FOREIGN KEY (unit_id) REFERENCES unit (id);

ALTER TABLE building
  ADD CONSTRAINT building_location_id_foreign_id FOREIGN KEY (location_id) REFERENCES unit (id);

ALTER TABLE unit ADD number int(10) after name;
# --- !Downs
DROP TABLE deficiency;
DROP TABLE building;

ALTER TABLE unit drop column number;
