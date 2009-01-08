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
--
--

--redirect_method_targets current prod row count = 0
create table 
rhn_redirect_method_targets
(
    redirect_id         number   (12)
        constraint rhn_rdrme_redirect_id not null,
    contact_method_id   number   (12)
        constraint rhn_rdrme_method_id not null
)
    enable row movement
  ;

comment on table rhn_redirect_method_targets 
    is 'rdrme  redirect method targets';

create unique index rhn_rdrme_pk 
    on rhn_redirect_method_targets ( redirect_id , contact_method_id )
    tablespace [[2m_tbs]]
  ;

create index rhn_rdrme_redirect_id_idx 
    on rhn_redirect_method_targets ( redirect_id )
    tablespace [[2m_tbs]]
  ;

create index rhn_rdrme_cmid_idx
    on rhn_redirect_method_targets ( contact_method_id )
    tablespace [[2m_tbs]]
  ;

alter table rhn_redirect_method_targets 
    add constraint rhn_rdrme_pk primary key ( redirect_id, contact_method_id );

alter table rhn_redirect_method_targets
    add constraint rhn_rdrmt_cmeth_redirect_id_fk
    foreign key ( contact_method_id )
    references rhn_contact_methods( recid )
    on delete cascade;

alter table rhn_redirect_method_targets
    add constraint rhn_rdrmt_rdrct_redirect_id_fk
    foreign key ( redirect_id )
    references rhn_redirects( recid )
    on delete cascade;

--
--Revision 1.2  2004/04/30 14:36:51  kja
--Moving foreign keys for non-circular dependencies.
--
--Revision 1.1  2004/04/16 02:21:57  kja
--More monitoring schema.
--
--
--
--
--
