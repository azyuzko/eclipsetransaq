create or replace trigger T_event_audit_BI before insert on event_audit 
for each row 
declare
begin
  if :new.id is null then
    select seq_event_audit.nextval into :new.id from dual;
  end if;
end T_event_audit_BI;
/
