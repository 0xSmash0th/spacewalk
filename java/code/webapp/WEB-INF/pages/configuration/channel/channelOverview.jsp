<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html"
	prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean"
	prefix="bean"%>
<html:xhtml />

<html>
<head>
</head>
<body>
<html:errors />
<%@ include
	file="/WEB-INF/pages/common/fragments/configuration/channel/details-header.jspf"%>
<div class="left-column">
<%@ include
	file="/WEB-INF/pages/common/fragments/configuration/channel/properties.jspf"%>
<%@ include
	file="/WEB-INF/pages/common/fragments/configuration/channel/summary.jspf"%>
</div>
<div class="right-column">
<%@ include
	file="/WEB-INF/pages/common/fragments/configuration/channel/tasks.jspf"%>
</div>
</body>
</html>

