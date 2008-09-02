Name:         perl-NOCpulse-ProcessPool
Version: 	  0.10.1
Release: 	  6%{?dist}
Summary:      Perl implementation of a process pool
# This src.rpm is cannonical upstream
# You can obtain it using this set of commands
# git clone git://git.fedorahosted.org/git/spacewalk.git/
# cd monitoring/PerlModules/NP/ProcessPool
# make srpm
URL:          https://fedorahosted.org/spacewalk
Source0:      %{name}-%{version}.tar.gz
BuildArch:    noarch
Requires:     perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version)) 
BuildRequires: perl(FreezeThaw)
BuildRequires: perl(NOCpulse::Debuggable)
Group:        Development/Libraries
License:      GPLv2
Buildroot:    %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description
NOCpulse provides application, network, systems and transaction monitoring,
coupled with a comprehensive reporting system including availability,
historical and trending reports in an easy-to-use browser interface.

This package provides an API for using a pool of processes to execute jobs.

%prep
%setup -q

%build
%{__perl} Makefile.PL INSTALLDIRS=vendor OPTIMIZE="$RPM_OPT_FLAGS"
make %{?_smp_mflags}

%install
rm -rf $RPM_BUILD_ROOT
make pure_install PERL_INSTALL_ROOT=$RPM_BUILD_ROOT

find $RPM_BUILD_ROOT -type f -name .packlist -exec rm -f {} \;
find $RPM_BUILD_ROOT -type f -name '*.bs' -size 0 -exec rm -f {} \;
find $RPM_BUILD_ROOT -depth -type d -exec rmdir {} 2>/dev/null \;

%{_fixperms} $RPM_BUILD_ROOT/*

%check
make test

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
%{perl_vendorlib}/NOCpulse/*

%changelog
* Tue Sep  2 2008 Miroslav Suchý <msuchy@redhat.com>
- edit spec to comply with Fedora Guidelines

* Fri Jun  6 2008 Milan Zazrivec <mzazrivec@redhat.com> 0.10.1-6
- cvs.dist import
