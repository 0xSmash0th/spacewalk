Name:           spacewalk-schema
Group:          Applications/Internet
Summary:        Oracle SQL schema for Spacewalk server.

Version:        0.4.14
Release:        1%{?dist}
Source0:        %{name}-%{version}.tar.gz

License:        GPLv2
Url:            http://fedorahosted.org/spacewalk/
BuildArch:      noarch
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

Obsoletes:	rhn-satellite-schema <= 5.1.0


%define rhnroot /etc/sysconfig/rhn/
%define universe universe.satellite.sql

%description
rhn-satellite-schema is the Oracle SQL schema for the Spacewalk server.
Oracle tablespace name conversions have NOT been applied.

%prep

%setup -q

%build
SCHEMA_VER=$(echo %{version} | sed 's/%{?dist}$//')
make -f Makefile.schema \
  UNIVERSE=%{universe} TOP=. SCHEMA=%{name} VERSION=$SCHEMA_VER RELEASE=%{release} \
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
* Tue Jan 13 2009 Miroslav Suchý <msuchy@redhat.com> 0.4.14-1
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

