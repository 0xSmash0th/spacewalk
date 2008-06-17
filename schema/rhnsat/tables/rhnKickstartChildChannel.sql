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
create table
rhnKickstartChildChannel
(
        channel_id   number
                         constraint rhn_ks_cc_cid_nn not null
                         constraint rhn_ks_cc_cid_fk
                                references rhnChannel(id)
                                on delete cascade,
        ksdata_id number
                        constraint rhn_ks_cc_ksd_nn not null
                        constraint rhn_ks_cc_ksd_fk
                                references rhnKSData(id)
                                on delete cascade,
        created         date default(sysdate)
                        constraint rhn_ks_cc_cre_nn not null,
        modified        date default(sysdate)
                        constraint rhn_ks_cc_mod_nn not null
)
        storage( freelists 16 )
        initrans 32;

create unique index rhn_ks_cc_uq
        on rhnKickstartChildChannel(channel_id, ksdata_id)
        tablespace [[4m_tbs]]
        storage( freelists 16 )
        initrans 32;

create or replace trigger
rhn_ks_cc_mod_trig
before insert or update on rhnKickstartChildChannel
for each row
begin
        :new.modified := sysdate;
end;
/
show errors

