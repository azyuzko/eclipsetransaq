create table trades(
tradeno varchar2(36)  primary key,
secid integer,
server varchar2(20),
orderno varchar2(36),
board varchar2(20),
seccode varchar2(20),
client varchar2(100),
buysell char(1),
period varchar2(20),
openinterest number(10),
time date,
brokerref varchar2(200),
value number(20,6),
comission number(20,6),
price number(20,6),
quantity number(10),
items number(10),
yield number(20,6),
accruedint number(20,6),
tradetype varchar2(50),
settlecode varchar2(50),
currentpos number(20,6),
received timestamp
);
