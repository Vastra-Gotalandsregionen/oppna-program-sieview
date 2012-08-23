<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript" src="/regionportalen-theme/js/rp-patient-context.js"></script>

<style type="text/css">
    .sie-view-portlet-view .update-person-id-form {
        margin-bottom: 8px;
    }
</style>

<portlet:resourceURL var="resourceUrl" escapeXml="false"/>

<portlet:actionURL var="updatePersonId">

</portlet:actionURL>

<div class="sie-view-portlet-view">

    <form class="update-person-id-form" action="${updatePersonId}" method="post">
        Person-nr: <input type="text" value="${personId}" name="personId"/>
        <input type="submit" value="Uppdatera">
    </form>

    <iframe src="${iframeSrc}" width="100%" height="800px">

    </iframe>

</div>

<script type="text/javascript">

    AUI().ready('aui-dialog', function (A) {
        pollForNewPatient('<%= resourceUrl %>');
    });

</script>