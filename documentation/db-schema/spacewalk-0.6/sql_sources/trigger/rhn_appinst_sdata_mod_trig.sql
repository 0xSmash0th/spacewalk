-- created by Oraschemadoc Mon Aug 31 10:54:36 2009
-- visit http://www.yarpen.cz/oraschemadoc/ for more info

  CREATE OR REPLACE TRIGGER "MIM1"."RHN_APPINST_SDATA_MOD_TRIG" 
before insert or update on rhnAppInstallSessionData
for each row
begin
	:new.modified := sysdate;
end rhn_appinst_sdata_mod_trig;
ALTER TRIGGER "MIM1"."RHN_APPINST_SDATA_MOD_TRIG" ENABLE
 
/
