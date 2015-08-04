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
openinterest number(10),
constraint pk_ticks primary key (board, seccode, time, tradeno)
)
organization index;


