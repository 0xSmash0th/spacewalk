<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>

<html:html xhtml="true">
<head>
</head>
<body>

<html:errors />
<html:messages id="message" message="true">
  <rhn:messages><c:out escapeXml="false" value="${message}" /></rhn:messages>
</html:messages>

<rhn:toolbar base="h1" img="/img/rhn-icon-system.gif"
	miscUrl="${url}"
	miscAcl="user_role(org_admin)"
	miscText="${text}"
	miscImg="${img}"
	miscAlt="${text}"
	imgAlt="users.jsp.imgAlt">
<bean:message key="sys_entitlements.${name}"/>
</rhn:toolbar>

<rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/systemEntitlementOrgs.xml" renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />

<h2><bean:message key="system_entitlement_details.access_grant"/></h2>

<bean:message key="system_entitlement_details.access_grant_desc.${name}"/>

<p/>


</body>
</html:html>

