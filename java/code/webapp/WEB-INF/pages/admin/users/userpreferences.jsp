<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<html:xhtml/>
<html>
<body>

<%@ include file="/WEB-INF/pages/common/fragments/user/user-header.jspf" %>

<html:errors />
<html:form action="/users/PrefSubmit">
<%@ include file="/WEB-INF/pages/common/fragments/user/preferences.jspf" %>
</html:form>
</body>
</html>
