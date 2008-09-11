-- $Id$

insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'noarch', 'noarch', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'i386', 'i386', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'i486', 'i486', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'i586', 'i586', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'i686', 'i686', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'alpha', 'alpha', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'alphaev6', 'alphaev6', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'ia64', 'ia64', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'sparc', 'sparc', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'sparcv9', 'sparcv9', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'sparc64', 'sparc64', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'src', 'src', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 's390', 's390', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'athlon', 'athlon', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 's390x', 's390x', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'ppc', 'ppc', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'ppc64', 'ppc64', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'pSeries', 'pSeries', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'iSeries', 'iSeries', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'x86_64', 'x86_64', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'ppc64iseries', 'ppc64iseries', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'ppc64pseries', 'ppc64pseries', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'sparc-solaris', 'Sparc Solaris', lookup_arch_type('sysv-solaris'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'sparc.sun4u-solaris', 'Sparc Solaris sun4u', lookup_arch_type('sysv-solaris'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'sparc.sun4v-solaris', 'Sparc Solaris sun4v', lookup_arch_type('sysv-solaris'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'tar', 'TAR archive', lookup_arch_type('tar'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'ia32e', 'EM64T', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'amd64', 'AMD64', lookup_arch_type('rpm'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'i386-solaris', 'i386-solaris', lookup_arch_type('sysv-solaris'));

insert into rhnPackageArch (id, label, name, arch_type_id) values 
( rhn_package_arch_id_seq.nextval, 'nosrc', 'nosrc', lookup_arch_type('rpm') );


insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'sparc-solaris-patch', 'Sparc Solaris patch', lookup_arch_type('solaris-patch'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'i386-solaris-patch', 'i386 Solaris patch', lookup_arch_type('solaris-patch'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'sparc-solaris-patch-cluster', 
'Sparc Solaris patch cluster', lookup_arch_type('solaris-patch-cluster'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'i386-solaris-patch-cluster', 
'i386 Solaris patch cluster', lookup_arch_type('solaris-patch-cluster'));
 
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'noarch-solaris', 'noarch-solaris', lookup_arch_type('sysv-solaris'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'noarch-solaris-patch', 'noarch-solaris-patch', lookup_arch_type('sysv-solaris'));
insert into rhnPackageArch (id, label, name, arch_type_id) values
(rhn_package_arch_id_seq.nextval, 'noarch-solaris-patch-cluster', 'noarch-solaris-patch-cluster', lookup_arch_type('sysv-solaris'));

commit;

-- $Log$
-- Revision 1.10  2004/05/11 18:29:40  pjones
-- bugzilla: none -- make EM64T and AMD64 registerable as such, and fix their
-- names while I'm at it.
--
-- Revision 1.9  2004/02/18 23:41:04  pjones
-- bugzilla: 116188 -- ia32e
--
-- Revision 1.8  2004/02/16 16:38:26  rnorwood
-- bugzilla: 115111 - package arch data for solaris-patch-cluster.
--
-- Revision 1.7  2004/02/13 14:38:50  misa
-- bugzilla: 115516  Arches and compat stuff for solaris patches
--
-- Revision 1.6  2004/02/06 02:21:16  misa
-- Weird solaris arches added
--
-- Revision 1.5  2004/02/05 17:33:12  pjones
-- bugzilla: 115009 -- rhnArchType is new, and has changes to go with it
--
-- Revision 1.4  2003/06/09 18:16:04  misa
-- bugzilla: 86150  Added the ppc64iseries and ppc64pseries arches, plus the channel-ppc channel arch
--
-- Revision 1.3  2003/01/29 17:11:36  misa
-- bugzilla: 83022  Adding x86_64 as a supported arch
--
-- Revision 1.2  2002/11/14 18:00:59  pjones
-- commits
--
-- Revision 1.1  2002/11/13 23:42:28  misa
-- Sequence; data to populate stuff
--

