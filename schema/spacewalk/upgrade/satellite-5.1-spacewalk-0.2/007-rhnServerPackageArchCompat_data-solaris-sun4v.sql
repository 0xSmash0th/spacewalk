insert into rhnServerPackageArchCompat
(server_arch_id, package_arch_id, preference) values
(LOOKUP_SERVER_ARCH('sparc-sun4v-solaris'), LOOKUP_PACKAGE_ARCH('sparc.sun4v-solaris'), 10);

insert into rhnServerPackageArchCompat
(server_arch_id, package_arch_id, preference) values
(LOOKUP_SERVER_ARCH('sparc-sun4v-solaris'), LOOKUP_PACKAGE_ARCH('sparc-solaris'), 100);

insert into rhnServerPackageArchCompat
(server_arch_id, package_arch_id, preference) values
(LOOKUP_SERVER_ARCH('sparc-sun4v-solaris'), LOOKUP_PACKAGE_ARCH('sparc-solaris-patch'), 210);

insert into rhnServerPackageArchCompat
(server_arch_id, package_arch_id, preference) values
(LOOKUP_SERVER_ARCH('sparc-sun4v-solaris'), LOOKUP_PACKAGE_ARCH('sparc-solaris-patch-cluster'), 310);

insert into rhnServerPackageArchCompat
(server_arch_id, package_arch_id, preference) values
(LOOKUP_SERVER_ARCH('sparc-sun4v-solaris'), LOOKUP_PACKAGE_ARCH('noarch-solaris'), 410);

insert into rhnServerPackageArchCompat
(server_arch_id, package_arch_id, preference) values
(LOOKUP_SERVER_ARCH('sparc-sun4v-solaris'), LOOKUP_PACKAGE_ARCH('noarch-solaris-patch'), 510);

insert into rhnServerPackageArchCompat
(server_arch_id, package_arch_id, preference) values
(LOOKUP_SERVER_ARCH('sparc-sun4v-solaris'), LOOKUP_PACKAGE_ARCH('noarch-solaris-patch-cluster'), 610);
