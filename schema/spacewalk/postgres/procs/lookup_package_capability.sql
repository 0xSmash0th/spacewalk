-- oracle equivalent source sha1 a234e21a0def0d24de24d91a12fcbc9bca9ac22a
-- retrieved from ./1241042199/53fa26df463811901487b608eecc3f77ca7783a1/schema/spacewalk/oracle/procs/lookup_package_capability.sql
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
LOOKUP_PACKAGE_CAPABILITY(name_in IN VARCHAR,
    version_in IN VARCHAR DEFAULT NULL)
RETURNS NUMERIC
AS
$$
DECLARE
      name_id         NUMERIC;
BEGIN
        IF version_in IS NULL THEN
                SELECT id
                  INTO name_id
                  FROM rhnPackageCapability
                 WHERE name = name_in
                   AND version IS NULL;
        ELSE
                SELECT id
                  INTO name_id
                  FROM rhnPackageCapability
                 WHERE name = name_in
                   AND version = version_in;
        END IF;

        IF NOT FOUND THEN
		INSERT INTO rhnPackageCapability (id, name, version) VALUES (nextval('rhn_pkg_capability_id_seq'), name_in, version_in);
		name_id := currval('rhn_pkg_capability_id_seq');
        END IF;
        
        RETURN name_id;
END;
$$
LANGUAGE PLPGSQL;
