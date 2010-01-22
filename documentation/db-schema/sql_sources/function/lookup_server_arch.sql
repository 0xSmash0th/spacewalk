-- created by Oraschemadoc Fri Jan 22 13:41:04 2010
-- visit http://www.yarpen.cz/oraschemadoc/ for more info

  CREATE OR REPLACE FUNCTION "MIM_H1"."LOOKUP_SERVER_ARCH" (label_in IN VARCHAR2)
RETURN NUMBER
IS
	server_arch_id		NUMBER;
BEGIN
	SELECT id
          INTO server_arch_id
          FROM rhnServerArch
         WHERE label = label_in;

	RETURN server_arch_id;
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            rhn_exception.raise_exception('server_arch_not_found');
END;
 
/
