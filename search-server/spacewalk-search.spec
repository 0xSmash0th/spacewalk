%{!?__redhat_release:%define __redhat_release UNKNOWN}

Name: spacewalk-search
Summary: Spacewalk Full Text Search Server
Group: Applications/Internet
License: GPLv2
Version: 0.3.2
Release: 1%{?dist}
# This src.rpm is cannonical upstream
# You can obtain it using this set of commands
# git clone git://git.fedorahosted.org/git/spacewalk.git/
# cd search-server
# make test-srpm
URL: https://fedorahosted.org/spacewalk
Source0: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch: noarch

Group: Applications/Internet
#Requires: apache-ibatis-sqlmap
Requires: jakarta-commons-cli
Requires: jakarta-commons-codec
Requires: jakarta-commons-httpclient
Requires: jakarta-commons-lang >= 0:2.1
Requires: jakarta-commons-logging
Requires: jpackage-utils >= 0:1.5
Requires: log4j
Requires: oro
#Requires: lucene
Requires: quartz
Requires: redstone-xmlrpc
#Requires: picocontainer
Requires: tanukiwrapper
Obsoletes: rhn-search <= 0.1
BuildRequires: ant
#BuildRequires: apache-ibatis-sqlmap
BuildRequires: jakarta-commons-cli
BuildRequires: jakarta-commons-codec
BuildRequires: jakarta-commons-httpclient
BuildRequires: jakarta-commons-lang >= 0:2.1
BuildRequires: jakarta-commons-logging
BuildRequires: java-devel >= 1.5.0
BuildRequires: log4j
BuildRequires: oro
#BuildRequires: lucene
BuildRequires: quartz
BuildRequires: redstone-xmlrpc
#BuildRequires: picocontainer
BuildRequires: tanukiwrapper
Requires(post): chkconfig
Requires(preun): chkconfig
# This is for /sbin/service
Requires(preun): initscripts

%description
This package contains the code for the Full Text Search Server for
Spacewalk Server.

%prep
%setup -n %{name}-%{version}

%install
ant -Djar.version=%{version} install
install -d -m 755 $RPM_BUILD_ROOT/%{_sysconfdir}/rhn/search
install -d -m 755 $RPM_BUILD_ROOT/%{_prefix}/share/rhn/search
install -d -m 755 $RPM_BUILD_ROOT/%{_prefix}/share/rhn/search/indexes
install -d -m 755 $RPM_BUILD_ROOT/%{_prefix}/share/rhn/search/lib
install -d -m 755 $RPM_BUILD_ROOT/%{_prefix}/share/rhn/search/classes/com/redhat/satellite/search/db
install -d -m 755 $RPM_BUILD_ROOT/etc/init.d
install -d -m 755 $RPM_BUILD_ROOT/%{_bindir}
install -d -m 755 $RPM_BUILD_ROOT/%{_var}/log/rhn/search
install -d -m 755 $RPM_BUILD_ROOT/%{_prefix}/share/rhn/search/nutch

install -m 644 dist/%{name}-%{version}.jar $RPM_BUILD_ROOT/%{_prefix}/share/rhn/search/lib/
# using install -m does not preserve the symlinks
cp -d lib/* $RPM_BUILD_ROOT/%{_prefix}/share/rhn/search/lib
install -m 644 src/config/log4j.properties $RPM_BUILD_ROOT/%{_prefix}/share/rhn/search/classes/log4j.properties
install -m 644 src/config/com/redhat/satellite/search/db/* $RPM_BUILD_ROOT/%{_prefix}/share/rhn/search/classes/com/redhat/satellite/search/db
install -m 755 src/config/rhn-search $RPM_BUILD_ROOT/%{_sysconfdir}/init.d
ln -s -f /usr/sbin/tanukiwrapper $RPM_BUILD_ROOT/%{_bindir}/rhnsearchd
install -m 644 src/config/search/rhn_search.conf $RPM_BUILD_ROOT/%{_sysconfdir}/rhn/search/rhn_search.conf
install -m 644 src/config/search/rhn_search_daemon.conf $RPM_BUILD_ROOT/%{_sysconfdir}/rhn/search/rhn_search_daemon.conf
ln -s -f %{_prefix}/share/rhn/search/lib/spacewalk-search-%{version}.jar $RPM_BUILD_ROOT%{_prefix}/share/rhn/search/lib/spacewalk-search.jar

%clean
rm -rf $RPM_BUILD_ROOT

%post
# This adds the proper /etc/rc*.d links for the script
/sbin/chkconfig --add rhn-search

%preun
if [ $1 = 0 ] ; then
    /sbin/service rhn-search stop >/dev/null 2>&1
    /sbin/chkconfig --del rhn-search
fi

%files
%defattr(644,root,root,755)
%attr(755, root, root) %{_var}/log/rhn/search
%{_prefix}/share/rhn/search/lib/*
%{_prefix}/share/rhn/search/classes/log4j.properties
%{_prefix}/share/rhn/search/classes/com/*
%attr(755, root, root) %{_prefix}/share/rhn/search/indexes
%attr(755, root, root) %{_sysconfdir}/init.d/rhn-search
%attr(755, root, root) %{_bindir}/rhnsearchd
%config(noreplace) %{_sysconfdir}/rhn/search/rhn_search.conf
%config(noreplace) %{_sysconfdir}/rhn/search/rhn_search_daemon.conf

%changelog
* Tue Oct 21 2008 Michael Mraka <michael.mraka@redhat.com> 0.3.2-1
- resolves #467717 - fixed sysvinit scripts

* Tue Sep 23 2008 Milan Zazrivec 0.3.1-1
- fixed package obsoletes

* Wed Sep  3 2008 Milan Zazrivec 0.2.6-1
- config file needs to point to correct spacewalk-search.jar

* Tue Sep  2 2008 Jesus Rodriguez 0.2.5-1
- tagged for rebuild
- includes errata search capability
- fix setup and source0 to be name-version
- removed unnecessary bloat from libsrc directory
- removed apache-ibatis-sqlmap as a requires for now. FIXME

* Mon Aug 11 2008 Jesus Rodriguez 0.1.2-1
- tagged for rebuild after rename, also bumping version

* Tue Aug  5 2008 Jan Pazdziora 0.1.2-0
- tagged for rebuild after rename, also bumping version

* Mon Aug  4 2008 Jan Pazdziora 0.1.1-0
- rebuilt with BuildRequires: java-devel >= 1.5.0
