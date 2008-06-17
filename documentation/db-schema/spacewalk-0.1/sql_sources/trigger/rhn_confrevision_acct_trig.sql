-- created by Oraschemadoc Fri Jun 13 14:06:09 2008
-- visit http://www.yarpen.cz/oraschemadoc/ for more info

  CREATE OR REPLACE TRIGGER "RHNSAT"."RHN_CONFREVISION_ACCT_TRIG" 
after insert on rhnConfigRevision
for each row
declare
	org_id number;
	available number;
	added number;
begin
	select	cc.org_id id,
			oq.total + oq.bonus - oq.used available,
			content.file_size added
	into	org_id, available, added
	from	rhnConfigContent	content,
			rhnOrgQuota			oq,
			rhnConfigChannel	cc,
			rhnConfigFile		cf
	where	cf.id = :new.config_file_id
			and cf.config_channel_id = cc.id
			and cc.org_id = oq.org_id
			and :new.config_content_id = content.id;
	if added > available then
		rhn_exception.raise_exception('not_enough_quota');
	end if;
end;
ALTER TRIGGER "RHNSAT"."RHN_CONFREVISION_ACCT_TRIG" ENABLE
 
/
