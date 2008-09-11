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

--redirects current prod row count = 19836
create table 
rhn_redirects
(
    recid                            number   (12)
        constraint rhn_rdrct_recid_nn not null 
        constraint rhn_rdrct_recid_pk primary key
            using index tablespace [[8m_tbs]]
            storage( pctincrease 1 freelists 16 )
            initrans 32,
    customer_id                      number   (12),
    contact_id                       number   (12),
    redirect_type                    varchar2 (20)
        constraint rhn_rdrct_redirect_type_nn not null,
    description                      varchar2 (25),
    reason                           varchar2 (2000),
    expiration                       date
        constraint rhn_rdrct_expiration_nn not null,
    last_update_user                 varchar2 (40),
    last_update_date                 date,
    start_date                       date
        constraint rhn_rdrct_start_date_nn not null,
    recurring                        number (12,0) default 0
        constraint rhn_rdrct_recurring_nn not null
        constraint RHN_RDRCT_RECUR_VALID check (recurring in (0, 1)),
    recurring_frequency             number (12,0) default 2
         constraint RHN_RDRCT_RECUR_FREQ_VALID check (recurring_frequency in (2,3,6)),
    recurring_duration              number(12,0)  default 0,
    recurring_dur_type              number(12,0) default 12
           constraint rhn_rdrct_rec_dtype_valid check ( recurring_dur_type in (12,11,5,3,1) )
)
    storage ( freelists 16 )
    initrans 32;

comment on table rhn_redirects 
    is 'rdrct  redirect definitions';

create index rhn_rdrct_customer_id_idx 
    on rhn_redirects ( customer_id )
    tablespace [[8m_tbs]]
    storage ( freelists 16 )
    initrans 32;

create index rhn_rdrct_redirect_type_idx 
    on rhn_redirects ( redirect_type )
    tablespace [[8m_tbs]]
    storage ( freelists 16 )
    initrans 32;

alter table rhn_redirects 
    add constraint rhn_rdrct_start_lte_expir check (start_date <= expiration );

create index rhn_rdrct_cid_idx
	on rhn_redirects( contact_id )
	tablespace [[4m_tbs]]
	storage ( freelists 16 )
	initrans 32;

alter table rhn_redirects
    add constraint rhn_rdrct_cntct_contact_id_fk
    foreign key ( contact_id )
    references web_contact( id );

alter table rhn_redirects
    add constraint rhn_rdrct_cstmr_customer_id_fk
    foreign key ( customer_id )
    references web_customer( id );

alter table rhn_redirects
    add constraint rhn_rdrct_rdrtp_redir_type_fk
    foreign key ( redirect_type )
    references rhn_redirect_types( name );

create sequence rhn_redirects_recid_seq;

--$Log$
--Revision 1.4  2004/05/28 22:27:32  pjones
--bugzilla: none -- audit usage of rhnServer/web_contact/web_customer in
--monitoring schema
--
--Revision 1.3  2004/05/06 17:35:10  kja
--More syntax/identifier length changes.
--
--Revision 1.2  2004/04/30 14:36:51  kja
--Moving foreign keys for non-circular dependencies.
--
--Revision 1.1  2004/04/16 02:21:57  kja
--More monitoring schema.
--
--
--$Id$
--
--
