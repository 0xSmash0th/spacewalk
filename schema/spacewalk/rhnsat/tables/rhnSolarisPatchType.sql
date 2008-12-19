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

create table rhnSolarisPatchType (
   id          number
               constraint rhn_solaris_pt_pk primary key,
   name        varchar2(32)
               constraint rhn_solaris_pt_name_nn not null,
   label       varchar2(32)
               constraint rhn_solaris_pt_label_nn not null
)
tablespace [[8m_data_tbs]]
enable row movement
  ;

create sequence rhn_solaris_pt_seq;

--
