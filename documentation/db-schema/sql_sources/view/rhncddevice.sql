-- created by Oraschemadoc Fri Jan 22 13:40:41 2010
-- visit http://www.yarpen.cz/oraschemadoc/ for more info

  CREATE OR REPLACE FORCE VIEW "MIM_H1"."RHNCDDEVICE" ("ID", "SERVER_ID", "CLASS", "BUS", "DETACHED", "DEVICE", "DRIVER", "DESCRIPTION", "DEV_HOST", "DEV_ID", "DEV_CHANNEL", "DEV_LUN", "PCITYPE") AS 
  select
	d.id,
	d.server_Id,
	d.class,
	d.bus,
	d.detached,
	d.device,
	d.driver,
	d.description,
	d.prop1,
	d.prop2,
	d.prop3,
	d.prop4,
	d.pcitype
from rhndevice d
where d.class = 'CDROM'
 
/
