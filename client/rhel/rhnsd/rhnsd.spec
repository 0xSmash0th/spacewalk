Summary: Red Hat Network query daemon
License: GPLv2
Group: System Environment/Base
Source0: https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
URL:     https://fedorahosted.org/spacewalk
Name: rhnsd
Version: 4.5.12
Release: 1%{?dist}
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

BuildRequires: gettext

Requires: rhn-check >= 0.0.8
Requires(post): chkconfig
Requires(preun): chkconfig
# This is for /sbin/service
Requires(preun): initscripts
Requires(postun): initscripts

%description
The Red Hat Update Agent that automatically queries the Red Hat
Network servers and determines which packages need to be updated on
your machine, and runs any actions.

%prep
%setup -q 

%build
make -f Makefile.rhnsd

%install
rm -rf $RPM_BUILD_ROOT
make -f Makefile.rhnsd install VERSION=%{version}-%{release} PREFIX=$RPM_BUILD_ROOT MANPATH=%{_mandir}

%find_lang %{name}


%post
/sbin/chkconfig --add rhnsd

%preun
if [ $1 = 0 ] ; then
    /etc/rc.d/init.d/rhnsd stop >/dev/null 2>&1
    /sbin/chkconfig --del rhnsd
fi


%postun
if [ "$1" -ge "1" ]; then
    /etc/rc.d/init.d/rhnsd condrestart >/dev/null 2>&1 || :
fi

%clean
rm -fr $RPM_BUILD_ROOT


%files -f %{name}.lang 
%defattr(-,root,root)
%config(noreplace) /etc/sysconfig/rhn/rhnsd
/usr/sbin/rhnsd
/etc/rc.d/init.d/rhnsd
%{_mandir}/man8/rhnsd.8*

%changelog
* Mon Sep 21 2009 Miroslav Suchý <msuchy@redhat.com> 4.5.12-1
- implement try-restart as alias for condrestart
- add LSB header
- change url, source0 and requires according to packaging guidelines

* Tue Jul 21 2009 John Matthews <jmatthew@redhat.com> 4.5.11-1
- #503719 - fix for postun scriptlet error (pkilambi@redhat.com)

* Tue Jul  7 2009 Pradeep Kilambi <pkilambi@redhat.com>
- Resolves: #503719 - fix for postun scriptlet error

* Thu Jun 25 2009 John Matthews <jmatthew@redhat.com> 4.5.10-1
- 243699: fixing the error code when unknown command is used with rhnsd init
  (pkilambi@redhat.com)
- updateing translations for rhnsd (pkilambi@redhat.com)

* Wed Jun  3 2009 Pradeep Kilambi <pkilambi@redhat.com> 4.7.0-3
- Resolves:  #243699 -fixing error code for unknown command to rhnsd init script

* Mon May 11 2009 Pradeep Kilambi <pkilambi@redhat.com> 4.7.0-2
- Updated translations.
- Resolves:  #243699
  
* Tue Jan 27 2009 Miroslav Suchý <msuchy@redhat.com> 4.5.9-1
- rebuild

* Wed Jan 21 2009 Pradeep Kilambi <pkilambi@redhat.com> 4.5.8-1
- Remove usage of version and sources files.
 
* Mon Dec 11 2006 James Bowes <jbowes@redhat.com> - 4.5.7-1
- Updated translations.
- Related: #216837

* Fri Dec 01 2006 James Bowes <jbowes@redhat.com> - 4.5.6-1
- Updated translations.

* Thu Nov 30 2006 James Bowes <jbowes@redhat.com> - 4.5.5-1
- New and updated translations.

* Tue Nov 28 2006 James Bowes <jbowes@redhat.com> - 4.5.4-1
- New and updated translations.

* Tue Nov 14 2006 James Bowes <jbowes@redhat.com> - 4.5.3-1
- Updated manual page.
- Require gettext.

* Mon Oct 30 2006 James Bowes <jbowes@redhat.com> - 4.5.2-1
- New and updated translations.

* Thu Sep 14 2006 James Bowes <jbowes@redhat.com> - 4.5.1-1
- Fix for bz 163483: rhnsd spawns children with SIGPIPE set to SIG_IGN

* Fri Jul 21 2006 James Bowes <jbowes@redhat.com> - 4.5.0-3
- Require rhn-check, not rhn_check.

* Wed Jul 19 2006 James Bowes <jbowes@redhat.com> - 4.5.0-2
- spec file cleanups.

* Fri Jul 07 2006 James Bowes <jbowes@redhat.com> - 4.5.0-1
- Release for RHEL5 matching with up2date.

* Thu May 18 2006 James Bowes <jbowes@redhat.com> - 0.0.2-1
- Refer to the proper commands in the man page.

* Tue Apr 11 2006 James Bowes <jbowes@redhat.com> - 0.0.1-1
- initial split from main up2date package.
