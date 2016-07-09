<#-- @ftlvariable name="" type="org.rr.expander.ExpanderUrlCreatorView" -->
<html>
	<head></head>
    <body>
        <h1>
	        <#if feedUrl?has_content>
				Your expanded url is<br/><code>${finalFeedUrl}</code>        	
	        <#else>
	        	Please enter a feed URL
			</#if>
        </h1>
    	<div style="width:800px">	        
			<form action="create" method="post">
		        <label for="feedUrl">Feed URL</label> 
		        <input type="text" name="feedUrl" value="${feedUrl}">
		 
		        <label for="includeCssSelector">Include expression</label>  
		        <input type="text" name="includeCssSelector" value="${includeCssSelector}">
		 
		        <label for="limit">Max. feed entries</label>  
		        <input type="text" name="limit" value="${limit}">
		
		        <button type="submit">Submit</button>
			</form>
			
			<h2>Usage</h2>
			<p>
				The <code>includes</code> expression, which selects a part of a 
				html page, describes a navigation path through the html tags.
				Each path element is separated by a slash and starts with the kind of
				element followed by a equality sign followed by the search value.
			</p>
			<ul>
				<li><code>id=someId</code> selects the tag with the id <code>someId</code> somewhere in the page.</li>
				<li><code>tag=div 3</code> selects the third div under the parent element. If no parent element is defined, the body element is automatically the parent.</li>
				<li><code>tag=*article</code> selects the first article tag somewhere in the page.</li>
			</ul>	  
			<p>	
				To include multiple page parts, additional expressions can be added by
				separating them with a `|` character. The following example expression
				select the first three div child tags of the parent tag with the id `someId`.
			</p>
			<ul>	  
				<li><code>id=someId/tag=div|id=someId/tag=div 2|id=someId/tag=div 3</code></li>
			</ul>
		</div>
    </body>
</html>
