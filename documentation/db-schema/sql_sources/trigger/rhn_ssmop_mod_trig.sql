-- created by Oraschemadoc Fri Jan 22 13:41:01 2010
-- visit http://www.yarpen.cz/oraschemadoc/ for more info

  CREATE OR REPLACE TRIGGER "MIM_H1"."RHN_SSMOP_MOD_TRIG" 
before insert or update on rhnSsmOperation
for each row
begin
    :new.modified := sysdate;
end;
ALTER TRIGGER "MIM_H1"."RHN_SSMOP_MOD_TRIG" ENABLE
 
/
