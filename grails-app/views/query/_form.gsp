<%@ page import="au.org.ala.alerts.Query" %>

<!-- Name -->
<div class="mb-3">
	<label for="name" class="form-label">
		<g:message code="query.name.label" default="Name"/>
		<span class="text-danger">*</span>
	</label>
	<g:textField
			name="name"
			value="${queryInstance?.name}"
			required=""
			class="form-control ${hasErrors(bean: queryInstance, field: 'name', 'is-invalid')}"
	/>
</div>

<!-- Description -->
<div class="mb-3">
	<label for="description" class="form-label">
		<g:message code="query.description.label" default="Description"/>
	</label>
	<g:textArea
			name="description"
			rows="5"
			maxlength="400"
			value="${queryInstance?.description}"
			class="form-control ${hasErrors(bean: queryInstance, field: 'description', 'is-invalid')}"
	/>
</div>

<!-- Date Format -->
<div class="mb-3">
	<label for="dateFormat" class="form-label">
		<g:message code="query.dateFormat.label" default="Date Format"/>
	</label>
	<g:textField
			name="dateFormat"
			value="${queryInstance?.dateFormat}"
			class="form-control ${hasErrors(bean: queryInstance, field: 'dateFormat', 'is-invalid')}"
	/>
</div>

<!-- Id Json Path -->
<div class="mb-3">
	<label for="idJsonPath" class="form-label">
		<g:message code="query.idJsonPath.label" default="Id Json Path"/>
	</label>
	<g:textField
			name="idJsonPath"
			value="${queryInstance?.idJsonPath}"
			class="form-control ${hasErrors(bean: queryInstance, field: 'idJsonPath', 'is-invalid')}"
	/>
</div>

<!-- Record Json Path -->
<div class="mb-3">
	<label for="recordJsonPath" class="form-label">
		<g:message code="query.recordJsonPath.label" default="Record Json Path"/>
	</label>
	<g:textField
			name="recordJsonPath"
			value="${queryInstance?.recordJsonPath}"
			class="form-control ${hasErrors(bean: queryInstance, field: 'recordJsonPath', 'is-invalid')}"
	/>
</div>

<!-- Update Message -->
<div class="mb-3">
	<label for="updateMessage" class="form-label">
		<g:message code="query.updateMessage.label" default="Update Message"/>
		<span class="text-danger">*</span>
	</label>
	<g:textArea
			name="updateMessage"
			required=""
			value="${queryInstance?.updateMessage}"
			class="form-control ${hasErrors(bean: queryInstance, field: 'updateMessage', 'is-invalid')}"
	/>
</div>

<!-- Query Path For UI -->
<div class="mb-3">
	<label for="queryPathForUI" class="form-label">
		<g:message code="query.queryPathForUI.label" default="Query Path For UI"/>
		<span class="text-danger">*</span>
	</label>
	<g:textArea
			name="queryPathForUI"
			required=""
			value="${queryInstance?.queryPathForUI}"
			class="form-control ${hasErrors(bean: queryInstance, field: 'queryPathForUI', 'is-invalid')}"
	/>
</div>

<!-- Query Path -->
<div class="mb-3">
	<label for="queryPath" class="form-label">
		<g:message code="query.queryPath.label" default="Query Path"/>
		<span class="text-danger">*</span>
	</label>
	<g:textArea
			name="queryPath"
			required=""
			value="${queryInstance?.queryPath}"
			class="form-control ${hasErrors(bean: queryInstance, field: 'queryPath', 'is-invalid')}"
	/>
</div>

<!-- Base Url -->
<div class="mb-3">
	<label for="baseUrl" class="form-label">
		<g:message code="query.baseUrl.label" default="Base Url"/>
		<span class="text-danger">*</span>
	</label>
	<g:textField
			name="baseUrl"
			required=""
			value="${queryInstance?.baseUrl}"
			class="form-control ${hasErrors(bean: queryInstance, field: 'baseUrl', 'is-invalid')}"
	/>
</div>

<!-- Base Url For UI -->
<div class="mb-3">
	<label for="baseUrlForUI" class="form-label">
		<g:message code="query.baseUrlForUI.label" default="Base Url For UI"/>
		<span class="text-danger">*</span>
	</label>
	<g:textField
			name="baseUrlForUI"
			required=""
			value="${queryInstance?.baseUrlForUI}"
			class="form-control ${hasErrors(bean: queryInstance, field: 'baseUrlForUI', 'is-invalid')}"
	/>
</div>

<!-- Custom -->
<div class="mb-3 form-check">
	<g:checkBox
			name="custom"
			value="${queryInstance?.custom}"
			class="form-check-input"
			id="custom"
	/>
	<label for="custom" class="form-check-label">
		<g:message code="query.custom.label" default="Custom"/>
	</label>
</div>

<!-- Email Template -->
<div class="mb-3">
	<label for="emailTemplate" class="form-label">
		<g:message code="query.emailTemplate.label" default="Email Template"/>
		<span class="text-danger">*</span>
	</label>
	<g:textField
			name="emailTemplate"
			required=""
			value="${queryInstance?.emailTemplate}"
			class="form-control ${hasErrors(bean: queryInstance, field: 'emailTemplate', 'is-invalid')}"
	/>
</div>