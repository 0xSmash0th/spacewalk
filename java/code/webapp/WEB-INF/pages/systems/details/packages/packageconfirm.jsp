<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html:xhtml/>
<html>

<body>
<html:messages id="message" message="true">
    <rhn:messages><c:out escapeXml="false" value="${message}" /></rhn:messages>
</html:messages>

<%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>
    <h2>
      <img src="/img/rhn-icon-packages.gif" />
      <bean:message key="${requestScope.header}" />
    </h2>
    <rhn:systemtimemessage server="${requestScope.system}" />

<c:set var="pageList" value="${requestScope.pageList}" />

<rl:listset name="packageListSet">
	<rl:list dataset="pageList"
         width="100%"        
         name="packageList"
         styleclass="list"
         emptykey="packagelist.jsp.nopackages">
 			<rl:decorator name="PageSizeDecorator"/>

		  <rl:column headerkey="packagelist.jsp.packagename" bound="false"
		  	sortattr="nvre"
		  	sortable="true" filterattr="nvre" styleclass="first-column last-column">
		      <a href="/rhn/software/packages/Details.do?sid=${param.sid}&amp;id_combo=${current.idCombo}">
		        ${current.nvre}</a>
		  </rl:column>
	</rl:list>
 			
<c:if test="${not empty requestScope.pageList}">
      <div align="right">
      <c:if test="${requestScope.enableRemoteCommand}">
        <rhn:require mixins="com.redhat.rhn.common.security.acl.SystemAclHandler" acl="system_feature(ftr_remote_command); client_capable(script.run)">
          <hr />
		    <input type="submit" 
		    	name ="dispatch"
			    value='<bean:message key="removeconfirm.jsp.runremotecommand"/>'/>          
        </rhn:require>
      </c:if>
      
        <div align="left"> 
          <p><bean:message key="${widgetSummary}"/></p>
        </div>
        
      
        <table class="schedule-action-interface" align="center">
        
          <tr>
            <td><input type="radio" name="use_date" value="false" checked="checked" /></td>
            <th><bean:message key="confirm.jsp.now"/></th>
          </tr>
          <tr>
            <td><input type="radio" name="use_date" value="true"/></td>
            <th><bean:message key="confirm.jsp.than"/></th>
          </tr>
          <tr>
            <th><img src="/img/rhn-icon-schedule.gif" alt="<bean:message key="confirm.jsp.selection"/>"
                                                    title="<bean:message key="confirm.jsp.selection"/>"/>
            </th>
            <td>
              <jsp:include page="/WEB-INF/pages/common/fragments/date-picker.jsp">
                <jsp:param name="widget" value="date"/>
              </jsp:include>
            </td>
          </tr>
        </table>

        <hr />
		    <input type="submit" 
		    	name ="dispatch"
			    value='<bean:message key="installconfirm.jsp.confirm"/>'/>          
      </div>
      
      <input type="hidden" name="sid" value="${param.sid}" /></c:if> 			
 			
</rl:listset>
</body>
</html>
