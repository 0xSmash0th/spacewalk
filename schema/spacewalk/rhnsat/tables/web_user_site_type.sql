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
-- Site Type
--
--
--

create table
web_user_site_type
(
	type			char(1)
				constraint wust_type_nn not null
				constraint wust_type_pk primary key,
	description		varchar2(64)
				constraint wust_desc_nn not null
)
	enable row movement
	;

insert into WEB_USER_SITE_TYPE VALUES('M', 'MARKET');
insert into WEB_USER_SITE_TYPE VALUES('B', 'BILL_TO');
insert into WEB_USER_SITE_TYPE VALUES('S', 'SHIP_TO');
insert into WEB_USER_SITE_TYPE VALUES('R', 'SERVICE');

--
-- Revision 1.2  2002/05/09 05:37:31  gafton
-- re-unify again
--
-- Revision 1.1  2002/02/13 16:20:43  pjones
-- commit these here
--
