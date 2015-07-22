create table positions (
  key varchar2(20) primary key,
  type varchar2(20),
  session_id char(36),
  server varchar2(20),
	secid number,
	seccode varchar2(50),
	register varchar2(50),
  client varchar2(50),
	market number,
  shortname varchar2(50),
	asset varchar2(50),
  saldoin number(20,6),
	saldomin number(20,6),
	bought number(20,6),
	sold number(20,6),
	saldo number(20,6),
	ordbuy number(20,6),
	ordsell number(20,6),
  ordbuycond number(20,6),
  comission number(20,6),
  
  -- FORST_MONEY
  curr number(20,6),
  blocked number(20,6),
  free number(20,6),
  varmargin number(20,6),
  
  --FORTS
 	startnet number(20),
	openbuys number(20),
	opensells number(20),
	totalnet number(20),
	todaybuy number(20),
	todaysell number(20),
	optmargin number(20,6),
	expirationpos number(20),
	usedsellspotlimit number(20,6),
	sellspotlimit number(20,6),
	netto number(20,6),
	kgo number(20,6)  
);