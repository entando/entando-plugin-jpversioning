<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="/apsadmin-form" prefix="wpsf" %>

<ol class="breadcrumb page-tabs-header breadcrumb-position">
    <li><s:text name="breadcrumb.integrations"/></li>
    <li><s:text name="breadcrumb.integrations.components"/></li>
    <li><s:text name="jpversioning.admin.menu"/></li>
    <li class="page-title-container"><s:text name="title.jpversioning.config" /></li>
</ol

<div class="page-tabs-header">
    <div class="row">
        <div class="col-sm-12 col-md-6">
            <h1 class="page-title-container">
                <s:text name="jpversioning.admin.menu"/>
                <span class="pull-right">
                <a tabindex="0" role="button" data-toggle="popover" data-trigger="focus" data-html="true" title=""
                   data-content="<s:text name="jpversioning.admin.help"/>" data-placement="left" data-original-title="">
                    <i class="fa fa-question-circle-o" aria-hidden="true"></i>
                </a>
                </span>
            </h1>
        </div>
        <div class="col-sm-6">
            <ul class="nav nav-tabs nav-justified nav-tabs-pattern">
                <li>
                    <a href="<s:url action="list" namespace="/do/jpversioning/Content/Versioning" />"><s:text name="jpversioning.menu.contentList"/></a>
                </li>
                <li>
                    <a href="<s:url action="list" namespace="/do/jpversioning/Resource/Trash"><s:param name="resourceTypeCode" >Image</s:param></s:url>" ><s:text name="jpversioning.menu.images" /></a>
                </li>
                <li>
                    <a href="<s:url action="list" namespace="/do/jpversioning/Resource/Trash"><s:param name="resourceTypeCode" >Attach</s:param></s:url>" ><s:text name="jpversioning.menu.attaches" /></a>
                </li>
                <li class="active">
                    <a href="<s:url namespace="/do/jpversioning/Config" action="systemParams"></s:url>"><s:text name="jpversioning.menu.config" /></a>
                </li>
            </ul>
        </div>
    </div>
</div>
<br>

<div id="main">
    <s:form action="updateSystemParams" class="form-horizontal">
        <s:if test="hasActionErrors()">
            <div class="alert alert-danger alert-dismissable">
                <button type="button" class="close" data-dismiss="alert" aria-hidden="true">
                    <span class="pficon pficon-close"></span>
                </button>
                <span class="pficon pficon-error-circle-o"></span>
                <strong><s:text name="message.title.ActionErrors" />
                </strong>
                <ul class="margin-base-top">
                    <s:iterator value="actionErrors">
                        <li><s:property escapeHtml="false" /></li>
                        </s:iterator>
                </ul>
            </div>
        </s:if>
        <s:if test="hasActionMessages()">
            <div class="alert alert-success">
                <span class="pficon pficon-ok"></span>
                <strong><s:text name="messages.confirm" /></strong>
                <s:iterator value="actionMessages">
                    <li><s:property escapeHtml="false" /></li>
                    </s:iterator>
            </div>
        </s:if>
        
        <div class="form-group">
            <label class="display-block"><s:text name="jpversioning.label.deleteMidVersions" /></label>
            <div class="btn-group" data-toggle="buttons">
                <s:set var="paramName" value="'jpversioning_deleteMidVersions'" />
                <s:include value="/WEB-INF/apsadmin/jsp/admin/booleanParamBlock.jsp" />
                <wpsf:hidden name="%{#paramName + externalParamMarker}" value="true"/>
            </div>
        </div>
        <div class="form-group">
            <s:set var="jpversioning_paramName" value="'jpversioning_contentsToIgnore'" />
            <label class="control-label col-sm-2 text-right" for="<s:property value="#jpversioning_paramName"/>"><s:text name="jpversioning.label.contentsToIgnore" /></label>
            <div class="col-sm-9">
                <wpsf:textfield name="%{#jpversioning_paramName}" id="%{#jpversioning_paramName}" value="%{systemParams.get(#jpversioning_paramName)}" cssClass="form-control" />
                <wpsf:hidden name="%{#jpversioning_paramName + externalParamMarker}" value="true"/>
            </div>
        </div>
        <div class="form-group">
            <s:set var="jpversioning_paramName" value="'jpversioning_contentTypesToIgnore'" />
            <label class="control-label col-sm-2 text-right" for="<s:property value="#jpversioning_paramName"/>"><s:text name="jpversioning.label.contentTypesToIgnore" /></label>
            <div class="col-sm-9">
                <wpsf:textfield name="%{#jpversioning_paramName}" id="%{#jpversioning_paramName}" value="%{systemParams.get(#jpversioning_paramName)}" cssClass="form-control" />
                <wpsf:hidden name="%{#jpversioning_paramName + externalParamMarker}" value="true"/>
            </div>
        </div>
            
        <div class="col-xs-12">
            <div class="form-group pull-right">
                <div class="btn-group">
                    <wpsf:submit type="button" cssClass="btn btn-primary ">
                        <s:text name="label.save"/>
                    </wpsf:submit>
                </div>
            </div>
        </div>
    </s:form>
</div>
