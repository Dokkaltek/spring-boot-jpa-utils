CREATE SEQUENCE test_sequence
  INCREMENT BY 1
  START WITH 1;

CREATE TABLE oracle_test_entities(
  id NUMBER(22) DEFAULT test_sequence.NEXTVAL PRIMARY KEY,
  "name" VARCHAR2(100),
  "description" VARCHAR2(100),
  age NUMBER(3),
  active NUMBER(1) DEFAULT 0,
  created_at DATE DEFAULT CURRENT_DATE
);