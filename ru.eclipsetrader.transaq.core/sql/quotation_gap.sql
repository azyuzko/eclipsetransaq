
create sequence quotation_seq start with 10000 increment by 10 cache 100 nocycle;

create table QUOTATION_GAP
(
  id number,
  board   VARCHAR2(20),
  seccode VARCHAR2(20),
  time    TIMESTAMP(6),
  hashmap VARCHAR2(3100),
  constraint pk_quotation_gap primary key (board, seccode, time, id)
) organization index;

