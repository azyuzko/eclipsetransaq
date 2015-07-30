create table ticks (
server varchar2(20),
tradeno varchar2(36),
time timestamp,
board varchar2(20),
seccode varchar2(20),
buysell char(1),
price number(20,6),
quantity number(10),
period varchar2(20),
openinterest number(10)
);

create index ticks_IX1 on ticks(board, seccode, time);

create index ticks_IX2 on ticks(board, seccode, tradeno);


