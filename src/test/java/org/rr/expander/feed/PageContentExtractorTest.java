package org.rr.expander.feed;

import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.img;
import static j2html.TagCreator.p;
import static j2html.TagCreator.text;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
	
	@Test
	public void testSelectAndGetGroups() {
		List<String> groupedPageElements = new PageContentExtractor(".headline,.article,.footer")
		.extractPageElements(createIterativeHtmlSniplet(), DUMMY_URL)
		.getGroupedPageElements();
		
		// there muste be two groups
		assertEquals(2, groupedPageElements.size());
		
		// test the content of group 1
		assertTrue(groupedPageElements.get(0).contains("headline1"));
		assertFalse(groupedPageElements.get(0).contains("headline2"));
		assertTrue(groupedPageElements.get(0).contains("text1"));
		assertFalse(groupedPageElements.get(0).contains("text2"));
		assertTrue(groupedPageElements.get(0).contains("footer1"));
		assertFalse(groupedPageElements.get(0).contains("footer2"));
		
		// test the content of group 2
		assertFalse(groupedPageElements.get(1).contains("headline1"));
		assertTrue(groupedPageElements.get(1).contains("headline2"));
		assertFalse(groupedPageElements.get(1).contains("text1"));
		assertTrue(groupedPageElements.get(1).contains("text2"));
		assertFalse(groupedPageElements.get(1).contains("footer1"));
		assertTrue(groupedPageElements.get(1).contains("footer2"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSelectAndGetGroupsNotFit() {
		new PageContentExtractor(".headline,.article,.footer,.footer")
		.extractPageElements(createIterativeHtmlSniplet(), DUMMY_URL)
		.getGroupedPageElements();
	}
	
	@Test
	public void testGetFirstElement() {
		String firstPageElement = new PageContentExtractor(".headline")
				.extractPageElements(createIterativeHtmlSniplet(), DUMMY_URL).getFirstPageElement();
		assertTrue(firstPageElement.contains("headline1"));
		assertFalse(firstPageElement.contains("headline2"));
	}
	
	@Test
	public void testGetFirstNonExistingElement() {
		String firstPageElement = new PageContentExtractor(".notexisting")
				.extractPageElements(createIterativeHtmlSniplet(), DUMMY_URL).getFirstPageElement();
		assertTrue(firstPageElement.isEmpty());
	}
	
	@Test
	public void testGetMergedElements() {
		String mergedElements = new PageContentExtractor(".headline")
				.extractPageElements(createIterativeHtmlSniplet(), DUMMY_URL).getMergedPageElements();
		assertTrue(mergedElements.contains("headline1"));
		assertTrue(mergedElements.contains("headline2"));
	}
	
	@Test
	public void testGetMergedElementsNotExists() {
		String mergedElements = new PageContentExtractor(".notexisting")
				.extractPageElements(createIterativeHtmlSniplet(), DUMMY_URL).getMergedPageElements();
		assertTrue(mergedElements.isEmpty());
	}
	
	private List<String> extractElements(PageContentExtractor extractor, int expectedElements) {
		List<String> extractedPageElements = extractor.extractPageElements(createSimpleHtmlSniplet(), DUMMY_URL)
				.getPageElements();
		assertEquals(expectedElements, extractedPageElements.size());
		return extractedPageElements;
	}
	
  public String createSimpleHtmlSniplet() {
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
  
  public String createIterativeHtmlSniplet() {
    return body().with(
    		div().with(h1("headline1")).attr("class", "headline"),
    		div().with(p("text1")).attr("class", "article"),
    		div().with(p("footer1")).attr("class", "footer"),
    		
    		div().with(h1("headline2")).attr("class", "headline"),
    		div().with(p("text2")).attr("class", "article"),
    		div().with(p("footer2")).attr("class", "footer")
    		).render();
  }

}
