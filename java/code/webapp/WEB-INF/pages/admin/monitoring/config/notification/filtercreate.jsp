<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>

<html:xhtml/>
<html>
    <head>
        <meta name="page-decorator" content="none" />
    </head>
<body>

<html:errors />
<html:messages id="message" message="true">
  <rhn:messages><c:out escapeXml="false" value="${message}" /></rhn:messages>
</html:messages>

  <rhn:toolbar base="h1" img="/img/rhn-icon-system_group.gif"
	           helpUrl="/rhn/help/reference/en/s2-sm-monitor-notif.jsp">
    <bean:message key="filtercreate.jsp.header1" />
  </rhn:toolbar>

<h2><bean:message key="filtercreate.jsp.header2"/></h2>

<html:form action="/monitoring/config/notification/FilterCreate" method="POST">
    <%@ include file="filter-form.jspf" %>
</html:form>

</body>
</html>
