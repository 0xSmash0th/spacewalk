-- created by Oraschemadoc Tue Nov  2 08:33:15 2010
-- visit http://www.yarpen.cz/oraschemadoc/ for more info

  CREATE OR REPLACE TRIGGER "SPACEWALK"."PRODUCT_NAME_MOD_TRIG" 
before insert or update on rhnProductName
for each row
begin
    :new.modified := sysdate;
end;
ALTER TRIGGER "SPACEWALK"."PRODUCT_NAME_MOD_TRIG" ENABLE
 
/
