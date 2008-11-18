%define release_name Alpha
Name:           spacewalk
Version:        0.4.2
Release:        1%{?dist}
Summary:        Spacewalk Systems Management Application
URL:            https://fedorahosted.org/spacewalk
Group:          Applications/Internet
License:        GPLv2
BuildRoot:      %{_tmppath}/%{name}-root-%(%{__id_u} -n)
BuildArch:      noarch
BuildRequires:  python
Requires:       python >= 2.3
Requires:       spacewalk-setup

# Java
Requires:       spacewalk-java
Requires:       spacewalk-taskomatic
Requires:       spacewalk-search

# Perl
Requires:       spacewalk-html
Requires:       spacewalk-base
Requires:       spacewalk-cypress
Requires:       spacewalk-grail
Requires:       spacewalk-pxt
Requires:       spacewalk-sniglets
Requires:       spacewalk-moon

# Python
Requires:       spacewalk-certs-tools
Requires:       spacewalk-backend
Requires:       spacewalk-backend-app
Requires:       spacewalk-backend-applet
Requires:       spacewalk-backend-config-files
Requires:       spacewalk-backend-config-files-common
Requires:       spacewalk-backend-config-files-tool
Requires:       spacewalk-backend-package-push-server
Requires:       spacewalk-backend-tools
Requires:       spacewalk-backend-server
Requires:       spacewalk-backend-sql
Requires:       spacewalk-backend-xml-export-libs
Requires:       spacewalk-backend-xmlrpc
Requires:       spacewalk-backend-xp
Requires:       rhnpush


# Misc
Requires:       spacewalk-schema
Requires:       spacewalk-config
Requires:       cobbler
Requires:       yum-utils

# Requires:       osa-dispatcher
# Requires:       jabberpy

# Monitoring packages
Requires:       spacewalk-monitoring

# Solaris
# Requires:       rhn-solaris-bootstrap
# Requires:       rhn_solaris_bootstrap_5_1_0_3



%description
Spacewalk is a systems management application that will 
inventory, provision, update and control your Linux and 
Solaris machines.

%prep
#nothing to do here

%build
#nothing to do here

%install
rm -rf $RPM_BUILD_ROOT
install -d $RPM_BUILD_ROOT/%{_sysconfdir}
echo "Spacewalk release %{version} (%{release_name})" > $RPM_BUILD_ROOT/%{_sysconfdir}/spacewalk-release

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root)
/%{_sysconfdir}/spacewalk-release

%changelog
* Tue Nov 18 2008 Miroslav Suchý <msuchy@redhat.com> 0.4.2-1
- require spacewalk-monitoring instead each individual monitoring package

* Fri Oct 24 2008 Jesus Rodriguez <jesusr@redhat.com> 0.3.2-1
- respin for 0.3

* Wed Oct 22 2008 Devan Goodwin <dgoodwin@redhat.com> 0.2.4-1
- Remove spacewalk-dobby dependency, only needed for Satellite embedded Oracle installs.

* Mon Sep 29 2008 Miroslav Suchý <msuchy@redhat.com> 0.2.3-1
- rename oracle_config to nocpulse-db-perl
- merge NPusers and NPconfig to nocpulse-common
- remove nslogs
- enable monitoring again
- fix rpmlint errors

* Tue Sep  2 2008 Jesus Rodriguez <jesusr@redhat.com> 0.2.2-1
- add spacewalk-search as a new Requires
- change version to work with the new make srpm rules

* Mon Sep  1 2008 Milan Zazrivec <mzazrivec@redhat.com> 0.2-4
- bumped minor release for new package build

* Wed Aug 13 2008 Mike 0.2-3
- Fixing requires for new package names

* Mon Aug 11 2008 Mike 0.2-2
- tag to rebuild

* Wed Aug  6 2008 Jan Pazdziora 0.1-7
- tag to rebuild

* Mon Aug  4 2008 Miroslav Suchy <msuchy@redhat.com>
- Migrate name of packages to spacewalk namespace.

* Tue Jun 3 2008 Jesus Rodriguez <mmccune at redhat dot com> 0.1
- initial rpm release
