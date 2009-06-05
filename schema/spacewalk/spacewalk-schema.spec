Name:           spacewalk-schema
Group:          Applications/Internet
Summary:        Oracle SQL schema for Spacewalk server

Version:        0.6.9
Release:        1%{?dist}
Source0:        %{name}-%{version}.tar.gz

License:        GPLv2
Url:            http://fedorahosted.org/spacewalk/
BuildArch:      noarch
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

Obsoletes:      rhn-satellite-schema <= 5.1.0


%define rhnroot /etc/sysconfig/rhn/
%define universe universe.satellite.sql

%description
rhn-satellite-schema is the Oracle SQL schema for the Spacewalk server.
Oracle tablespace name conversions have NOT been applied.

%prep

%setup -q

%build
make -f Makefile.schema \
  UNIVERSE=%{universe} TOP=. SCHEMA=%{name} VERSION=%{version} RELEASE=%{release} \
  all
pod2man spacewalk-schema-upgrade spacewalk-schema-upgrade.1

%install
rm -rf $RPM_BUILD_ROOT
install -m 0755 -d $RPM_BUILD_ROOT%{rhnroot}
install -m 0644 %{universe} $RPM_BUILD_ROOT%{rhnroot}
install -m 0755 -d $RPM_BUILD_ROOT%{_bindir}
install -m 0755 %{name}-upgrade $RPM_BUILD_ROOT%{_bindir}
install -m 0755 -d $RPM_BUILD_ROOT%{rhnroot}/schema-upgrade
tar cf - -C upgrade . | tar xf - -C $RPM_BUILD_ROOT%{rhnroot}/schema-upgrade
mkdir -p $RPM_BUILD_ROOT%{_mandir}/man1
cp -p spacewalk-schema-upgrade.1 $RPM_BUILD_ROOT%{_mandir}/man1

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
%{rhnroot}/*
%{_bindir}/%{name}-upgrade
%{_mandir}/man1/spacewalk-schema-upgrade*

%changelog
* Fri Jun 05 2009 jesus m. rodriguez <jesusr@redhat.com> 0.6.9-1
- 503243 - Dropping the is_default column as we now determine the default in
  the app code based on the compatible eus channel instead of jus the default.
  (pkilambi@redhat.com)
- no need to add whitespace to upgrade script (mzazrivec@redhat.com)
- monitoring log files belong to /var/log/nocpulse (mzazrivec@redhat.com)
- 502641 - renaming upgrade script to 119-rhnSystemMigrations.sql
  (bbuckingham@redhat.com)
- 502641 - rhnSystemMigrations remove not null constraints from to/from org ids
  (bbuckingham@redhat.com)
- 498467 - A few changes related to the channel name limit increase.
  (jason.dobies@redhat.com)

* Tue May 26 2009 Devan Goodwin <dgoodwin@redhat.com> 0.6.8-1
- 501389 - splitting up virt types none and kvm guests, as well as improving
  virt type names (jsherril@redhat.com)

* Mon May 25 2009 Miroslav Suchy <msuchy@redhat.com> 0.6.7-1
- 502476 - rhn_sat_node should have uniq constraint on column server_id

* Mon May 18 2009 Jan Pazdziora 0.6.6-1
- spacewalk-schema-upgrade: add support for reference files
- 498467 - Forgot to update the create scripts with the new column length
  (jason.dobies@redhat.com)
- Merge branch 'bugs' (jason.dobies@redhat.com)
- 498467 - Increased size of channel name column (jason.dobies@redhat.com)

* Wed May 06 2009 jesus m. rodriguez <jesusr@redhat.com> 0.6.5-1
- 499046 - making it so that pre/post scripts can be templatized or not,
  defaulting to not (jsherril@redhat.com)
- alter index needs to be run via execute immediate (mzazrivec@redhat.com)
- 461704 - clean time_series when deleting a server (upgrade script)
  (mzazrivec@redhat.com)
- 461704 - clear time_series when deleting server (mzazrivec@redhat.com)
- 496174 - upgrade to view (mmccune@gmail.com)
- 496174 - view optimization. (mmccune@gmail.com)

* Fri Apr 24 2009 Jan Pazdziora 0.6.4-1
- 497477 - add function based index on time_series for faster probe_id lookups,
  use hint in delete to use it

* Wed Apr 22 2009 jesus m. rodriguez <jesusr@redhat.com> 0.6.3-1
- 494976 - adding cobbler systme record name usage to reprovisioning (jsherril@redhat.com)

* Tue Apr 21 2009 Jan Pazdziora 0.6.2-1
- spacewalk-schema-upgrade: other stability and code cleanup changes
- 495869 - label the /var/log/spacewalk/schema-upgrade based on the SELinux
  policy
- set noparallel if index rhn_snc_speid_idx exists (mzazrivec@redhat.com)
- spacewalk-schema-upgrade: for upgrades from Satellite 5.3.0 up, the starting
  schema name is satellite-schema
- 487319 - restore text input for SNMP Community String field
  (mzazrivec@redhat.com)
- 487319 - text input for "SNMP Community String" field (mzazrivec@redhat.com)

* Wed Apr 15 2009 Devan Goodwin <dgoodwin@redhat.com> 0.6.1-1
- 495133 - fixing errata mailer such that mails are only sent for a particular
  channel that was changed (jsherril@redhat.com)
- fix ORA-00955 when creating RHN_SNC_SPEID_IDX (mzazrivec@redhat.com)
- 149695 - Including channel_id as part of rhnErrataQueue table so that
  taskomatic can send errata notifications based on channel_id instead of
  sending to everyone subscribed to the channel. The changes include db change
  to rhnErrataQueue table and backend change to satellite-sync's errata import.
  (pkilambi@redhat.com)
- 485870 - only recalculate the channel family counts once per family.
  (mmccune@gmail.com)
- 494475,460136 - remove faq & feedback code which used customerservice emails.
  (jesusr@redhat.com)
- fixing some index mixups, nologging was left off the base schema for a few
  indexes for 0.5 (jsherril@redhat.com)
- 480060 - schema changes to support errata list enhancements.  Simple
  (mmccune@gmail.com)
- bump Versions to 0.6.0 (jesusr@redhat.com)
- adding missing index for errata cache\ (jsherril@redhat.com)

* Wed Mar 25 2009 Mike McCune <mmccune@gmail.com> 0.5.20-1
-  472595 - forgot the index in the table definition.  was in the upgrade area only

* Thu Mar 19 2009 jesus m. rodriguez <jesusr@redhat.com> 0.5.19-1
- 487316 - disallows multiple eus channels to be considered the most recent
- 472595 - fixes for kickstart performance, start porting ks downloads to java
- update rhnUserInfo page_size to 25

* Wed Mar 11 2009 Milan Zazrivec <mzazrivec@redhat.com> 0.5.17-1
- fix typo in index name (upgrade script)
- add sql upgrade part for 466035

* Tue Mar 10 2009 Milan Zazrivec <mzazrivec@redhat.com> 0.5.15-1
- add missing ';' when creating web_user_site_info_wuid

* Mon Mar  9 2009 Milan Zazrivec <mzazrivec@redhat.com> 0.5.14-1
- fix upgrade script dropping parallel query

* Mon Mar 09 2009 Michael Mraka <michael.mraka@redhat.com> 0.5.13-1
- fixed #489319

* Thu Mar 05 2009 jesus m. rodriguez <jesusr@redhat.com> 0.5.12-1
- fixing upgrade script to properly update all dependant tables
- Fix bug 474597, schema updated but upgrade script not included.

* Thu Feb 26 2009 jesus m. rodriguez <jesusr@redhat.com> 0.5.11-1
- fix comment to avoid confusion with / symbol

* Thu Feb 19 2009 Devan Goodwin <dgoodwin@redhat.com> 0.5.10-1
- 486254 - Fix broken schema population during spacewalk-setup.

* Wed Feb 18 2009 Pradeep Kilambi 0.5.9-1
- minor typo and dep fixes for rhnRepoRegenQueue table 

* Mon Feb 16 2009 Pradeep Kilambi 0.5.8-1
- rhnRepoRegenQueue table for yum repodata regen queue

* Thu Feb 12 2009 Mike McCune <mmccune@gmail.com> 0.5.7-1
- 484312 - massive cleanup of virt types.  getting rid of useless AUTO type.

* Thu Feb 12 2009 jesus m. rodriguez <jesusr@redhat.com> 0.5.6-1
- 484964 - increasing the copyright column size

* Thu Feb 12 2009 Miroslav Suchý <msuchy@redhat.com> 0.5.5-1
- move logs from /var/tmp to /var/log/nocpulse

* Wed Feb 11 2009 Milan Zazrivec 0.5.4-1
- fixed multiorg sql upgrade script

* Thu Feb 05 2009 jesus m. rodriguez <jesusr@redhat.com> 0.5.3-1
- 443718 - fixing a view mistage and having a query just use the view
- 443718 - improving errata cache calcs when pushing a single errata
- 481671 - rewrote inner query to improve performance.
- 480671 fix for deleting orgs in multiorg env
- fixing some forgotten indexes
- a few schema fixes and test case fixes related to the errata-cache update
- fixing a few test cases
- renaming upgrade script
- upgrade support for multiorg sharing logic
- validate channel is 'protected' when joining to the rhnChannelTrusts table.

* Fri Jan 23 2009 Jan Pazdziora 0.5.2-1
- fix for ORA-01440 error occurring when updating populated table (Michael M.)
- removed s/%{?dist}// substitution with no effect (Milan Z.)
- spacewalk-schema-upgrade: minor cleanup
- spacewalk-schema-upgrade: add support for schema overrides

* Wed Jan 14 2009 Mike McCune <mmccune@gmail.com> 0.4.17-1
- 461162 - correcting to match upgrade scripts

* Wed Jan 14 2009 Milan Zazrivec 0.4.16-1
- fixes for #479950 - spacewalk 0.4: new and upgraded schemas do not match

* Tue Jan 13 2009 Miroslav Suchý <msuchy@redhat.com> 0.4.15-1
- 479837 - Support rpm, which contains files with filename + path longer than 256 chars

* Tue Jan 13 2009 Milan Zazrivec 0.4.13-1
- 461162 - cleanup dead code in systemmanager and add new distro types
- 461162 - more virt-type fixing
- 461162 - get the virtualization provisioning tracking system to work with a :virt system record
- 476730 - increase advisory column to 37

* Wed Jan  7 2009 Milan Zazrivec 0.4.12-1
- added spacewalk-schema-upgrade manual page (bz #479003)
- renamed two sql upgrade scripts to use uniform extension

* Thu Dec 18 2008 Milan Zazrivec <mzazrivec@redhat.com> 0.4.11-1
- fixed duplicate modification of rhnChannel

* Thu Dec 18 2008 Michael Mraka <michael.mraka@redhat.com> 0.4.10-1
- 476644 - fixed rhn_org.delete_user

* Mon Dec 15 2008 Jan Pazdziora 0.4.9-1
- 461162 - adding virt options for cobbler
- 461162 - add type 'auto' virtualization
- remove vmware choice from vm type list
- drop also unique index associated with constraint
- updated the alter script numbers to have things go sequential
- 456532 - initial changes to stored profiles to support multiarch
- initial commit for stored profiles to support multiarch
- fixed upgrade script to upgrade the constraint as well
- added new rhnSet column to clean SQL create scripts
- added upgrade scripts to add necessary column to rhnSet
- updates to rhnRegTokenPackages to include id as primary key as well as sequence for generating ids
- making the arch_id column nullable
- updating rhnRegTokenPackages to include arch_id column
- 461162 - initial commit of the manager layer for cobbler
- changes by multiple authors

* Fri Dec  5 2008 Miroslav Suchý <msuchy@redhat.com> 0.4.8-1
- fix monitoring paths in schema

* Mon Dec  1 2008 Miroslav Suchý <msuchy@redhat.com> 0.4.6-1
- 472910 - fix paths to nofitication configs

* Thu Nov 27 2008 Miroslav Suchy <msuchy@redhat.com> 0.4.5-1
- 473242 - fix paths for alert_queue and ack_queue

* Wed Nov 26 2008 Miroslav Suchy <msuchy@redhat.com> 0.4.4-1
- 473097 - point monitoring paths to new destination

* Fri Nov 21 2008 Michael Mraka <michael.mraka@redhat.com> 0.4.3-1
- resolved #471199 - performance improvement of delete_server

* Fri Oct 31 2008 Miroslav Suchy <msuchy@redhat.com> 0.3.5-1
- 469244 - remove trailing /

* Thu Oct 23 2008 Jan Pazdziora 0.3.4-1
- release containing multiple contributions:
- removed rhn_clean_current_state, is_user_org_admin
- moved /opt dir to proper location
- removed unused $Id$, $id$, and $Log$ in the schema
- removed unused macros from table SQL
- rhn_channel_cloned_comps_trig depends on rhnChannelComps
- changed mode of spacewalk-schema-upgrade
- spacewalk-schema-upgrade: require confirming Enter
- 468016 - remove orphaned rhn_contact_groups in rhn_org.delete_user

* Tue Sep 23 2008 Milan Zazrivec 0.3.3-1
- fixed package obsoletes

* Thu Sep 18 2008 Devan Goodwin <dgoodwin@redhat.com> 0.3.2-1
- Fix bug with bad /var/log/rhn/ permissions.

* Thu Sep 18 2008 Michael Mraka <michael.mraka@redhat.com> 0.2.5-1
- Added upgrade scripts

* Wed Sep 17 2008 Devan Goodwin <dgoodwin@redhat.com> 0.3.1-1
- Bumping version to 0.3.x.

* Wed Sep 10 2008 Milan Zazrivec 0.2.3-1
- fixed package obsoletes

* Tue Sep  2 2008 Devan Goodwin <dgoodwin@redhat.com> 0.2.2-1
- Adding new kickstart profile options.

* Mon Sep  1 2008 Milan Zazrivec <mzazrivec@redhat.com> 0.2.1-1
- bumping version for spacewalk 0.2

* Tue Aug  5 2008 Michael Mraka <michael.mraka@redhat.com> 0.1.0-2
- renamed from rhn-satellite-schema and changed version

* Mon Jun  9 2008 Michael Mraka <michael.mraka@redhat.com> 5.2.0-2
- fixed build issue

* Tue Jun  3 2008 Michael Mraka <michael.mraka@redhat.com> 5.2.0-1
- purged unused code
- rebuilt via brew / dist-cvs

