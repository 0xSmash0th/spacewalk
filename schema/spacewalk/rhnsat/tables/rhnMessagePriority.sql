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
--
-- priority for an rhnMessage

create table
rhnMessagePriority
(
	id		number
			constraint rhn_m_priority_id_nn not null
			constraint rhn_m_priority_id_pk primary key
				using index tablespace [[64k_tbs]],
	label		varchar2(48)
			constraint rhn_m_priority_label_nn not null
)
	storage ( freelists 16 )
	enable row movement
	initrans 32;

create sequence rhn_m_priority_id_seq;

create unique index rhn_m_priority_label_uq
	on rhnMessagePriority(label)
	tablespace [[64k_tbs]]
	storage ( freelists 16 )
	initrans 32;

-- last created gets used in Rule, make it the most useful index.
create index rhn_m_priority_label_id_idx
	on rhnMessagePriority(label,id)
	tablespace [[64k_tbs]]
	storage ( freelists 16 )
	initrans 32;

--
-- Revision 1.3  2003/01/30 16:11:28  pjones
-- storage parameters, also fix deps to make it build again
--
-- Revision 1.2  2002/07/29 20:39:07  pjones
-- oops, tablespaces for satcon
--
-- Revision 1.1  2002/07/29 20:26:23  pjones
-- add support for labeled message priorities
--
