Name: spacewalk-ssl-cert-check
Summary: check ssl certs for impending expiration
Group: RHN/Client
License: GPLv2
Source1: ssl-cert-check 
Source2: rhn-ssl-cert-check 
Version: 1.4
Release: 10.10%{?dist}
BuildRoot: /var/tmp/%{name}-%{version}-root
BuildArch: noarch
Requires: openssl

%description 
Runs a check once a day to see if the ssl certificates installed on this
server are expected to expire in the next 30 days, and if so, email the 
administrator.

%prep

%build
%install
rm -rf $RPM_BUILD_ROOT
install -d $RPM_BUILD_ROOT
ls -la $RPM_SOURCE_DIR/ssl-cert-check
install -d $RPM_BUILD_ROOT/%{_datadir}/ssl
install -d $RPM_BUILD_ROOT/etc/cron.daily

install -m755 %{SOURCE1} $RPM_BUILD_ROOT/%{_datadir}/ssl/ssl-cert-check
install -m755 %{SOURCE2} $RPM_BUILD_ROOT/%{_sysconfdir}/cron.daily/rhn-ssl-cert-check

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root) 
%attr(0755,root,root) %{_sysconfdir}/cron.daily/rhn-ssl-cert-check
%attr(0755,root,root) %{_datadir}/ssl/ssl-cert-check

# $Id: ssl-cert-check.spec,v 1.1 2005/05/09 17:58:28 alikins Exp $
%changelog
* Tue Sep  2 2008 Milan Zazrivec 1.4-10.10
- Renamed rhn-ssl-cert-check to spacewalk-ssl-cert-check

* Mon Aug 11 2008 Mike McCune 1.4-10.9
- building to clean up src vs binary rpm mismatch in public repo

* Mon Aug  4 2008 Jan Pazdziora 1.4-10.8
- remove the version file

* Thu Jun 2 2005 Adrian Likins <alikins@redhat.com>
- fix some bugs in rhn-ssl-cert-check

* Mon May 9 2005 Adrian Likins <alikins@redhat.com> 
- initial build
