-- created by Oraschemadoc Mon Aug 31 10:54:39 2009
-- visit http://www.yarpen.cz/oraschemadoc/ for more info

  CREATE OR REPLACE TRIGGER "MIM1"."RHN_RAM_MOD_TRIG" 
before insert or update on rhnRam
for each row
begin
        :new.modified := sysdate;
end;
ALTER TRIGGER "MIM1"."RHN_RAM_MOD_TRIG" ENABLE
 
/
