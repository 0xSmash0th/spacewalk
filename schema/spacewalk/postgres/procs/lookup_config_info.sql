-- oracle equivalent source sha1 0c237c905234e929d8cca49d6d44b62cd2bede5c
-- retrieved from ./1249507968/a04b2169abc1974cee1a27cc15be4c4f9ba60dc1/schema/spacewalk/oracle/procs/lookup_config_info.sql
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

create or replace function
lookup_config_info
(
    username_in     in varchar,
    groupname_in    in varchar,
    filemode_in     in numeric,
    selinux_ctx_in  in varchar
)
returns numeric
as
$$
declare
    r numeric;
    v_id    numeric;
    lookup_cursor cursor  for
        select id
          from rhnConfigInfo
         where username = username_in
           and groupname = groupname_in
           and filemode = filemode_in
           and nvl(selinux_ctx, ' ') = nvl(selinux_ctx_in, ' ');
begin
    for r in lookup_cursor loop
        return r.id;
    end loop;
    -- If we got here, we don't have the id
    select nextval('rhn_confinfo_id_seq') into v_id;
    insert into rhnConfigInfo
        (id, username, groupname, filemode, selinux_ctx)
    values (v_id, username_in, groupname_in, filemode_in, selinux_ctx_in);
    return v_id;
end;
$$ language plpgsql;
