-- oracle equivalent source sha1 791c0067198055114ca9fdf61f4a6a6d66f95b7d
-- retrieved from ./1241042199/53fa26df463811901487b608eecc3f77ca7783a1/schema/spacewalk/oracle/procs/lookup_package_name.sql
--
-- Copyright (c) 2008--2010 Red Hat, Inc.
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


CREATE OR REPLACE FUNCTION
LOOKUP_PACKAGE_NAME(name_in IN VARCHAR, ignore_null in NUMERIC DEFAULT 0)
RETURNS NUMERIC
AS
$$
DECLARE
        name_id         NUMERIC;
BEGIN
        if ignore_null = 1 and name_in is null then
                return null;
        end if;

        SELECT id
          INTO name_id
          FROM rhnPackageName
         WHERE name = name_in;

         IF NOT FOUND THEN
		INSERT INTO rhnPackageName (id, name) VALUES (nextval('rhn_pkg_name_seq'), name_in);
		name_id := currval('rhn_pkg_name_seq');
         
         END IF;

        RETURN name_id;
END;
$$ LANGUAGE PLPGSQL;
