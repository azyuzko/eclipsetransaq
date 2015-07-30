create table client
(id varchar2(50) primary key,
  server varchar2(20),
	remove number(1),
	currency varchar2(20),
	type varchar2(20),
	ml_intraday number(20,6),
  ml_overnight number(20,6),
	ml_restrict number(20,6),
	ml_call number(20,6),
	ml_close number(20,6)
);
