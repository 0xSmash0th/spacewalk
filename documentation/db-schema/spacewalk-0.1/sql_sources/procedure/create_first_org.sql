-- created by Oraschemadoc Fri Jun 13 14:06:11 2008
-- visit http://www.yarpen.cz/oraschemadoc/ for more info

  CREATE OR REPLACE PROCEDURE "RHNSAT"."CREATE_FIRST_ORG" 
(
	name_in in varchar2,
	password_in in varchar2
) is
	ug_type			number;
	group_val		number;
begin
	insert into web_customer (
		id, name, password,
		oracle_customer_id, oracle_customer_number,
		customer_type
	) values (
		1, name_in, password_in,
		1, 1, 'B'
	);
	select rhn_user_group_id_seq.nextval into group_val from dual;
	select	id
	into	ug_type
	from	rhnUserGroupType
	where	label = 'org_admin';
	insert into rhnUserGroup (
		id, name,
		description,
		max_members, group_type, org_id
	) values (
		group_val, 'Organization Administrators',
		'Organization Administrators for Org ' || name_in || ' (1)',
		NULL, ug_type, 1
	);
	select rhn_user_group_id_seq.nextval into group_val from dual;
	select	id
	into	ug_type
	from	rhnUserGroupType
	where	label = 'org_applicant';
	insert into rhnUserGroup (
		id, name,
		description,
		max_members, group_type, org_id
	) VALues (
		group_val, 'Organization Applicants',
		'Organization Applicants for Org ' || name_in || ' (1)',
		NULL, ug_type, 1
	);
	select rhn_user_group_id_seq.nextval into group_val from dual;
	select	id
	into	ug_type
	from	rhnUserGroupType
	where	label = 'system_group_admin';
	insert into rhnUserGroup (
		id, name,
		description,
		max_members, group_type, org_id
	) values (
		group_val, 'System Group Administrators',
		'System Group Administrators for Org ' || name_in || ' (1)',
		NULL, ug_type, 1
	);
	select rhn_user_group_id_seq.nextval into group_val from dual;
	select	id
	into	ug_type
	from	rhnUserGroupType
	where	label = 'activation_key_admin';
	insert into rhnUserGroup (
		id, name,
		description,
		max_members, group_type, org_id
	) values (
		group_val, 'Activation Key Administrators',
		'Activation Key Administrators for Org ' || name_in || ' (1)',
		NULL, ug_type, 1
	);
	select rhn_user_group_id_seq.nextval into group_val from dual;
	select	id
	into	ug_type
	from	rhnUserGroupType
	where	label = 'channel_admin';
	insert into rhnUserGroup (
		id, name,
		description,
		max_members, group_type, org_id
	) values (
		group_val, 'Channel Administrators',
		'Channel Administrators for Org ' || name_in || ' (1)',
		NULL, ug_type, 1
	);
	select rhn_user_group_id_seq.nextval into group_val from dual;
	select	id
	into	ug_type
	from	rhnUserGroupType
	where	label = 'satellite_admin';
	insert into rhnUserGroup (
		id, name,
		description,
		max_members, group_type, org_id
	) values (
		group_val, 'Satellite Administrators',
		'Satellite Administrators for Org ' || name_in || ' (1)',
		NULL, ug_type, 1
	);
	insert into rhnOrgQuota(
		org_id, total
	) values (
		1, 1024*1024*1024*16
	);
        insert into rhnServerGroup
		( id, name, description, max_members, group_type, org_id )
		select rhn_server_group_id_seq.nextval, sgt.name, sgt.name,
			0, sgt.id, 1
		from rhnServerGroupType sgt
		where sgt.label = 'sw_mgr_entitled';
end create_first_org;
 
/
