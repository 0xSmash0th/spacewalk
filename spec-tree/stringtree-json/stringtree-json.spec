%define base_package stringtree
Summary: An json string library
Name: stringtree-json
Version: 2.0.9
Release: 2%{?dist}
License: LGPL
Group: Development/Library
URL: http://stringtree.org/stringtree-json.html
Source0: %{base_package}-%{version}-src.zip
Patch0: %{base_package}-%{version}-build-xml.patch
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-buildroot
BuildRequires: jpackage-utils >= 0:1.5
BuildRequires: ant
BuildArch: noarch

%description 
a simple json reader/writer library for java

%prep
%setup -n %{base_package}
%patch0 -p1


%build
ant -f src/build.xml dist-json

%install
rm -rf $RPM_BUILD_ROOT

install -d -m 0755 $RPM_BUILD_ROOT%{_javadir}
install -m 644 dist/%{name}-%{version}.jar $RPM_BUILD_ROOT%{_javadir}/%{name}-%{version}.jar
(cd $RPM_BUILD_ROOT%{_javadir} && for jar in *-%{version}*; do ln -sf ${jar} `echo $jar| sed  "s|-%{version}||g"`; done)
install -d -m 755 $RPM_BUILD_ROOT%{_docdir}/%{name}-%{version}

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(0644,root,root,0755)
%{_javadir}

%changelog
* Wed Oct 22 2008 Jesus M. Rodriguez <jesusr@redhat.com 2.0.9-2
- First build

* Mon Sep 25 2008 Partha Aji <paji@redhat.com>
- Initial build.
