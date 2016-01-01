<#-- @ftlvariable name="" type="org.rr.expander.ExpanderUrlCreatorView" -->
<html>
	<head></head>
    <body>
        <h1>
	        <#if feedUrl?has_content>
				Your expanded url is <code>${feedUrl}</code>        	
	        <#else>
	        	Please enter a feed URL
			</#if>
        </h1>
        
		<form action="create" method="post">
	        <label for="feedUrl">Feed URL</label> 
	        <input type="text" name="feedUrl">
	 
	        <label for="includeExpression">Include Expression</label>  
	        <input type="text" name="includeExpression">
	
	        <button type="submit">Submit</button>
		</form>
		
		<h2>Usage</h2>
		<p>
			The <code>includes</code> expression, which selects a part of a <br/> 
			html page, describes a navigation path through the html tags.<br/>
			Each path element is separated by a slash and starts with the kind of<br/>
			element followed by a equality sign followed by the search value.<br/>
		</p>
		<ul>
			<li><code>id=someId</code> selects the tag with the id <code>someId</code> somewhere in the page.</li>
			<li><code>tag=div 3</code> selects the third div counted from the parent element.<br/>If no parent element is defined, the body element is automatically the parent.</li>
			<li><code>tag=*article</code> selects the first article tag somewhere in the page.</li>
		</ul>	  
			
		<p>	
			To include multiple page parts, additional expressions can be added by<br/>
			separating them with a `|` character. The following example expression<br/>
			select the first three div child tags of the parent tag with the id `someId`.<br/>
		</p>
		<ul>	  
			<li><code>id=someId/tag=div|id=someId/tag=div 2|id=someId/tag=div 3</code></li>
		</ul>
				
    </body>
</html>
