create table security(
  id number,
  server varchar2(20),
  seccode varchar2(20),
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
	cancelBalance number(1),
  
  minprice number(20,6), 
	maxprice number(20,6),
	buy_deposit number(20,6),
	sell_deposit number(20,6),
	bgo_c number(20,6),
	bgo_nc number(20,6),
	bgo_buy number(20,6)
);

alter table security add constraint uq_security_1 unique (seccode, board);


