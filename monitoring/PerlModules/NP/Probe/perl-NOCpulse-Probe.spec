%define npbin        /opt/home/nocpulse/bin
Name:         perl-NOCpulse-Probe
Summary:      Probe execution framework
URL:          https://fedorahosted.org/spacewalk
Source0:      https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
Version:      1.183.4
Release:      1%{?dist}
BuildArch:    noarch
Group:        Development/Libraries
Requires:     perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
License:      GPLv2
Buildroot:    %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Requires(pre): nocpulse-common

%description
NOCpulse provides application, network, systems and transaction monitoring,
coupled with a comprehensive reporting system including availability,
historical and trending reports in an easy-to-use browser interface.

This package provides classes for executing probes.

%prep
%setup -q

%build
#Nothing to build

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Config/test
mkdir -p $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/DataSource/test
mkdir -p $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Shell/test
mkdir -p $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Utils/test
mkdir -p $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/SNMP/test
mkdir -p $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/test

install -m 755 -D rhn-runprobe $RPM_BUILD_ROOT/%{_bindir}/rhn-runprobe
install -m 644 Config/*.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Config/
install -m 644 Config/test/*.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Config/test/
install -m 644 DataSource/*.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/DataSource/
install -m 644 DataSource/test/*.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/DataSource/test/
install -m 644 SNMP/*.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/SNMP/
install -m 644 SNMP/test/*.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/SNMP/test/
install -m 644 Shell/*.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Shell/
install -m 644 Shell/test/*.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Shell/test/
install -m 644 Utils/*.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Utils/
install -m 644 Utils/test/*.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Utils/test/
install -m 644 *.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/
install -m 644 test/*.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/test/

mkdir -p $RPM_BUILD_ROOT%{_mandir}/man3
/usr/bin/pod2man $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/ItemStatus.pm |gzip > $RPM_BUILD_ROOT%{_mandir}/man3/NOCpulse::Probe::ItemStatus.3pm.gz
/usr/bin/pod2man $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/DataSource/MySQL.pm |gzip > $RPM_BUILD_ROOT%{_mandir}/man3/NOCpulse::Probe::DataSource::MySQL.3pm.gz
/usr/bin/pod2man $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/DataSource::NetworkServiceCommands.pm |gzip > $RPM_BUILD_ROOT%{_mandir}/man3/NOCpulse::Probe::DataSource::NetworkServiceCommands.3pm.gz
/usr/bin/pod2man $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Result.pm |gzip > $RPM_BUILD_ROOT%{_mandir}/man3/NOCpulse::Probe::Result.3pm.gz
/usr/bin/pod2man $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Shell/AbstractShell.pm |gzip > $RPM_BUILD_ROOT%{_mandir}/man3/NOCpulse::Probe::Shell::AbstractShell.3pm.gz
/usr/bin/pod2man $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Shell/SSH.pm |gzip > $RPM_BUILD_ROOT%{_mandir}/man3/NOCpulse::Probe::Shell::SSH.3pm.gz
/usr/bin/pod2man $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Shell/SQLPLus.pm |gzip > $RPM_BUILD_ROOT%{_mandir}/man3/NOCpulse::Probe::Shell::SQLPLus.3pm.gz
/usr/bin/pod2man $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Shell/Local.pm |gzip > $RPM_BUILD_ROOT%{_mandir}/man3/NOCpulse::Probe::Shell::Local.3pm.gz
/usr/bin/pod2man $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/Probe/Shell/Unix.pm |gzip > $RPM_BUILD_ROOT%{_mandir}/man3/NOCpulse::Probe::Shell::Unix.3pm.gz

%clean
rm -rf $RPM_BUILD_ROOT

%files 
%defattr(-,root,root,-)
%{_bindir}/rhn-runprobe
%{perl_vendorlib}/NOCpulse/*
%{_mandir}/man3/*

%changelog
* Tue Dec 16 2008 Miroslav Suchý <msuchy@redhat.com> 1.183.4-1
- 472895 - remove grouped_fields from Class::MethodMaker declaration

* Mon Oct 20 2008 Miroslav Suchý <msuchy@redhat.com> 1.183.3-1
- 467441 - fix namespace

* Tue Sep  2 2008 Miroslav Suchý <msuchy@redhat.com> 1.183.2-1
- edit spec to comply with Fedora guidelines

* Thu Jun 19 2008 Miroslav Suchy <msuchy@redhat.com>
- migrating nocpulse home dir (BZ 202614)

* Wed Jun  4 2008 Milan Zazrivec <mzazrivec@redhat.com> 1.183.1-22
- fixed file permissions

* Wed May 28 2008 Jan Pazdziora 1.183.1-21
- rebuild in dist-cvs
