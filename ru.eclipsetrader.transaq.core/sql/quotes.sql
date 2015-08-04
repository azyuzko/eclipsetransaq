
create sequence quote_seq start with 10000 increment by 10 cache 100 nocycle;

create table quotes (
  id number not null,
  board varchar2(10),
  seccode varchar2(20),
  time timestamp,
  price number(20,6),
	yield number(10),
	buy number(10),
  sell number(10),
  CONSTRAINT pk_quotes PRIMARY KEY (board, seccode, time, id)
) organization index;

