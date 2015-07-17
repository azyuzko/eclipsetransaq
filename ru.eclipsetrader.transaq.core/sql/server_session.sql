create table server_session(
  session_id char(36) primary key,
  serverid varchar2(20),
  status varchar2(50),
  connected date,
  disconnected date,
  timezone varchar2(50),
  error varchar2(200)
);

alter table server_session add constraint server_session_serverid_fk
 foreign key (serverid) references servers(id) on delete set null;