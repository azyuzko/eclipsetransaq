create table quotes (
  id number,
  time timestamp,
  board varchar2(20),
  seccode varchar2(20),
  price number(20,6),
	yield number(10),
	buy number(10),
  sell number(10)
);

create index quotes_IX1 on quotes(board, seccode, time);
