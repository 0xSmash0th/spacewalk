-- oracle equivalent source sha1 71be2b50bd6bdff9c65510eb06c5c16555ca37c7
-- retrieved from ./1235066623/21f37df477f4c9a372b85916798c9ad2ff734e58/schema/spacewalk/rhnsat/views/rhnServerNeededPackageView.sql
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

CREATE OR REPLACE VIEW
rhnServerNeededPackageView
(
    org_id,
    server_id,
    errata_id,
    package_id,
    package_name_id
)
AS
SELECT   S.org_id,
         S.id,
	  (SELECT EP.errata_id
	     FROM rhnErrataPackage EP,
	          rhnChannelErrata CE,
		  rhnServerChannel SC
	    WHERE SC.server_id = S.id
	      AND SC.channel_id = CE.channel_id
	      AND CE.errata_id = EP.errata_id
	      AND EP.package_id = P.id
	    LIMIT 1),
	 P.id,
	 P.name_id
FROM
	 rhnPackage P,
	 rhnServerPackageArchCompat SPAC,
	 rhnPackageEVR P_EVR,
	 rhnPackageEVR SP_EVR,
	 rhnServerPackage SP,
	 rhnChannelPackage CP,
	 rhnServerChannel SC,
         rhnServer S
WHERE
    	 SC.server_id = S.id
  AND  	 SC.channel_id = CP.channel_id
  AND    CP.package_id = P.id
  AND    p.package_arch_id = spac.package_arch_id
  AND    spac.server_arch_id = s.server_arch_id
  AND    SP_EVR.id = SP.evr_id
  AND    P_EVR.id = P.evr_id
  AND    SP.server_id = S.id
  AND    SP.name_id = P.name_id
  AND    SP.evr_id != P.evr_id
  AND    SP_EVR.evr < P_EVR.evr
  AND    SP_EVR.evr = (SELECT MAX(PE.evr) FROM rhnServerPackage SP2, rhnPackageEvr PE WHERE PE.id = SP2.evr_id AND SP2.server_id = SP.server_id AND SP2.name_id = SP.name_id)
;

