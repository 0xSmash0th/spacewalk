<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>

<html:xhtml/>
<html>
<body>

<html:errors />
<html:messages id="message" message="true">
  <rhn:messages><c:out escapeXml="false" value="${message}" /></rhn:messages>
</html:messages>

<%@ include
	file="/WEB-INF/pages/common/fragments/configuration/channel/details-header.jspf"%>

<div class="importfragment">
<a name="import"/>
<%@ include file="/WEB-INF/pages/common/fragments/configuration/channel/import.jspf" %>
</div>
<p />

</body>
</html>

