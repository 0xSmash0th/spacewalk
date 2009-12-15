-- created by Oraschemadoc Mon Aug 31 10:54:37 2009
-- visit http://www.yarpen.cz/oraschemadoc/ for more info

  CREATE OR REPLACE TRIGGER "MIM1"."RHN_CONFCHAN_DEL_TRIG" 
before delete on rhnConfigChannel
for each row
declare
	cursor snapshots is
		select	snapshot_id id
		from	rhnSnapshotConfigChannel
		where	config_channel_id = :old.id;
begin
	for snapshot in snapshots loop
		update rhnSnapshot
			set invalid = lookup_snapshot_invalid_reason('cc_removed')
			where id = snapshot.id;
		delete from rhnSnapshotConfigChannel
			where snapshot_id = snapshot.id
				and config_channel_id = :old.id;
	end loop;
end;
ALTER TRIGGER "MIM1"."RHN_CONFCHAN_DEL_TRIG" ENABLE
 
/
