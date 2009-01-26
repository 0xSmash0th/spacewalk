Name:           spacewalk-setup
Version:        0.5.4
Release:        1%{?dist}
Summary:        Initial setup tools for Red Hat Spacewalk

Group:          Applications/System
License:        GPLv2
URL:            http://spacewalk.redhat.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

BuildRequires:  perl
BuildRequires:  perl(ExtUtils::MakeMaker)
## non-core
#BuildRequires:  perl(Getopt::Long), perl(Pod::Usage)
#BuildRequires:  perl(Test::Pod::Coverage), perl(Test::Pod)

BuildArch:      noarch
Requires:       perl
Requires:       perl-Params-Validate
Requires:       spacewalk-schema
Requires:       perl-DBD-Pg
Requires:       /sbin/restorecon
Requires:       spacewalk-admin
Requires:       spacewalk-certs-tools
Requires:       perl-Satcon
Requires:       spacewalk-backend-tools
Requires:       cobbler >= 0:1.4
Requires:       jabberd


%description
A collection of post-installation scripts for managing Spacewalk's initial
setup tasks, re-installation, and upgrades.


%prep
%setup -q


%build
%{__perl} Makefile.PL INSTALLDIRS=vendor
make %{?_smp_mflags}


%install
rm -rf %{buildroot}
make pure_install PERL_INSTALL_ROOT=%{buildroot}
find %{buildroot} -type f -name .packlist -exec rm -f {} ';'
find %{buildroot} -type d -depth -exec rmdir {} 2>/dev/null ';'
chmod -R u+w %{buildroot}/*
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0755 share/embedded_diskspace_check.py %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/sudoers.base %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/sudoers.rhn %{buildroot}/%{_datadir}/spacewalk/setup/
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/defaults.d/
install -m 0644 share/defaults.d/defaults.conf %{buildroot}/%{_datadir}/spacewalk/setup/defaults.d/

# Oracle specific stuff, possible candidate for sub-package down the road:
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/oracle/
install -m 0755 share/oracle/install-db.sh %{buildroot}/%{_datadir}/spacewalk/setup/oracle
install -m 0755 share/oracle/remove-db.sh %{buildroot}/%{_datadir}/spacewalk/setup/oracle
install -m 0755 share/oracle/upgrade-db.sh %{buildroot}/%{_datadir}/spacewalk/setup/oracle


%check
make test


%clean
rm -rf %{buildroot}


%files
%defattr(-,root,root,-)
%doc Changes README
%{perl_vendorlib}/*
%{_bindir}/spacewalk-setup
%{_bindir}/cobbler-setup
%{_mandir}/man[13]/*.[13]*
%{_datadir}/spacewalk/*


%changelog
* Fri Jan 23 2009 Milan Zazrivec 0.5.4-1
- re-enable satellite upgrades

* Wed Jan 21 2009 Michael Mraka <michael.mraka@redhat.com> 0.5.3-1
- fixed branding stuff

* Mon Jan 19 2009 Jan Pazdziora 0.5.2-1
- fix path in Makefile

* Mon Jan 19 2009 Jan Pazdziora 0.5.1-1
- rebuilt for 0.5, after repository reorg

* Thu Jan 15 2009 Milan Zazrivec 0.4.23-1
- upgrade setup fixes

* Wed Jan 14 2009 Jan Pazdziora 0.4.22-1
- 479971 - require jabberd so that the jabberd user exists.

* Tue Jan 13 2009 Devan Goodwin <dgoodwin@redhat.com> 0.4.21-1
- 477492 - Remove "assuming Oracle" message from spacewalk-setup.

* Thu Jan  8 2009 Jan Pazdziora 0.4.20-1
- support symlinked and NFS-mounted /var/satellite during setup
- run chkconfig for "stock" httpd

* Thu Jan  8 2009 Milan Zazrivec 0.4.19-1
- Build for Spacewalk 0.4

* Mon Dec 22 2008 Mike McCune <mmccune@gmail.com> 0.4.18-1
- Adding cobbler requirement

* Mon Dec 22 2008 Michael Mraka <michael.mraka@redhat.com> 0.4.17-1
- changed defaults.conf to default.d/*
- moved spacewalk-public.cert to spacewalk-branding
- resolved #477490, #477493

* Fri Dec 19 2008 Dave Parker <dparker@redhat.com> 0.4.10-1
- added apache default ssl server config generation to spacewalk-setup

* Thu Dec 18 2008 Jan Pazdziora 0.4.11-1
- fixing duplicated $sth variable

* Wed Dec 17 2008 Miroslav Suchý <msuchy@redhat.com> 0.4.10-1
- 226915 - db_name can be different from db instance name

* Tue Dec 16 2008 Partha Aji <paji@redhat.com> 0.4.10-1
- added the cobbler setup module to build the spacewalk rpm.

* Thu Dec 11 2008 Michael Mraka <michael.mraka@redhat.com> 0.4.9-1
- resolved #471225 - moved /sbin stuff to /usr/sbin

* Wed Dec  3 2008 Milan Zazrivec 0.4.7-1
- updated fix for bz #473438

* Fri Nov 28 2008 Miroslav Suchý <msuchy@redhat.com> 0.4.6-1
- 473438 - inititate db alias

* Thu Nov 27 2008 Michael Mraka <michael.mraka@redhat.com> 0.4.5-1
- resolved #473082 - fixed sql query 
- resolved #472378 - set autostart flag on rhnsat entry

* Thu Nov 20 2008 Jan Pazdziora 0.4.3-1
- use full path to usermod
- check if we are on Red Hat Enterprise Linux before using its key
- run restorecon on Spacewalk::Setup::INSTALL_LOG_FILE

* Tue Nov 18 2008 Miroslav Suchý <msuchy@redhat.com> 0.4.2-1
- enable Monitoring services (#471220)

* Thu Oct 30 2008 Michael Mraka <michael.mraka@redhat.com> 0.4.1-1
- resolved #455421

* Tue Oct 21 2008 Michael Mraka <michael.mraka@redhat.com> 0.3.6-1
- resolves #467877 - use runuser instead of su

* Tue Oct 21 2008 Devan Goodwin <dgoodwin@redhat.com> 0.3.5-1
- Remove dependency on spacewalk-dobby. (only needed for embedded Oracle installations)

* Tue Oct 21 2008 Michael Mraka <michael.mraka@redhat.com> 0.3.4-1
- resolves #467717 - fixed sysvinit scripts

* Mon Sep 22 2008 Devan Goodwin <dgoodwin@redhat.com> 0.3.3-1
- Remove explicit chmod/chown on /var/log/rhn/.

* Thu Sep 18 2008 Devan Goodwin <dgoodwin@redhat.com> 0.3.2-1
- Fix bug with /var/log/rhn/ permissions.

* Wed Sep  3 2008 Milan Zazrivec <mzazrivec@redhat.com> 0.2.4-1
- include correct namespace when invoking system_debug()
- build-require perl(ExtUtils::MakeMaker) rather than package name

* Fri Aug 22 2008 Mike McCune <mmccune@redhat.com 0.2.2-2
- adding BuildRequires perl-ExtUtils-MakeMaker

* Wed Aug 20 2008 Devan Goodwin <dgoodwin@redhat.com> 0.2.2-1
- Updating build for spacewalk 0.2.

* Wed Jun  4 2008 Devan Goodwin <dgoodwin@redhat.com> 0.01-1
- Initial packaging.

