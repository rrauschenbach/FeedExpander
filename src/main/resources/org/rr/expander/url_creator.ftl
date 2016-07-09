<#-- @ftlvariable name="" type="org.rr.expander.ExpanderUrlCreatorView" -->
<html>
	<head>
		<style>
      		legend { font-size: 20px }
      		label.field {width:200px; float:left; font-weight:bold}
    	</style>
	</head>
    <body>
        <h2>
	        <#if feedUrl?has_content>
				Your expanded url is<br/><a href="${finalFeedUrl}"><code>${finalFeedUrl}</code></a>        	
	        <#else>
	        	Please enter a feed URL
			</#if>
        </h2>
    	<div style="width:800px">	        
			<form action="create" method="post">
				<fieldset>
					<legend>Feed parameter</legend>
			        <p>
			        <label class="field" for="feedUrl">Feed URL</label> 
			        <input class="field" type="text" name="feedUrl" value="${feedUrl}">
			 		<p>
			        <label class="field" for="includeCssSelector">CSS selector</label>  
			        <input class="field" type="text" name="includeCssSelector" value="${includeCssSelector}">
			 		<p>
			        <label class="field" for="limit">Max. feed entries</label>  
			        <input type="text" name="limit" max="3" value="${limit}">
			
			        <button type="submit">Submit</button>
		        </fieldset>
			</form>
			
			<h2>Usage</h2>
			<p>
				The <code>includes</code> expression, which selects a part of a 
				html page, describes a navigation path through the html tags.
			</p>
	      <h3>Selector overview</h3> 
	      <ul> 
	       <li><code>tagname</code>: find elements by tag, e.g. <code><a href="/apidocs/org/jsoup/select/Evaluator.CssNthEvaluator.html#a">a</a></code></li> 
	       <li><code>ns|tag</code>: find elements by tag in a namespace, e.g. <code>fb|name</code> finds <code>&lt;fb:name&gt;</code> elements</li> 
	       <li><code>#id</code>: find elements by ID, e.g. <code>#logo</code></li> 
	       <li><code>.class</code>: find elements by class name, e.g. <code>.masthead</code></li> 
	       <li><code>[attribute]</code>: elements with attribute, e.g. <code>[href]</code></li> 
	       <li><code>[^attr]</code>: elements with an attribute name prefix, e.g. <code>[^data-]</code> finds elements with HTML5 dataset attributes</li> 
	       <li><code>[attr=value]</code>: elements with attribute value, e.g. <code>[width=500]</code> (also quotable, like <code>[data-name='launch sequence']</code>)</li> 
	       <li><code>[attr^=value]</code>, <code>[attr$=value]</code>, <code>[attr*=value]</code>: elements with attributes that start with, end with, or contain the value, e.g. <code>[href*=/path/]</code></li> 
	       <li><code>[attr~=regex]</code>: elements with attribute values that match the regular expression; e.g. <code>img[src~=(?i)\.(png|jpe?g)]</code></li> 
	       <li><code>*</code>: all elements, e.g. <code>*</code></li> 
	      </ul> 
	      <h3>Selector combinations</h3> 
	      <ul> 
	       <li><code>el#id</code>: elements with ID, e.g. <code>div#logo</code></li> 
	       <li><code>el.class</code>: elements with class, e.g. <code>div.masthead</code></li> 
	       <li><code>el[attr]</code>: elements with attribute, e.g. <code>a[href]</code></li> 
	       <li>Any combination, e.g. <code>a[href].highlight</code></li> 
	       <li><code>ancestor child</code>: child elements that descend from ancestor, e.g. <code>.body p</code> finds <code>p</code> elements anywhere under a block with class "body"</li> 
	       <li><code>parent &gt; child</code>: child elements that descend directly from parent, e.g. <code>div.content &gt; p</code> finds <code>p</code> elements; and <code>body &gt; *</code> finds the direct children of the body tag</li> 
	       <li><code>siblingA + siblingB</code>: finds sibling B element immediately preceded by sibling A, e.g. <code>div.head + div</code></li> 
	       <li><code>siblingA ~ siblingX</code>: finds sibling X element preceded by sibling A, e.g. <code>h1 ~ p</code></li> 
	       <li><code>el, el, el</code>: group multiple selectors, find unique elements that match any of the selectors; e.g. <code>div.masthead, div.logo</code></li> 
	      </ul> 
	      <h3>Pseudo selectors</h3> 
	      <ul> 
	       <li><code>:lt(n)</code>: find elements whose sibling index (i.e. its position in the DOM tree relative to its parent) is less than <code>n</code>; e.g. <code>td:lt(3)</code></li> 
	       <li><code>:gt(n)</code>: find elements whose sibling index is greater than <code>n</code>; e.g. <code>div p:gt(2)</code></li> 
	       <li><code>:eq(n)</code>: find elements whose sibling index is equal to <code>n</code>; e.g. <code>form input:eq(1)</code></li> 
	       <li><code>:has(seletor)</code>: find elements that contain elements matching the selector; e.g. <code>div:has(p)</code></li> 
	       <li><code>:not(selector)</code>: find elements that do not match the selector; e.g. <code>div:not(.logo)</code></li> 
	       <li><code>:contains(text)</code>: find elements that contain the given text. The search is case-insensitive; e.g. <code>p:contains(jsoup)</code></li> 
	       <li><code>:containsOwn(text)</code>: find elements that directly contain the given text</li> 
	       <li><code>:matches(regex)</code>: find elements whose text matches the specified regular expression; e.g. <code>div:matches((?i)login)</code></li> 
	       <li><code>:matchesOwn(regex)</code>: find elements whose own text matches the specified regular expression</li> 
	       <li>Note that the above indexed pseudo-selectors are 0-based, that is, the first element is at index 0, the second at 1, etc</li> 
	      </ul> 
	      <p>See the <code><a href="/apidocs/org/jsoup/select/Selector.html" title="CSS-like element selector, that finds elements matching a query.">Selector</a></code> API reference for the full supported list and details.</p> 
		</div>
    </body>
</html>
