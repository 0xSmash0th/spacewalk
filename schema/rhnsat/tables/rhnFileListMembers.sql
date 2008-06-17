--
-- Copyright (c) 2008 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
-- 
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation. 
--
--
-- $Id$
--

create table
rhnFileListMembers
(
	file_list_id		number
				constraint rhn_flmembers_flid_nn not null
				constraint rhn_flmembers_flid_fk
					references rhnFileList(id)
					on delete cascade,
	config_file_name_id	number
				constraint rhn_flmembers_cfnid_nn not null
				constraint rhn_flmembers_cfnid_fk
					references rhnConfigFileName(id),
	created			date default (sysdate)	
				constraint rhn_flmembers_creat_nn not null,
	modified		date default (sysdate)
				constraint rhn_flmembers_mod_nn not null
)
	storage ( freelists 16 )
	initrans 32;

create unique index rhn_flmembers_flid_cfnid_uq
	on rhnFileListMembers( file_list_id, config_file_name_id )
	tablespace [[4m_tbs]]
	storage ( freelists 16 )
	initrans 32;

create or replace trigger
rhn_flmembers_mod_trig
before insert or update on rhnFileListMembers
for each row
begin
	:new.modified := sysdate;
end rhn_flmembers_mod_trig;
/
show errors

--
-- $Log$
-- Revision 1.1  2004/05/25 02:25:34  pjones
-- bugzilla: 123426 -- tables in which to keep lists of files to be preserved.
--

