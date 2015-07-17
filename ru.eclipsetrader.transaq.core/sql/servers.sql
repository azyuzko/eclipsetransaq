create table servers (
  id varchar2(20) primary key,
  description varchar(200),
  host varchar2(200),
  port number(10),
  login varchar2(20),
  password varchar2(20),
  dbLogging number(1),
  logDir varchar2(200),
  logLevel varchar2(20),
  requestDelay number(10) default 100,
  autopos number(1) default 0,
  micex_registers number(1) default 0,
  milliseconds number(1) default 1,
  utc_time number(1) default 0
);