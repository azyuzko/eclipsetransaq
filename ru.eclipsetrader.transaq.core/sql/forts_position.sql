create table FORTS_POSITION
(
  server            VARCHAR2(20),
  secid             NUMBER,
  seccode           VARCHAR2(50),
  client            VARCHAR2(50),
  market            NUMBER,
  varmargin         NUMBER(20,6),
  startnet          NUMBER(20),
  openbuys          NUMBER(20),
  opensells         NUMBER(20),
  totalnet          NUMBER(20),
  todaybuy          NUMBER(20),
  todaysell         NUMBER(20),
  optmargin         NUMBER(20,6),
  expirationpos     NUMBER(20),
  usedsellspotlimit NUMBER(20,6),
  sellspotlimit     NUMBER(20,6),
  netto             NUMBER(20,6),
  kgo               NUMBER(20,6)
);
