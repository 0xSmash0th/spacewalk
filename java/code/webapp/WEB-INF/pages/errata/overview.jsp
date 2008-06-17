<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>

<html:xhtml/>
<html>
<head>
    <meta name="page-decorator" content="none" />
</head>
<body>

<html:messages id="message" message="true">
    <rhn:messages><c:out escapeXml="false" value="${message}" /></rhn:messages>
</html:messages>

<rhn:toolbar base="h1" img="/img/rhn-icon-errata.gif" imgAlt="errata.overview.jsp.alt"
 helpUrl="/rhn/help/reference/en/s1-sm-errata.jsp">
  <bean:message key="errata.overview.jsp.errataoverview"/>
</rhn:toolbar>

<p><bean:message key="errata.overview.jsp.summary"/></p>

<h2><bean:message key="errata.jsp.header"/></h2>
<form method="post" name="rhn_list" action="/rhn/errata/OverviewSubmit.do">
<%@ include file="/WEB-INF/pages/common/fragments/errata/relevant-errata-list.jspf" %>
</form>
</body>
</html>
