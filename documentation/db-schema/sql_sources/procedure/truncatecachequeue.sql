-- created by Oraschemadoc Fri Jan 22 13:41:03 2010
-- visit http://www.yarpen.cz/oraschemadoc/ for more info

  CREATE OR REPLACE PROCEDURE "MIM_H1"."TRUNCATECACHEQUEUE" as
begin
  execute immediate 'Truncate Table rhnOrgErrataCacheQueue';
end;
 
/
