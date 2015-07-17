create table candlekind (
  session_id char(36),
  server varchar2(20),
  id number primary key,
  period number(10),
  name varchar2(50)
);