create table security(
  id number,
  session_id char(36)not null,
  server varchar2(20),
  secCode varchar2(20),
	board varchar2(20),
	market varchar2(20),
	decimals number(10),
	minStep number(20,6),
	lotSize number(10),
	point_cost number(20,6),  
	active number(1),
	TZ varchar2(50),
	shortName varchar2(200),
	type varchar2(20),
	useCredit number(1),
	byMarket number(1),
	noSplit number(1),
	immorCancel number(1),
	cancelBalance number(1)
);

alter table security add constraint uq_security_1 unique (secCode, board);


