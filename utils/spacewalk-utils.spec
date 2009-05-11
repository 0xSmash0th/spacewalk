%define rhnroot %{_prefix}/share/rhn

Name:		spacewalk-utils
Version:	0.6.4
Release:	1%{?dist}
Summary:	Utilities that may be run against a Spacewalk server.

Group:		Applications/Internet
License:	GPLv2
URL:		https://fedorahosted.org/spacewalk
Source0:	https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
BuildRoot:	%{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch

BuildRequires:  /usr/bin/docbook2man
BuildRequires:  docbook-utils

Requires:       python, rpm-python

%description
Generic utilities that may be run against a Spacewalk server.  This package
contains utilities that may be installed on a client and run against the
server.


%prep
%setup -q


%build
make all


%install
rm -rf $RPM_BUILD_ROOT
install -d $RPM_BUILD_ROOT/%{rhnroot}
make install PREFIX=$RPM_BUILD_ROOT ROOT=%{rhnroot} \
    MANDIR=%{_mandir}


%clean
rm -rf $RPM_BUILD_ROOT


%files
%defattr(-,root,root)
%attr(755,root,root) %{_bindir}/sw-system-snapshot
%attr(755,root,root) %{_bindir}/migrate-system-profile
%dir %{rhnroot}/utils
%{rhnroot}/utils/__init__.py*
%{rhnroot}/utils/systemSnapshot.py*
%{rhnroot}/utils/migrateSystemProfile.py*
%{_mandir}/man8/sw-system-snapshot.8*
%{_mandir}/man8/migrate-system-profile.8*


%changelog
* Mon May 11 2009 Brad Buckingham <bbuckingham@redhat.com> 0.6.4-1
- 500173 - update migrate-system-profile to import scripts from utils vs
  spacewalk_tools (bbuckingham@redhat.com)

* Sun May 03 2009 Brad Buckingham <bbuckingham@redhat.com> 0.6.3-1
- updates to include system migration scripts


* Mon Apr 27 2009 Brad Buckingham <bbuckingham@redhat.com> 0.6.2-1
- Adding migrate system profile tool to utils package

* Tue Apr 07 2009 Brad Buckingham <bbuckingham@redhat.com> 0.6.1-1
- Initial spec created to include sw-system-snapshot package
