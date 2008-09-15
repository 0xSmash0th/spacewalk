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

<rhn:toolbar base="h1" img="/img/rhn-icon-system.gif"
 helpUrl="/rhn/help/reference/en/s1-sm-systems.jsp">
  <bean:message key="satellitelist.jsp.header"/>
</rhn:toolbar>

<form method="POST" name="rhn_list" action="/rhn/systems/SatelliteListSubmit.do">
  <rhn:list pageList="${requestScope.pageList}" noDataText="satellitelist.jsp.nosystems"
          legend="system">
    <%@ include file="/WEB-INF/pages/common/fragments/systems/system_listdisplay.jspf" %>
  </rhn:list>
</form>
</body>
</html>
