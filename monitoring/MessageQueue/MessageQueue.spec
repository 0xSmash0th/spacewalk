%define startup_root   %{_sysconfdir}/rc.d
%define queue_dir      %{_var}/lib/nocpulse/queue
%define notif_qdir     %queue_dir/notif
%define states_qdir    %queue_dir/sc_db
%define trends_qdir    %queue_dir/ts_db
%define commands_qdir  %queue_dir/commands
%define snmp_qdir      %queue_dir/snmp

Name:         MessageQueue
Version:      3.26.0
Release:      6%{?dist}
Summary:      Message buffer/relay system
# This src.rpm is cannonical upstream
# You can obtain it using this set of commands
# git clone git://git.fedorahosted.org/git/spacewalk.git/
# cd monitoring/MessageQueue
# make srpm
URL:          https://fedorahosted.org/spacewalk
Source0:      %{name}-%{version}.tar.gz
BuildArch:    noarch
Requires:     perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
Requires:     ProgAGoGo nocpulse-common
Group:        Applications/Communications
License:      GPLv2
Buildroot:    %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description
MessageQueue is a mechanism by which Spacewalk plugins and event handlers
can safely and quickly buffer outbound messages. The system provides
a dequeue daemon that reliably dequeues messages to internal systems.

%prep
%setup -q

%build
#Nothing to build

%install
rm -rf $RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse
mkdir -p $RPM_BUILD_ROOT%{_bindir}
mkdir -p $RPM_BUILD_ROOT%notif_qdir
mkdir -p $RPM_BUILD_ROOT%states_qdir
mkdir -p $RPM_BUILD_ROOT%trends_qdir
mkdir -p $RPM_BUILD_ROOT%commands_qdir
mkdir -p $RPM_BUILD_ROOT%snmp_qdir

# Install libraries
install	-m 644 *.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/

# Install binaries
install -m 755 dequeue $RPM_BUILD_ROOT%{_bindir}

# stuff needing special ownership doesn't go in filelist
install -m 755 queuetool $RPM_BUILD_ROOT%{_bindir}

%files
%defattr(-,root,root,-)
%attr(755,nocpulse,nocpulse) %dir %queue_dir
%attr(755,nocpulse,nocpulse) %dir %states_qdir
%attr(755,nocpulse,nocpulse) %dir %notif_qdir
%attr(755,nocpulse,nocpulse) %dir %trends_qdir
%attr(755,nocpulse,nocpulse) %dir %commands_qdir
%attr(755,nocpulse,nocpulse) %dir %snmp_qdir
%{_bindir}/queuetool
%{_bindir}/dequeue
%{perl_vendorlib}/NOCpulse/*

%clean
rm -rf $RPM_BUILD_ROOT

%changelog
* Mon Sep 29 2008 Miroslav Suchý <msuchy@redhat.com> 
- spec cleanup for Fedora

* Thu Jun 19 2008 Miroslav Suchy <msuchy@redhat.com>
- migrating nocpulse home dir (BZ 202614)

* Wed Jun  4 2008 Milan Zazrivec <mzazrivec@redhat.com> 3.26.0-6
- fixed file permissions

* Thu May 29 2008 Jan Pazdziora 3.26.0-5
- rebuild in dist.cvs

