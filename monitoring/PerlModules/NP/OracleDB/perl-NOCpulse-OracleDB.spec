Name:         perl-NOCpulse-OracleDB
Version: 	  1.28.10
Release:      1%{?dist}
Summary:      Perl modules for NOCpulse Oracle database access
URL:          https://fedorahosted.org/spacewalk
Source0:      https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
BuildArch:    noarch
Requires:     perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
BuildRequires: perl(NOCpulse::Debug) perl(NOCpulse::Config) perl(NOCpulse::Utils::XML) perl(NOCpulse::Object)
BuildRequires: perl(DBD::Oracle) perl(DBI)
Group:        Development/Libraries
License:      GPLv2
Buildroot:    %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description
NOCpulse provides application, network, systems and transaction monitoring,
coupled with a comprehensive reporting system including availability,
historical and trending reports in an easy-to-use browser interface.

This package provides an API for accessing NOCpulse Oracle databases.

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
* Tue Jan 13 2009 Miroslav Suchý <msuchy@redhat.com>
- 479161 - explicitly call disconnect

* Tue Oct 21 2008 Miroslav Suchý <msuchy@redhat.com> 1.28.10-1
- 467441 - fix namespace

* Tue Sep  2 2008 Miroslav Suchý <msuchy@redhat.com> 1.28.3-1
- spec cleanup for Fedora

* Wed May 28 2008 Jan Pazdziora 1.28.2-12
- rebuild in dist.cvs
