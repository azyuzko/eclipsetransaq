create table FORTSMONEY_POSITION
(
  session_id CHAR(36),
  server     VARCHAR2(20),
  client     VARCHAR2(50),
  market     NUMBER,
  shortname  VARCHAR2(50),
  curr       NUMBER(20,6),
  blocked    NUMBER(20,6),
  free       NUMBER(20,6),
  varmargin  NUMBER(20,6)
);
