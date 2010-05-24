<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>

<html:html xhtml="true">
<body>
<c:choose>
	<c:when test = "${not empty requestScope.create_mode}">
<rhn:toolbar base="h1" img="/img/rhn-icon-info.gif" imgAlt="info.alt.img">
  <bean:message key="repos.jsp.toolbar"/>
</rhn:toolbar>
<h2><bean:message key="repos.jsp.header2"/></h2>
	</c:when>
	<c:otherwise>
<rhn:toolbar base="h1" img="/img/rhn-icon-info.gif" imgAlt="info.alt.img"
		               deletionUrl="RepoDelete.do?label=${contentSourceForm.map.label}"
               deletionType="snippets">
	<c:out value="${requestScope.snippet.displayName}"/>
</rhn:toolbar>
<h2><bean:message key="snippetdetails.jsp.header2"/></h2>
	</c:otherwise>
</c:choose>	


<c:choose>
	<c:when test="${empty requestScope.create_mode}">
		<c:set var="url" value ="/channels/manage/repos/RepoEdit"/>
	</c:when>
	<c:otherwise> 
		<c:set var="url" value ="/channels/manage/repos/RepoCreate"/>
	</c:otherwise>
</c:choose>


<div>
    <html:form action="${url}">
    <rhn:submitted/>
	<table class="details">
    <tr>
        <th>
        	<rhn:required-field key = "repos.jsp.create.label"/>
        </th>
        <td>
        		<html:text property="label"/>
            
            <c:if  test = "${empty requestScope.create_mode}">	            
            	<html:hidden property="oldLabel"/>
            </c:if>
        </td>
    </tr>	
    </table>
    
    <hr />

    <table align="right">
    	  <tr>
      		<td></td>
      		<c:choose>
      		<c:when test = "${empty requestScope.create_mode}">
      			<td align="right"><html:submit><bean:message key="snippetupdate.jsp.submit"/></html:submit></td>
      		</c:when>
      		<c:otherwise>
      			<td align="right"><html:submit><bean:message key="snippetcreate.jsp.submit"/></html:submit></td>
      		</c:otherwise>
      		</c:choose>
    	  </tr>
	</table>

    </html:form>
</div>

</body>
</html:html>

