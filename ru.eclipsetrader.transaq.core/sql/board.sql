create table board
(
	id varchar2(20) primary key,
  session_id char(36),
  server varchar2(20),
	name varchar2(500),
	market varchar2(20),
	type varchar2(20)
);