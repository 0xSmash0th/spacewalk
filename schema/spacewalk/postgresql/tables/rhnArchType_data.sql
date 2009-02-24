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
insert into rhnArchType (id, label, name) values
	(rhn_archtype_id_seq.nextval, 'rpm', 'RPM');
insert into rhnArchType (id, label, name) values
	(rhn_archtype_id_seq.nextval, 'sysv-solaris', 'SysV-Solaris');
insert into rhnArchType (id, label, name) values
	(rhn_archtype_id_seq.nextval, 'tar', 'tar');
insert into rhnArchType (id, label, name) values
	(rhn_archtype_id_seq.nextval, 'solaris-patch', 'Solaris Patch');
insert into rhnArchType (id, label, name) values
	(rhn_archtype_id_Seq.nextval, 'solaris-patch-cluster',
		'Solaris Patch Cluster');

