<#-- @ftlvariable name="" type="org.rr.expander.ExpanderUrlCreatorView" -->
<html>
	<head>
		<meta name="robots" content="noindex,nofollow"/>
	</head>
    <body>
		<#list feedAliases as alias>
			${getDescription(alias)}<br/>
			<a href="${getFeedUrl(alias)}">${getFeedUrl(alias)}</a><br/>
			<br/>
		</#list>
    </body>
</html>
