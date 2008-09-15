-- created by Oraschemadoc Fri Jun 13 14:06:09 2008
-- visit http://www.yarpen.cz/oraschemadoc/ for more info

  CREATE OR REPLACE FORCE VIEW "RHNSAT"."RHNUSERAPPLETOVERVIEW" ("USER_ID", "SECURITY_COUNT", "BUG_COUNT", "ENHANCEMENT_COUNT") AS 
  SELECT      USP.user_id,
    	    SUM((SELECT COUNT(*) FROM rhnServerErrataTypeView SEV
	         WHERE SEV.server_id = USP.server_id
	         AND SEV.errata_type = 'Security Advisory')),
    	    SUM((SELECT COUNT(*) FROM rhnServerErrataTypeView SEV
	         WHERE SEV.server_id = USP.server_id
	         AND SEV.errata_type = 'Bug Fix Advisory')),
    	    SUM((SELECT COUNT(*) FROM rhnServerErrataTypeView SEV
	         WHERE SEV.server_id = USP.server_id
	         AND SEV.errata_type = 'Product Enhancement Advisory'))
FROM        rhnUserServerPerms USP
GROUP BY    USP.user_id
 
/
