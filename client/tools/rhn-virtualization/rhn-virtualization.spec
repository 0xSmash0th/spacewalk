%define rhn_dir /usr/share/rhn
%define rhn_conf_dir /etc/sysconfig/rhn
%define cron_dir /etc/cron.d
%define init_dir /etc/rc.d/init.d


Name:           rhn-virtualization 
Summary:        RHN action support for virualization

Group:          System Environment/Base
License:        GPLv2
URL:            http://rhn.redhat.com
Source0:        %{name}-%{version}.tar.gz
Source1:        version

Version:        %(echo `awk '{ print $1 }' %{SOURCE1}`)
Release:        %(echo `awk '{ print $2 }' %{SOURCE1}`)

BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

BuildArch:      noarch

%description
rhn-virtualization provides various RHN actions for manipulation virtual
machine guest images.

%package common
Summary: Files needed by both rhn-virtualization-host and -guest.
Group: System Environment/Base
Requires: rhn-client-tools
Requires: chkconfig

%description common
This package contains files that are needed by the rhn-virtualization-host
and rhn-virtualization-guest packages.

%package host
Summary: RHN Virtualization support specific to the Host system.
Group: System Environment/Base
Requires: libvirt-python
Requires: rhn-virtualization-common
Conflicts: rhn-virtualization-guest

%description host
This package contains code for RHN's Virtualization support that is 
specific to the Host system (a.k.a. Dom0).

%package guest
Summary: RHN Virtualization support specific to Guest systems.
Group: System Environment/Base
Requires: rhn-virtualization-common
Conflicts: rhn-virtualization-host

%description guest
This package contains code for RHN's Virtualization support that is
specific to Guest systems (a.k.a. DomUs).


%prep
%setup -q


%build
make


%install
rm -rf $RPM_BUILD_ROOT
make DESTDIR=$RPM_BUILD_ROOT install

 
%clean
rm -rf $RPM_BUILD_ROOT


%post host
/sbin/chkconfig --add rhn-virtualization-host
/sbin/service crond restart

%preun host
/sbin/chkconfig --del rhn-virtualization-host

%postun host
/sbin/service crond restart

%post guest
/sbin/chkconfig --add rhn-virtualization-guest
/sbin/service rhn-virtualization-guest start

%preun guest
/sbin/chkconfig --del rhn-virtualization-guest

%files common
%defattr(-,root,root,-)
%dir %{rhn_dir}/virtualization
%{rhn_dir}/virtualization/__init__.py
%{rhn_dir}/virtualization/__init__.pyc
%{rhn_dir}/virtualization/__init__.pyo
%{rhn_dir}/virtualization/batching_log_notifier.py
%{rhn_dir}/virtualization/batching_log_notifier.pyc
%{rhn_dir}/virtualization/batching_log_notifier.pyo
%{rhn_dir}/virtualization/constants.py
%{rhn_dir}/virtualization/constants.pyc
%{rhn_dir}/virtualization/constants.pyo
%{rhn_dir}/virtualization/errors.py
%{rhn_dir}/virtualization/errors.pyc
%{rhn_dir}/virtualization/errors.pyo
%{rhn_dir}/virtualization/notification.py
%{rhn_dir}/virtualization/notification.pyc
%{rhn_dir}/virtualization/notification.pyo
%{rhn_dir}/virtualization/util.py
%{rhn_dir}/virtualization/util.pyc
%{rhn_dir}/virtualization/util.pyo


%files host
%defattr(-,root,root,-)
%dir %{rhn_conf_dir}/virt
%dir %{rhn_conf_dir}/virt/auto
%{init_dir}/rhn-virtualization-host
%attr(644,root,root) %{cron_dir}/rhn-virtualization.cron
%{rhn_dir}/virtualization/domain_config.py
%{rhn_dir}/virtualization/domain_config.pyc
%{rhn_dir}/virtualization/domain_control.py
%{rhn_dir}/virtualization/domain_control.pyc
%{rhn_dir}/virtualization/domain_directory.py
%{rhn_dir}/virtualization/domain_directory.pyc
%{rhn_dir}/virtualization/get_config_value.py
%{rhn_dir}/virtualization/get_config_value.pyc
%{rhn_dir}/virtualization/init_action.py
%{rhn_dir}/virtualization/init_action.pyc
%{rhn_dir}/virtualization/poller.py
%{rhn_dir}/virtualization/poller.pyc
%{rhn_dir}/virtualization/schedule_poller.py
%{rhn_dir}/virtualization/schedule_poller.pyc
%{rhn_dir}/virtualization/poller_state_cache.py
%{rhn_dir}/virtualization/poller_state_cache.pyc
%{rhn_dir}/virtualization/start_domain.py
%{rhn_dir}/virtualization/start_domain.pyc
%{rhn_dir}/virtualization/state.py
%{rhn_dir}/virtualization/state.pyc
%{rhn_dir}/virtualization/support.py
%{rhn_dir}/virtualization/support.pyc
%{rhn_dir}/actions/virt.py
%{rhn_dir}/actions/virt.pyc
%{rhn_dir}/virtualization/domain_config.pyo
%{rhn_dir}/virtualization/domain_control.pyo
%{rhn_dir}/virtualization/domain_directory.pyo
%{rhn_dir}/virtualization/get_config_value.pyo
%{rhn_dir}/virtualization/init_action.pyo
%{rhn_dir}/virtualization/poller.pyo
%{rhn_dir}/virtualization/schedule_poller.pyo
%{rhn_dir}/virtualization/poller_state_cache.pyo
%{rhn_dir}/virtualization/start_domain.pyo
%{rhn_dir}/virtualization/state.pyo
%{rhn_dir}/virtualization/support.pyo
%{rhn_dir}/actions/virt.pyo


%files guest
%defattr(-,root,root,-)
%{init_dir}/rhn-virtualization-guest
%{rhn_dir}/virtualization/report_uuid.py
%{rhn_dir}/virtualization/report_uuid.pyc
%{rhn_dir}/virtualization/report_uuid.pyo


%changelog
* Fri Oct 06 2006 James Bowes <jbowes@redhat.com> - 1.0.1-13
- Require rhn-client-tools rather than up2date.

* Tue Sep 26 2006 Peter Vetere <pvetere@redhat.com> - 1.0.1-12
- Added batching_log_notifier file to common.

* Fri Sep 15 2006 James Bowes <jbowes@redhat.com> - 1.0.1-11
- Stop ghosting pyo files.

* Wed Sep 13 2006 Peter Vetere <pvetere@redhat.com> 1.0.1-10
- made host- and guest- specific names for their respective init scripts
- added an init script so the guest can report its uuid when it boots

* Wed Aug 30 2006 John Wregglesworth <wregglej@redhat.com> 1.0.1-7
- split the everything into three subpackages: common, host, guest
- added report_uuid.

* Wed Aug 02 2006 James Bowes <jbowes@redhat.com> 1.0.1-2
- get_name was renamed to get_config_value
- rhn_xen was renamed to rhn-virtualization

* Fri Jul 07 2006 James Bowes <jbowes@redhat.com> 1.0.1-1
- New version.
- Remove unused macro.

* Fri Jul 07 2006 James Bowes <jbowes@redhat.com> 0.0.1-1
- Initial packaging outside of up2date
