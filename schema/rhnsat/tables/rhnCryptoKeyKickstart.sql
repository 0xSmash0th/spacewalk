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
-- $Id$
--

create table
rhnCryptoKeyKickstart
(
	crypto_key_id	number
			constraint rhn_ckey_ks_ckid_nn not null
			constraint rhn_ckey_ks_ckid_fk
				references rhnCryptoKey(id)
				on delete cascade,
        ksdata_id number
                        constraint rhn_ckey_ks_ksd_nn not null
                        constraint rhn_ckey_ks_ksd_fk
                                references rhnKSData(id)
				on delete cascade
)
	storage( freelists 16 )
	enable row movement
	initrans 32;

create unique index rhn_ckey_ks_uq
	on rhnCryptoKeyKickstart(crypto_key_id, ksdata_id)
	tablespace [[4m_tbs]]
	storage( freelists 16 )
	initrans 32;

create index rhn_ckey_ks__ckuq
	on rhnCryptoKeyKickstart(ksdata_id, crypto_key_id)
	tablespace [[4m_tbs]]
	storage( freelists 16 )
	initrans 32;

-- $Log$
-- Revision 1.1  2003/11/15 20:28:24  cturner
-- bugzilla: 109898, schema to associate cryptokeys with kickstarts
--
