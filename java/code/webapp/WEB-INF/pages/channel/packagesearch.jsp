<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>

<html:xhtml/>
<html>
<head>
<script type="text/javascript" src="/javascript/highlander.js"></script>
</head>
<body>

  <html:messages id="message" message="true">
    <rhn:messages><c:out escapeXml="false" value="${message}" /></rhn:messages>
  </html:messages>

  <html:errors />
  <rhn:toolbar base="h1" img="/img/rhn-icon-search.gif" imgAlt="packagesearch.jsp.imgAlt"
               helpUrl="/rhn/help/reference/en/s2-sm-software-search.jsp">
    <bean:message key="packagesearch.jsp.toolbar"/>
  </rhn:toolbar>

  <p><bean:message key="packagesearch.jsp.pagesummary"/></p>
  
  <p><bean:message key="packagesearch.jsp.instructions"/></p>
  
  <html:form action="/channels/software/Search.do">
  
  <!-- Search Box -->
    <div class="search-choices">

       <div class="search-choices-group">
         <table class="details">
           <tr><th><bean:message key="packagesearch.jsp.searchfor"/></th>
             <td>
               <input type="text" name="search_string" value="${search_string}" />
               <input type="image" src="/img/button-search.gif" name="Search!" />
             </td>
           </tr>
           <tr><th><bean:message key="packagesearch.jsp.whatsearch"/></th>
             <td>
               <div style="text-align: left">
                 <html:select property="view_mode" value="${view_mode}" >
	               <html:options collection="searchOptions"
	                             property="value"
	                             labelProperty="display" />
                 </html:select>
               </div>
             </td>
           </tr>
           <tr><th><bean:message key="packagesearch.jsp.wheresearch"/></th>
             <td rowspan="2">
               <div style="text-align: center">
                   <input type="checkbox" name="relevant" value="yes" onclick="javascript:highlander(this);"
                           <c:if test="${not empty relevant}">checked="checked"</c:if> />
                   <bean:message key="packagesearch.jsp.relevant"/></div>
               <div style="font-weight: bold; text-align: center">-or-</div>
               <div style="text-align: center">
                   <html:select property="channel_arch" multiple="multiple" size="5" onclick="javascript:highlander(this);">
	                   <html:options collection="channelArches"
	                                 property="value"
	                                 labelProperty="display" />
                   </html:select><br/>
           
               </div>
               <div style="text-align: center"><bean:message key="packagesearch.jsp.searchwherelegend"/></div>
             </td>
           </tr>
         </table>
       </div>

    </div>
    <input type="hidden" name="submitted" value="true" />
  </html:form> 

  <hr />
  <c:set var="pageList" value="${requestScope.pageList}" />
  <!-- collapse the params into a string -->
  <c:forEach items="${requestScope.channel_arch}" var="item">
    <c:set var="archparams" value="${archparams}&channel_arch=${item}"/>
  </c:forEach>
  <rl:listset name="searchSet">
    <rl:list name="searchResults" dataset="pageList"
             emptykey="packagesearch.jsp.nopackages" width="100%"
             filter="com.redhat.rhn.frontend.action.channel.PackageNameFilter">
      <rl:decorator name="PageSizeDecorator"/>
      <rl:column bound="false" sortable="false" headerkey="packagesearch.jsp.name" styleclass="first-column">
        <a href="/rhn/software/packages/NameOverview.do?package_name=${current.urlEncodedPackageName}${archparams}&search_subscribed_channels=${requestScope.relevant}">${current.packageName}</a>
      </rl:column>
      <rl:column bound="false" sortable="false" headerkey="packagesearch.jsp.summary" styleclass="last-column">
        <rhn:highlight tag="strong" text="${search_string}">
          ${current.summary}
        </rhn:highlight>
      </rl:column>
    </rl:list>

    <!-- there are two forms here, need to keep the formvars around for pagination -->
    <input type="hidden" name="submitted" value="true" />
    <input type="hidden" name="search_string" value="${search_string}" />
    <input type="hidden" name="view_mode" value="${view_mode}" />
    <input type="hidden" name="relevant" value="${relevant}" />
    <c:forEach items="${requestScope.channel_arch}" var="item">
    <input type="hidden" name="channel_arch" value="${item}" />
    </c:forEach>
    

  </rl:listset>
</body>
</html>
