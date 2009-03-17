%define rhnroot /usr/share/rhn
%define rhnconf /etc/sysconfig/rhn
%define client_caps_dir /etc/sysconfig/rhn/clientCaps.d

Name: rhncfg
Summary: Red Hat Network Configuration Client Libraries
Group:   Applications/System
License: GPLv2
URL:     https://fedorahosted.org/spacewalk
Source0: https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
Version: 5.9.3
Release: 1%{?dist}
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch: noarch
BuildRequires: docbook-utils
BuildRequires: python
Requires: python
Requires: rhnlib
Provides: rhn-config-action = %{version}
Provides: rhn-config-client-package = %{version}
Provides: rhn-config-management-package = %{version}

%description 
Red Hat Network Configuration Client Libraries

%package client
Summary: Red Hat Network Configuration Client
Group:   Applications/System
PreReq: %{name} = %{version}-%{release}
Provides: rhn-config-action = %{version}
Provides: rhn-config-client-package = %{version}

%description client
Red Hat Network Configuration Client

%package management
Summary: Red Hat Network Configuration Management Client
Group:   Applications/System
PreReq: %{name} = %{version}-%{release}
Provides: rhn-config-management-package = %{version}

%description management
Red Hat Network Configuration Management Client

%package actions
Summary: Red Hat Network Configuration Client Actions
Group:   Applications/System
PreReq: %{name} = %{version}-%{release}
Requires: %{name}-client

# If this is rhel 4 or less we need up2date.
%if 0%{?rhel} && "%rhel" < "5"
Requires: up2date
%else
Requires: rhn-client-tools
%endif

Provides: rhn-config-action = %{version}
Provides: rhn-config-client-package = %{version}

%description actions
Red Hat Network Configuration Client Actions

%prep
%setup -q

%build
make -f Makefile.rhncfg all

%install
rm -rf $RPM_BUILD_ROOT
install -d $RPM_BUILD_ROOT/%{rhnroot}
make -f Makefile.rhncfg install PREFIX=$RPM_BUILD_ROOT ROOT=%{rhnroot} \
    MANDIR=%{_mandir}

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
%{rhnroot}/config_common

%files client
%defattr(-,root,root)
%{rhnroot}/config_client
/usr/bin/rhncfg-client
%attr(644,root,root) %config(noreplace) %{rhnconf}/rhncfg-client.conf
%{_mandir}/man8/rhncfg-client.8*

%files management
%defattr(-,root,root)
%{rhnroot}/config_management
/usr/bin/rhncfg-manager
%attr(644,root,root) %config(noreplace) %{rhnconf}/rhncfg-manager.conf
%{_mandir}/man8/rhncfg-manager.8*

%files actions
%defattr(-,root,root)
%{rhnroot}/actions/*
/usr/bin/rhn-actions-control
%config(noreplace) %{client_caps_dir}/*
%{_mandir}/man8/rhn-actions-control.8*

# $Id$
%changelog
* Wed Feb 18 2009 Pradeep Kilambi <pkilambi@redhat.com> 5.9.3-1
- Applying patch for exclude files for rhncfg get call

* Thu Feb 12 2009 jesus m. rodriguez <jesusr@redhat.com> 5.9.2-1
- replace "!#/usr/bin/env python" with "!#/usr/bin/python"

* Thu Jan 22 2009 Michael Mraka <michael.mraka@redhat.com> 5.9.1-1
- resolved #428721 - bumped version

* Thu Jan 15 2009 Pradeep Kilambi <pkilambi@redhat.com> - 0.4.2-1
- BZ#476562 Extended list(elist) option for rhncfg

* Thu Oct 16 2008 Michael Mraka <michael.mraka@redhat.com> 0.3.1-1
- BZ#428721 - fixes filemode and ownership

* Tue Sep  2 2008 Milan Zazrivec 0.2.1-1
- Renamed Makefile to Makefile.rhncfg

* Mon Oct 01 2007 Pradeep Kilambi <pkilambi@redhat.com> - 5.1.0-2
- BZ#240513: fixes wrong umask issue

* Tue Sep 25 2007 Pradeep Kilambi <pkilambi@redhat.com> - 5.1.0-1
- rev build

* Wed Mar 07 2007 Pradeep Kilambi <pkilambi@redhat.com> - 5.0.2-2
- rev build
* Tue Feb 20 2007 James Bowes <jbowes@redhat.com> - 5.0.1-1
- Add dist tag.

* Tue Dec 19 2006 James Bowes <jbowes@redhat.com>
- Drastically reduce memory usage for configfiles.mtime_upload
  (and probably others).

* Thu Jun 23 2005 Nick Hansen <nhansen@redhat.com>: 4.0.0-18
- BZ#154746: make rhncfg-client diff work on solaris boxes
  BZ#160559:  Changed the way repositories are instantiated so 
  that the networking stuff won't get set up if --help is used with a mode.

* Wed Jun 15 2005 Nick Hansen <nhansen@redhat.com>: 4.0-16
- BZ#140501: catch outage mode message and report it nicely. 

* Fri May 20 2005 John Wregglesworth <wregglej@redhat.com>: 4.0-9
- Fixing True/False to work on AS 2.1

* Fri May 13 2005 Nick Hansen <nhansen@redhat.com>: 4.0-8
- BZ#156618: fix client capabilities list that is sent to the server

* Fri Apr 29 2005 Nick Hansen <nhansen@redhat.com>
- adding rhn-actions-control script to actions package

* Fri Jun 04 2004 Bret McMillan <bretm@redhat.com>
- many bug fixes
- removed dependencies on rhns-config-libs

* Mon Jan 20 2004 Todd Warner <taw@redhat.com>
- rhncfg-{client,manager} man pages added

* Mon Nov 24 2003 Mihai Ibanescu <misa@redhat.com>
- Added virtual provides
- Added client capabilities for actions

* Fri Nov 14 2003 Mihai Ibanescu <misa@redhat.com>
- Added default config files

* Fri Sep 12 2003 Mihai Ibanescu <misa@redhat.com>
- Requires rhnlib

* Mon Sep  8 2003 Mihai Ibanescu <misa@redhat.com>
- Initial build
