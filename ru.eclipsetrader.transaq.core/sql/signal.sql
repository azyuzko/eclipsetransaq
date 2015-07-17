create table signal
( id number primary key,
  session_id char(36),
  strategy varchar2(20),
  instrument varchar2(20),
  signaltime date,
  buysell char(1),
  quantity number(10),
  price number(20,6),
  byMarket number(1),
  log clob
);

create sequence seq_signal start with 100 increment by 10 nocache nocycle;
