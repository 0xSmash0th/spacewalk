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
--$Id$
--
--

--reference table
--threshold_type current prod row count = 4
create table 
rhn_threshold_type
(
    name                varchar2 (10)
        constraint rhn_trtyp_name_nn not null
        constraint rhn_trtyp_name_pk primary key
            using index tablespace [[64k_tbs]]
            storage( pctincrease 1 freelists 16 )
            initrans 32,
    description         varchar2 (80)
        constraint rhn_trtyp_desc_nn not null,
    ordinal             number   (3)
        constraint rhn_trtyp_ord_nn not null,
    last_update_user    varchar2 (40),
    last_update_date    date
)
    storage ( freelists 16 )
    initrans 32;

comment on table rhn_threshold_type 
    is 'trtyp threshold type:warn_min, warn_max, crit_min, crit_max';

--$Log$
--Revision 1.2  2004/04/16 21:49:57  kja
--Adjusted small table sizes.  Documented small tables that are primarily static
--as "reference tables."  Fixed up a few syntactical errs.
--
--Revision 1.1  2004/04/16 19:51:58  kja
--More monitoring schema.
--
