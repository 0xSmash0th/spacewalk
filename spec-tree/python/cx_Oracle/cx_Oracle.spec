%{!?python_sitearch: %define python_sitearch %(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib(1)")}

Summary: Python interface to Oracle
Name: cx_Oracle
Version:        4.2.1
Release:        2%{?dist}
Source0: %{name}-%{version}.tar.gz
Patch0: %{name}-instantclient.patch
License: BSD-style
Group: Development/Libraries
BuildRoot: %{_tmppath}/%{name}-buildroot
Obsoletes: DCOracle
Provides: python(:DBAPI:oracle) = 2.0
BuildRequires: python-devel
BuildRequires: oracle-instantclient-devel
Url: http://sourceforge.net/projects/cx-oracle/

%description
Python interface to Oracle conforming to the Python DB API 2.0 specification.
See http://www.python.org/topics/database/DatabaseAPI-2.0.html.

%prep
%setup -q
%patch0 -p1 -b .instantclient

%build
#kinda ugly but we need ORACLE_HOME to be set 
%if "%{_lib}" == "lib64"
export ORACLE_HOME=/usr/lib/oracle/10.2.0.4/client64/
%else
export ORACLE_HOME=/usr/lib/oracle/10.2.0.4/client/
%endif
env CFLAGS="$RPM_OPT_FLAGS" FORCE_RPATH=1 %{__python} setup.py build

%install
rm -rf $RPM_BUILD_ROOT
#kinda ugly but we need ORACLE_HOME to be set 
%if "%{_lib}" == "lib64"
export ORACLE_HOME=/usr/lib/oracle/10.2.0.4/client64/
%else
export ORACLE_HOME=/usr/lib/oracle/10.2.0.4/client/
%endif
%{__python} setup.py install --root=$RPM_BUILD_ROOT 

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
%doc LICENSE.txt README.txt HISTORY.txt html test
%{python_sitearch}/*

%changelog
* Tue Sep  2 2008 Milan Zazrivec
- rebuild for el5 and el4

* Mon Aug 25 2008 Dennis Gilmore <dgilmore@redhat.com> 4.2.1-2
- add disttag define ORACLE_HOME
- builds in koji
- setup %%files correctly 

* Wed Aug 21 2008 Mike McCune <mmccune@redhat.com> 4.2.1
- Migrating to git and new Makefile structure
* Tue Jan 29 2008 Michael Mraka <michael.mraka@redhat.com> 4.2.1
- Updated to 4.2.1
- Adapted for oracle-instantclient
* Mon Jan 16 2006 Mihai Ibanescu <misa@redhat.com> 4.1.2-0
- Updated to 4.1.2
* Thu Aug 12 2004 Mihai Ibanescu <misa@redhat.com> 4.0.1-1.8
- Memory leak patched
* Tue Jun 29 2004 Mihai Ibanescu <misa@redhat.com> 4.0.1-1.5
- Patched to resize variables when executemany() is called
* Wed May 19 2004 Mihai Ibanescu <misa@redhat.com> 4.0.1-1.4
- Provides python(:DBAPI:oracle) = 2.0
