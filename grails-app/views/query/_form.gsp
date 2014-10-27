<%@ page import="au.org.ala.alerts.Query" %>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'name', 'error')} required">
    <label for="name">
        <g:message code="query.name.label" default="Name" />
        <span class="required-indicator">*</span>
    </label>
    <g:textField name="name" required="" value="${queryInstance?.name}" class="input-xxlarge"/>

</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'description', 'error')} ">
	<label for="description">
		<g:message code="query.description.label" default="Description" />
		
	</label>
	<g:textArea name="description" cols="80" class="input-xxlarge" rows="5" maxlength="400" value="${queryInstance?.description}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'dateFormat', 'error')} ">
	<label for="dateFormat">
		<g:message code="query.dateFormat.label" default="Date Format" />
		
	</label>
	<g:textField name="dateFormat" value="${queryInstance?.dateFormat}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'idJsonPath', 'error')} ">
	<label for="idJsonPath">
		<g:message code="query.idJsonPath.label" default="Id Json Path" />
		
	</label>
	<g:textField name="idJsonPath" class="input-xxlarge"  value="${queryInstance?.idJsonPath}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'recordJsonPath', 'error')} ">
	<label for="recordJsonPath">
		<g:message code="query.recordJsonPath.label" default="Record Json Path" />
		
	</label>
	<g:textField name="recordJsonPath" class="input-xxlarge"  value="${queryInstance?.recordJsonPath}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'updateMessage', 'error')} required">
	<label for="updateMessage">
		<g:message code="query.updateMessage.label" default="Update Message" />
		<span class="required-indicator">*</span>
	</label>
	<g:textArea name="updateMessage" required="" class="input-xxlarge"  value="${queryInstance?.updateMessage}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'queryPathForUI', 'error')} required">
	<label for="queryPathForUI">
		<g:message code="query.queryPathForUI.label" default="Query Path For UI" />
		<span class="required-indicator">*</span>
	</label>
	<g:textArea name="queryPathForUI" required="" class="input-xxlarge"  value="${queryInstance?.queryPathForUI}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'queryPath', 'error')} required">
	<label for="queryPath">
		<g:message code="query.queryPath.label" default="Query Path" />
		<span class="required-indicator">*</span>
	</label>
	<g:textArea name="queryPath" required="" class="input-xxlarge"  value="${queryInstance?.queryPath}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'baseUrl', 'error')} required">
	<label for="baseUrl">
		<g:message code="query.baseUrl.label" default="Base Url" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="baseUrl" required="" class="input-xxlarge"  value="${queryInstance?.baseUrl}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'baseUrlForUI', 'error')} required">
	<label for="baseUrlForUI">
		<g:message code="query.baseUrlForUI.label" default="Base Url For UI" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="baseUrlForUI" required="" class="input-xxlarge"  value="${queryInstance?.baseUrlForUI}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'custom', 'error')} ">
	<label for="custom">
		<g:message code="query.custom.label" default="Custom" />
		
	</label>
	<g:checkBox name="custom" value="${queryInstance?.custom}" />

</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'emailTemplate', 'error')} required">
	<label for="emailTemplate">
		<g:message code="query.emailTemplate.label" default="Email Template" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="emailTemplate" required="" value="${queryInstance?.emailTemplate}"/>

</div>


%{--<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'notifications', 'error')} ">--}%
	%{--<label for="notifications">--}%
		%{--<g:message code="query.notifications.label" default="Notifications" />--}%
	%{--</label>--}%
	%{----}%
%{--<ul class="one-to-many">--}%
%{--<g:each in="${queryInstance?.notifications?}" var="n">--}%
    %{--<li><g:link controller="notification" action="show" id="${n.id}">${n?.encodeAsHTML()}</g:link></li>--}%
%{--</g:each>--}%
%{--</ul>--}%


%{--</div>--}%

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'propertyPaths', 'error')} ">
	<label for="propertyPaths">
		<g:message code="query.propertyPaths.label" default="Property Paths" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${queryInstance?.propertyPaths?}" var="p">
    <li><g:link controller="propertyPath" action="show" id="${p.id}">${p?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="propertyPath" action="create" params="['query.id': queryInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'propertyPath.label', default: 'PropertyPath')])}</g:link>
</li>
</ul>


</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'queryResults', 'error')} ">
	<label for="queryResults">
		<g:message code="query.queryResults.label" default="Query Results" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${queryInstance?.queryResults?}" var="q">
    <li><g:link controller="queryResult" action="show" id="${q.id}">${q?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>


</div>

<div class="fieldcontain ${hasErrors(bean: queryInstance, field: 'resourceName', 'error')} required">
	<label for="resourceName">
		<g:message code="query.resourceName.label" default="Resource Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="resourceName" required="" value="${queryInstance?.resourceName}"/>

</div>

