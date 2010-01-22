-- created by Oraschemadoc Fri Jan 22 13:40:54 2010
-- visit http://www.yarpen.cz/oraschemadoc/ for more info

  CREATE OR REPLACE TRIGGER "MIM_H1"."RHN_CHANNELCOMPS_MOD_TRIG" 
before insert or update on rhnChannelComps
for each row
begin
    :new.modified := sysdate;
    -- allow us to manually set last_modified if we wish
    if :new.last_modified = :old.last_modified
    then
        :new.last_modified := sysdate;
        end if;
end rhn_channelcomps_mod_trig;
ALTER TRIGGER "MIM_H1"."RHN_CHANNELCOMPS_MOD_TRIG" ENABLE
 
/
