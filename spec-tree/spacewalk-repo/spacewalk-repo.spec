Summary: Spacewalk packages yum repository configuration.
Name: spacewalk-repo
Version: 0.2
Release: 1%{?dist}
License: GPL
Group: Development
# This src.rpm is cannonical upstream
# You can obtain it using this set of commands
# git clone git://git.fedorahosted.org/git/spacewalk.git/
# cd web
# make test-srpm
URL:          https://fedorahosted.org/spacewalk
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-buildroot
BuildArch: noarch

%description
This package contains the Spacewalk repository configuration for yum.

%prep
mkdir -p $RPM_BUILD_ROOT

%build

%install
rm -rf $RPM_BUILD_ROOT

# some sane default value
%define reposubdir      rhel/5Server
# redefine on fedora
%{?fedora: %define reposubdir      fedora/%{fedora}}

mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/yum.repos.d
cat >>$RPM_BUILD_ROOT%{_sysconfdir}/yum.repos.d/spacewalk.repo <<REPO
[spacewalk]
name=Spacewalk
baseurl=http://spacewalk.redhat.com/yum/%{version}/%{reposubdir}/\$basearch/
gpgkey=http://spacewalk.redhat.com/yum/RPM-GPG-KEY-spacewalk
enabled=1
gpgcheck=1
REPO

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
%config %{_sysconfdir}/yum.repos.d/spacewalk.repo

%changelog
* Fri Oct 24 2008 Michael Mraka <michael.mraka@redhat.com> 0.2-1
- Initial release
