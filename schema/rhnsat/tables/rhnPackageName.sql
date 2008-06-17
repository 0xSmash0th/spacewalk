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

CREATE TABLE
rhnPackageName
(
        id              number
			constraint rhn_pn_id_nn not null
                        constraint rhn_pn_id_pk primary key
			using index tablespace [[2m_tbs]],
        name            varchar2(128)
			constraint rhn_pn_name_nn not null
)
	storage ( freelists 16 )
	initrans 32;

create unique index rhn_pn_name_uq
	on rhnPackageName(name)
	tablespace [[2m_tbs]]
	storage ( freelists 16 )
	initrans 32;

create sequence rhn_pkg_name_seq;

-- $Log$
-- Revision 1.11  2003/01/30 16:11:28  pjones
-- storage parameters, also fix deps to make it build again
--
-- Revision 1.10  2002/05/10 21:54:45  pjones
-- add rhnFAQClass, and make it a dep for rhnFAQ
-- add grants where appropriate
-- add cvs id/log where it's been missed
-- split data out where appropriate
-- add excludes where appropriate
-- make sure it still builds (at least as sat).
--
