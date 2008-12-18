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

create table
state_change
(
    o_id       varchar2(64)
    	       constraint state_change_o_id_nn not null,
    entry_time number
               constraint state_change_etime_nn not null,
    data       varchar2(4000)
)
    enable row movement
  ;
    
create index state_change_oid_entry_idx
    on state_change(o_id, entry_time)
    tablespace [[64k_tbs]]
  ;
