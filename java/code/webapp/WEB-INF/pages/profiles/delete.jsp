<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>

<html:xhtml/>
<html>

<body>

<html:messages id="message" message="true">
    <rhn:messages><c:out escapeXml="false" value="${message}" /></rhn:messages>
</html:messages>

<html:errors />

<%@ include file="/WEB-INF/pages/common/fragments/profile/header.jspf" %>

<h2><bean:message key="deleteconfirm.jsp.confirmprofiledeletion"/></h2>

<div class="page-summary">
   <p><bean:message key="deleteconfirm.jsp.profile_pagesummary" arg0="${requestScope.profile.name}"/></p>
</div>

<html:form action="/profiles/Delete">
      <div align="right">
        <hr />
        <html:hidden property="prid" value="${param.prid}" />
        <html:hidden property="submitted" value="true" />
        <html:submit>
	        <bean:message key="deleteconfirm.jsp.deleteprofile"/>
	    </html:submit>
      </div>
</html:form>

</body>
</html>

