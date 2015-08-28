create table stoporders
( server varchar2(20),
  activeorderno varchar2(20),
  transactionId varchar2(20),
  board varchar2(20),
  seccode varchar2(20),
  client varchar2(50),
  buysell char(1),
  canceller varchar2(200),
  alltradeno number,
  validbefore timestamp,
  author varchar2(50),
  accepttime timestamp,
  linkedorderno number,
  expdate timestamp,
	status varchar2(50),
  
  -- stoploss
	usecredit varchar2(50),
	sl_activationprice number(20,6),
	sl_guardtime number(5), -- in seconds
	sl_brokerref varchar2(20),
	sl_quantity  varchar2(20),-- :integer или :double (в случае %)
	orderprice number(20,6), 
  
  --take profit
  tp_activationprice number(20,6),
	tp_guardtime number(5), -- in seconds
	tp_brokerref varchar2(20),
  tp_quantity varchar2(20), -- :integer или :double (в случае %)
	extremum number(20,6),
	tp_level number(20,6),
	correction varchar2(20),
	guardspread number(20,6)
);
