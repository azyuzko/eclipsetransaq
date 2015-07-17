create table event_audit(
  id number primary key,
  session_id char(36),
  event_time timestamp(6) not null,
  direction varchar2(10),
  operation varchar2(100),
  data clob,
  create_on timestamp(6) default systimestamp not null
 );

create index event_audit_ix1 on event_audit(event_time);
create index event_audit_ix2 on event_audit(operation, event_time);
create index event_audit_ix3 on event_audit(session_id);

create sequence seq_event_audit start with 1000 increment by 1 cache 1000 nocycle;

