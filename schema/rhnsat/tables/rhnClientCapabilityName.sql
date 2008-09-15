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
rhnClientCapabilityName
(
	id		number
			constraint rhn_clientcapnam_id_nn not null
			constraint rhn_clientcapnam_id_pk primary key 
				using index tablespace [[8m_tbs]],
	name		varchar2(32)
			constraint rhn_clientcapnam_name_nn not null
			constraint rhn_clientcapnam_name_unq unique
) 
	storage (freelists 16 )
	enable row movement
	initrans 32;

create sequence rhn_client_capname_id_seq;

-- $Log$
-- Revision 1.1  2003/07/21 22:11:44  misa
-- bugzilla: none  More normalization; s/value/version/
--
