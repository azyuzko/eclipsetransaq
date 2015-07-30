
create sequence quote_seq start with 10000 increment by 10 cache 100 nocycle;

create table quotes (
  id number not null,
  time timestamp,
  board varchar2(20),
  seccode varchar2(20),
  price number(20,6),
	yield number(10),
	buy number(10),
  sell number(10)
);

create index quotes_IX1 on quotes(board, seccode, time);

create index quotes_IX2 on quotes(time);
