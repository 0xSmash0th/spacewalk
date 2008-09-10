Name: spacewalk-certs-tools
Summary: Spacewalk SSL Key/Cert Tool
Group: Applications/Internet
License: GPLv2
Version: 0.2.3
Release: 1%{?dist}
# This src.rpm is cannonical upstream
# You can obtain it using this set of commands
# git clone git://git.fedorahosted.org/git/spacewalk.git/
# cd satellite/certs
# make test-srpm
URL:      https://fedorahosted.org/spacewalk 
Source0: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch: noarch
Requires: openssl rpm-build
BuildRequires: docbook-utils
BuildRequires: python
Obsoletes: rhns-certs <= 5.2.0
Obsoletes: rhns-certs-tools <= 5.2.0

%description
This package contains tools to generate the SSL certificates required by 
Spacewalk.

%define rhnroot %{_prefix}/share/rhn

%prep
%setup -q

%build
#nothing to do here

%install
rm -rf $RPM_BUILD_ROOT
install -d -m 755 $RPM_BUILD_ROOT/%{rhnroot}/certs
make -f Makefile.certs install PREFIX=$RPM_BUILD_ROOT ROOT=%{rhnroot} \
    MANDIR=%{_mandir}

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
%attr(755,root,root) %{rhnroot}/certs/*.py*
%attr(755,root,root) %{rhnroot}/certs/sign.sh
%attr(755,root,root) %{rhnroot}/certs/gen-rpm.sh
%attr(755,root,root) %{_bindir}/rhn-ssl-tool
%attr(755,root,root) %{_bindir}/rhn-bootstrap
%doc %{_mandir}/man1/rhn-*.1*
%{_var}/www/html/pub/bootstrap/client_config_update.py*

%changelog
* Wed Sep 10 2008 Milan Zazrivec 0.2.3-1
- fixed package obsoletes

* Tue Sep  2 2008 Milan Zazrivec 0.2.2-1
- Bumped version for tag-release

* Tue Aug 18 2008 Mike McCune <mmccune@redhat.com> 0.2-1
- get rid of python-optik

* Tue Aug  5 2008 Miroslav Suchy <msuchy@redhat.com> 0.2-0
- Rename to spacewalk-certs-tools
- clean up spec

* Mon Aug  4 2008 Jan Pazdziora 0.1-1
- removed version and sources files

* Wed May 28 2008 Jan Pazdziora 5.2.0-2
- fix for padding L on RHEL 5

* Wed May 21 2008 Jan Pazdziora - 5.2.0-1
- rebuild in dist-cvs.

* Wed Mar 07 2007 Pradeep Kilambi <pkilambi@redhat.com> - 5.0.0-1
- adding dist tag

* Fri Dec 01 2006 Ryan Newberry <rnewberr@redhat.com> 
- adding docbook2man to build requires

* Mon Dec 20 2004 Todd Warner <taw@redhat.com> 3.6.0
- requirement added: python-optik (bug: 143413)

* Tue Jul 06 2004 Todd Warner <taw@redhat.com>
- rhn-bootstrap and associated files added.

* Tue Apr 20 2004 Todd Warner <taw@redhat.com>
- added rhn-ssl-tool and associated modules
- using a Makefile which builds a tarball now.
- added man page
- added __init__.py*
- GPLed this code. No reason to do otherwise.

* Tue Aug 20 2002 Cristian Gafton <gafton@redhat.com>
- update for the new build system

* Tue May 21 2002 Cristian Gafton <gafton@redhat.com>
- no more RHNS

* Tue May 14 2002 Todd Warner <taw@redhat.com>
- Initial.
