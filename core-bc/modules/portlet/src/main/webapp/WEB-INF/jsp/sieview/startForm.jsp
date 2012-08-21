<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript" src="/regionportalen-theme/js/rp-patient-context.js?a=cdd"></script>

<portlet:resourceURL var="resourceUrl" escapeXml="false"/>

<portlet:actionURL var="actionUrl" />

<form action="${actionUrl}" method="post">

Personnummer: <input name="personId" type="text" />
    <input type="submit" value="Go!" />

</form>

<script type="text/javascript">

    AUI().ready('aui-dialog', function (A) {
        pollForNewPatient('<%= resourceUrl %>');
    });

</script>