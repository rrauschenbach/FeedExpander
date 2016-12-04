<#-- @ftlvariable name="" type="org.rr.expander.ExpanderUrlCreatorView" -->
<html>
	<head>
		<link rel="stylesheet" href="css/pure.css">
		<link rel="stylesheet" href="css/layout.css">
		
		<meta name="robots" content="noindex,nofollow"/>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
	</head>
    <body>
		<div class="header">
		    <div class="home-menu pure-menu pure-menu-horizontal pure-menu-fixed">
		        <a class="pure-menu-heading" href="/feeds">FeedExpander</a>
		
		        <ul class="pure-menu-list">
		            <li class="pure-menu-item"><a href="https://github.com/meerkatzenwildschein/FeedExpander" target="_blank" class="pure-menu-link">GitHub</a></li>
		        </ul>
		    </div>
		</div>

		<div class="content">
			<h2 class="content-head is-center">Expanded Feeds</h2>
			<table class="pure-table">
			    <thead>
			        <tr>
			            <th class="numeric-cell">#</th>
			            <th class="link-cell">Link</th>
			            <th>Description</th>
			        </tr>
			    </thead>
			
			    <tbody>
					<#list feedAliases as alias>
						<tr class="${(alias?index % 2 == 1)?then('pure-table-odd','')}">
							<td class="numeric-cell"><label>${alias?index + 1}<label></td>
							<td class="link-cell"><a class="pure-menu-link" href="${getFeedUrl(alias)}">${getFeedUrl(alias)}</a></td>
							<td><label>${getFeedDescription(alias)}</label></td>
						</tr>
					</#list>
			    </tbody>
			</table>
    	</div>
    	
		<div class="content">
			<h2 class="content-head is-center">Created Feeds</h2>
			<table class="pure-table">
			    <thead>
			        <tr>
			            <th class="numeric-cell">#</th>
			            <th class="link-cell">Link</th>
			            <th>Description</th>
			        </tr>
			    </thead>
			
			    <tbody>
					<#list pageAliases as alias>
						<tr class="${(alias?index % 2 == 1)?then('pure-table-odd','')}">
							<td class="numeric-cell"><label>${alias?index + 1}<label></td>
							<td class="link-cell"><a class="pure-menu-link" href="${getPageUrl(alias)}">${getPageUrl(alias)}</a></td>
							<td><label>${getPageDescription(alias)}</label></td>
						</tr>
					</#list>
			    </tbody>
			</table>
    	</div>
    </body>
</html>
