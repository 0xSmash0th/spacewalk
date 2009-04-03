%define rhnroot %{_datadir}/rhn

Name:          rhnpush
Summary:       Common programs needed to be installed on the RHN servers/proxies.
Group:         Applications/System
License:       GPLv2
URL:           http://fedorahosted.org/spacewalk
Version:       0.4.7
Release:       1%{?dist}
Source0:       https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
BuildRoot:     %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:     noarch
Requires:      rpm-python, rhnlib
BuildRequires: docbook-utils, gettext

Summary: Package uploader for the Red Hat Network Satellite Server

%description
rhnpush uploads package headers to the Red Hat Network servers into
specified channels and allows for several other channel management
operations relevant to controlling what packages are available per
channel.

%prep
%setup -q

%build
make -f Makefile.rhnpush all

%install
rm -rf $RPM_BUILD_ROOT
install -d $RPM_BUILD_ROOT/%{rhnroot}
make -f Makefile.rhnpush install PREFIX=$RPM_BUILD_ROOT ROOT=%{rhnroot} \
    MANDIR=%{_mandir}

%clean
rm -rf $RPM_BUILD_ROOT


%files
%defattr(-,root,root)
%dir %{rhnroot}/rhnpush
%{rhnroot}/rhnpush/*
%attr(755,root,root) %{_bindir}/rhnpush
%attr(755,root,root) %{_bindir}/rpm2mpm
%attr(755,root,root) %{_bindir}/solaris2mpm
%config(noreplace) %attr(644,root,root) %{_sysconfdir}/sysconfig/rhn/rhnpushrc
%{_mandir}/man8/rhnpush.8*
%{_mandir}/man8/solaris2mpm.8*

%changelog
* Wed Apr 01 2009 Pradeep Kilambi <pkilambi@redhat.com> 0.4.7-1
- new build for 485880 (pkilambi@redhat.com)
- 241221 - add release of the package (if it exists) to Provides (Jan P.)

* Wed Apr  1 2009 Pradeep Kilambi <pkilambi@redhat.com>
- Resolves: 485880 - missing help and man options for rhnpush - new build

* Wed Mar 11 2009 jesus m. rodriguez <jesusr@redhat.com> 0.4.6-1
- 485880 - adding missing man page options to rhnpush
- Fix tab in rhnpush.py.

* Thu Feb 26 2009 jesus m. rodriguez <jesusr@redhat.com> 0.4.5-1
- rebuild

* Wed Feb 25 2009 Pradeep Kilambi <pkilambi@redhat.com> 0.4.4-1
- Resolves: 487426 rhnpush should now require rhnlib

* Fri Feb 20 2009 Miroslav Suchy <msuchy@redhat.com> 0.4.4-1
- change builrequires from file dep. to package dep.

* Fri Feb 20 2009 Michael Stahnke <stahnma@fedoraproject.org> 0.4.3-1
- Package cleanup for Fedora Inclusion

* Thu Feb 12 2009 jesus m. rodriguez <jesusr@redhat.com> 0.4.2-1
- replace "!#/usr/bin/env python" with "!#/usr/bin/python"
- 436332 - return an error code other than 0 if there is a mismatch
- more changes for nvrea error handling
- 241127 - Solaris patch-requires fix
- 241369 - --force and --nullorg are incompatible options
- bump up version 0.4.1
- 461701 - don't use cached session if username is provided on commandline

* Wed Sep 24 2008 Milan Zazrivec 0.3.1-1
- Bumped version for spacewalk 0.3

* Tue Sep  2 2008 Milan Zazrivec 0.2.2-1
- Bumped version for spacewalk 0.2

* Thu Nov 02 2006 James Bowes <jbowes@redhat.com> - 4.2.0-48
- Initial seperate packaging.
