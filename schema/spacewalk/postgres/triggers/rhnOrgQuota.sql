-- oracle equivalent source sha1 5e066ec4afee03b340a9cb2636ae0a0f9e59114d
-- retrieved from ./1239053651/49a123cbe214299834e6ce97b10046d8d9c7642a/schema/spacewalk/oracle/triggers/rhnOrgQuota.sql
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

CREATE OR REPLACE FUNCTION rhn_orgquote_mod_trig_fun() RETURNS TRIGGER AS
$$
declare
	available_quota numeric;
begin
	new.modified := CURRENT_TIMESTAMP;

	available_quota := new.total + new.bonus;
	if new.used > available_quota then
		PERFORM rhn_exception.raise_exception('not_enough_quota');
	end if;

	RETURN new;
end;
$$ language plpgsql;

create trigger
rhn_orgquota_mod_trig
before insert or update on rhnOrgQuota
for each row
EXECUTE PROCEDURE rhn_orgquote_mod_trig_fun();

