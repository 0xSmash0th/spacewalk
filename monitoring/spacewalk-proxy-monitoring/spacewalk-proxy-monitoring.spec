
Summary:      Meta-package that pulls in all of the Spacewalk monitoring packages
Name:         spacewalk-proxy-monitoring
Source0:      %{name}-%{version}.tar.gz
Version:      0.4.1
Release:      1%{?dist}
# This src.rpm is cannonical upstream
# You can obtain it using this set of commands
# git clone git://git.fedorahosted.org/git/spacewalk.git/
# cd monitoring/spacewalk-proxy-monitoring
# make srpm
URL:          https://fedorahosted.org/spacewalk
License:      GPLv2
Group:        Applications/System
BuildArch:    noarch
Obsoletes:    rhns-proxy-monitoring <= 5.2.0
Provides:     rhns-proxy-monitoring
Conflicts: rhnmd
Requires: nocpulse-db-perl 
Requires: eventReceivers 
Requires: MessageQueue 
Requires: NOCpulsePlugins 
Requires: NPalert 
Requires: nocpulse-common 
Requires: perl-NOCpulse-CLAC 
Requires: perl-NOCpulse-Debug 
Requires: perl-NOCpulse-Gritch 
Requires: perl-NOCpulse-Object 
Requires: perl-NOCpulse-OracleDB 
Requires: perl-NOCpulse-PersistentConnection 
Requires: perl-NOCpulse-Probe 
Requires: perl-NOCpulse-ProcessPool 
Requires: perl-NOCpulse-Scheduler 
Requires: perl-NOCpulse-SetID 
Requires: perl-NOCpulse-Utils 
Requires: ProgAGoGo 
Requires: SatConfig-bootstrap 
Requires: SatConfig-bootstrap-server 
Requires: SatConfig-cluster 
Requires: SatConfig-dbsynch 
Requires: SatConfig-general 
Requires: SatConfig-generator 
Requires: SatConfig-installer 
Requires: SatConfig-spread 
Requires: scdb 
Requires: SNMPAlerts 
Requires: SputLite-client 
Requires: SputLite-server 
Requires: ssl_bridge 
Requires: status_log_acceptor 
Requires: tsdb 
Requires: mod_perl
Buildroot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description
NOCpulse provides application, network, systems and transaction monitoring,
coupled with a comprehensive reporting system including availability,
historical and trending reports in an easy-to-use browser interface.

This package pulls in all of the Spacewalk Monitoring packages, including all 
MOC and Scout functionality.

%prep
%setup -q

%build
# nothing to do

%install
rm -Rf $RPM_BUILD_ROOT

#/etc/satname needs to be created on the proxy box, with the contents of '1'       
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}
install satname $RPM_BUILD_ROOT%{_sysconfdir}/satname

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-, root,root,-)
%config %{_sysconfdir}/satname
%doc README

%changelog
* Mon Sep 29 2008 Miroslav Suchý <msuchy@redhat.com> 0.3.2-1
- rename oracle_perl to nocpulse-db-perl

* Thu Sep 25 2008 Miroslav Suchy <msuchy@redhat.com>
- removed nslogs

* Fri Sep 12 2008 Miroslav Suchy <msuchy@redhat.com> 0.3.1-1
- removed ConfigPusher-general
- renamed to spacewalk-proxy-monitoring
- clean up to comply with Fedora Guidelines
- add documentation

* Thu Jul  3 2008 Milan Zazrivec <mzazrivec@redhat.com> 5.2.0-2
- removed dependencies on FcntlLock, SatConfig-ApacheDepot, scdb_accessor_perl,
  Time-System, tsdb_accessor_perl
* Tue Jun 17 2008 Milan Zazrivec <mzazrivec@redhat.com> 5.2.0-1
- cvs.dist import
- rhns-proxy-monitoring does not depend on bdb_perl anymore (bz #450687)
* Fri Oct 12 2007 Miroslav Suchy <msuchy@redhat.com>
- modified for modperl2
* Thu Jun 09 2005 Nick Hansen <nhansen@redhat.com>
- added a conflict on rhnmd and specified noarch
* Mon May 23 2005 Nick Hansen <nhansen@redhat.com>
- Bumped version to 4.0.0
* Wed Oct 20 2004 Nick Hansen <nhansen@redhat.com>
- added the /etc/satname file for creation on scout-on-proxy systems
  and added a version file
* Mon Oct 18 2004 Nick Hansen <nhansen@redhat.com>
- dropped the timesync-server and timesync-client packages 
* Sun Oct 17 2004 Nick Hansen <nhansen@redhat.com>
- upped version to match the RHN release in which this will go out 
* Wed Sep 22 2004 Mihai Ibanescu <misa@redhat.com>
- Initial build
