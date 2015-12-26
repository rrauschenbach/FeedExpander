package org.rr.expander;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class UrlPatternManagerTest {

	@Test
	@Parameters({ 
		"www.abcdefg.de", 
		"www.mytest1.com", 
		"www.mytest1.de.",
		"mytest3.de/some"
		})	
	public void testSimplePatternNotContains(String url) {
		assertFalse(new UrlPatternManager().setUrlPatterns(createSimpleUrlPatternList()).containsUrl(url));
	}
	
	@Test
	@Parameters({ 
		"http://www.mytest1.de", 
		"www.mytest1.de", 
		"http://mytest1.de",
		"mytest1.de",
		"http://www.mytest2.de",
		"www.mytest2.de",
		"http://mytest2.de",
		"mytest2.de",
		"http://www.mytest3.de/some/path",
		"www.mytest3.de/some/path",
		"http://mytest3.de/some/path",
		"mytest3.de/some/path",
		"mytest3.de/some/path/"
		})
	public void testSimplePatternContains(String url) {
		assertTrue(new UrlPatternManager().setUrlPatterns(createSimpleUrlPatternList()).containsUrl(url));
	}
	
	@Test
	@Parameters({ 
		"http://www.mytest1.de",
		"www.mytest1.de/some/path",
		"http://sub.mytest2.de",
		"http://mytest2.de",
		"http://sub.mytest2.de",
		"http://mytest2.com",
		"http://sub.mytest2.com",
		"https://mytest3.de/some/path/",
		"https://mytest3.de/some/path/other",
		"www.mytest4.de/some/other/path",
		"www.mytest4.de/somewhre",
	})
	public void testWildcardPatternContains(String url) {
		assertTrue(new UrlPatternManager().setUrlPatterns(createWildcardUrlPatternList()).containsUrl(url));
	}
	
	private List<String> createSimpleUrlPatternList() {
		return new ArrayList<String>() {{
			add("# comment line");
			add("");
			add("http://www.mytest1.de");
			add("https://www.mytest2.de");
			add("www.mytest3.de/some/path");
		}};
	}
	
	private List<String> createWildcardUrlPatternList() {
		return new ArrayList<String>() {{
			add("# comment line");
			add("");
			add("http://www.mytest1.de*");
			add("https://*mytest2.de");
			add("https://*.mytest2.com");
			add("www.mytest3.de/some/path/*");
			add("www.mytest4.de/some*");
		}};
	}
}
