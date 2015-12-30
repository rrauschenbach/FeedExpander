package org.rr.expander.feed;

import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.img;
import static j2html.TagCreator.text;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class PageContentExtractorTest {

	private static final String TEST_URL = "http://test.org";

	@Test
	public void testSimpleWildcardSelection() {
		PageContentExtractor extractor = new PageContentExtractor("tag=*h1");
		List<String> extractedPageElements = extractElements(extractor, 1);
		assertEquals("<h1>Heading1</h1>", extractedPageElements.get(0));
	}
	
	@Test
	public void testMultipleWildcardSelection() {
		PageContentExtractor extractor = new PageContentExtractor("tag=div/tag=*h1");
		List<String> extractedPageElements = extractElements(extractor, 1);
		assertEquals("<h1>Heading1</h1>", extractedPageElements.get(0));
	}
	
	@Test
	public void testMultipleHierarchySelection() {
		PageContentExtractor extractor = new PageContentExtractor("tag=div/tag=h1");
		List<String> extractedPageElements = extractElements(extractor, 1);
		assertEquals("<h1>Heading1</h1>", extractedPageElements.get(0));
	}
	
	@Test
	public void testSingleIdSelection() {
		PageContentExtractor extractor = new PageContentExtractor("id=id1");
		List<String> extractedPageElements = extractElements(extractor, 1);
		assertEquals("<div>content-id1<h1>Heading2</h1></div>", 
				deleteWhitespace(stripNewLines(extractedPageElements.get(0))));
	}
	
	@Test
	public void testIdWithChildSelection() {
		PageContentExtractor extractor = new PageContentExtractor("id=id1/tag=h1");
		List<String> extractedPageElements = extractElements(extractor, 1);
		assertEquals("<h1>Heading2</h1>", stripNewLines(extractedPageElements.get(0)));
	}
	
	@Test
	public void testMultipleElements() {
		PageContentExtractor extractor = new PageContentExtractor("tag=*h1|id=id1/tag=h1");
		List<String> extractedPageElements = extractElements(extractor, 2);
		assertEquals("<h1>Heading1</h1>", extractedPageElements.get(0));
		assertEquals("<h1>Heading2</h1>", extractedPageElements.get(1));
	}
	
	private String stripNewLines(String s) {
		return remove(s, "\n");
	}

	private List<String> extractElements(PageContentExtractor extractor, int expectedElements) {
		List<String> extractedPageElements = extractor.extractPageElements(createHtmlSniplet(), TEST_URL);
		assertEquals(expectedElements, extractedPageElements.size());
		return extractedPageElements;
	}
	
  public String createHtmlSniplet() {
    return body().with(
    		div().with(
    				h1("Heading1"), 
    				img().withSrc("img/hello.png")),
    		div().with(
    				div().withId("id1").with(
    						text("content-id1"), h1("Heading2"))),
    		div().with(text("third-div"))
    ).render();
}

}
