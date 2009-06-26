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
-- The types of checksum associated with a package... md5sum, sha1, sha256...?

create table
rhnPackageChecksum
(
        package_id         number
                           constraint rhn_pkgcs_nn not null
                           constraint rhn_pkgcs_id_fk
                                  references rhnPackage(id),
        checksum_type_id   number
                           constraint rhn_pkg_checksum_type_nn not null
                           constraint rhn_pkg_checksum_type_id_fk
                                 references rhnChecksumType(id),
        checksum           varchar2(128)
                           constraint rhn_pkg_checksum_nn not null
)
        enable row movement
  ;

create index rhn_pkgcsum_id_n_idx
        on rhnPackageChecksum (package_id, checksum_type_id)
        tablespace [[64k_tbs]];

alter table rhnPackageChecksum add constraint rhn_pkgcs_id_pk primary key ( package_id);
alter table rhnPackageChecksum add constraint rhn_pkgcs_id_uq unique ( package_id, checksum_type_id);

--
-- Revision 1.1  2009/06/26 10:37:16  pkilambi
-- 
