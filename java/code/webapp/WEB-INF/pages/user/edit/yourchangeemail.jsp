<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<html:xhtml/>
<html>
<body>


<html:messages id="message" message="true">
    <rhn:messages><c:out escapeXml="false" value="${message}" /></rhn:messages>
</html:messages>


<rhn:toolbar base="h1" img="/img/rhn-icon-users.gif"
                 helpUrl="/rhn/help/reference/en/s2-sm-your-rhn-account.jsp#s3-sm-your-rhn-email"
                 imgAlt="users.jsp.imgAlt">
    <bean:message key="yourchangeemail.jsp.title"/>
</rhn:toolbar>

<p>
${pageinstructions}
</p>

<html:errors />

<html:form action="/account/ChangeEmailSubmit">
  <html:text property="email" size="32" />
  <html:submit value="${button_label}" />
</html:form>

</body>
</html>
