package org.rr.expander.feed;

import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.img;
import static j2html.TagCreator.text;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class PageContentExtractorTest {

	private static final String DUMMY_URL = "http://test.org";

	@Test
	public void testSelectHeadingTags() {
		PageContentExtractor extractor = new PageContentExtractor("h1");
		List<String> extractedPageElements = extractElements(extractor, 2);
		assertEquals("<h1>Heading1</h1>", extractedPageElements.get(0));
	}

	@Test
	public void testSelectSomethingNonExisting() {
		PageContentExtractor extractor = new PageContentExtractor("test");
		extractElements(extractor, 0);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testSelectWithWrongSyntax() {
		PageContentExtractor extractor = new PageContentExtractor(":a/b.d*");
		extractElements(extractor, -1);
	}
	
	private List<String> extractElements(PageContentExtractor extractor, int expectedElements) {
		List<String> extractedPageElements = extractor.extractPageElements(createHtmlSniplet(), DUMMY_URL);
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
