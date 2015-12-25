package org.rr.expander.feed;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.rr.expander.feed.ExpressionParser;

public class ExpressionParserTest {

	@Test
	public void testZeroExpressionCount() {
		List<ExpressionParser> parsers = ExpressionParser.createExpressionParser("");
		Assert.assertEquals(0, parsers.size());
	}
	
	@Test
	public void testNullExpression() {
		List<ExpressionParser> parsers = ExpressionParser.createExpressionParser(null);
		Assert.assertEquals(0, parsers.size());
	}
	
	@Test
	public void testOneExpressionCount() {
		List<ExpressionParser> parsers = ExpressionParser.createExpressionParser("tag=div");
		Assert.assertEquals(1, parsers.size());
		ExpressionParser parser = parsers.get(0);
		
		Assert.assertEquals(1, parser.segmentCount());
	}
	
	@Test
	public void testMultipleExpressionCount() {
		List<ExpressionParser> parsers = ExpressionParser.createExpressionParser("tag=div/tag=div/tag=div");
		Assert.assertEquals(1, parsers.size());
		ExpressionParser parser = parsers.get(0);
		
		Assert.assertEquals(3, parser.segmentCount());
	}
	
	@Test
	public void testExpressionGetTag() {
		List<ExpressionParser> parsers = ExpressionParser.createExpressionParser("tag=div/tag=p");
		Assert.assertEquals(1, parsers.size());
		ExpressionParser parser = parsers.get(0);
		
		Assert.assertEquals("div", parser.getSegmentValue(0));
		Assert.assertEquals("p", parser.getSegmentValue(1));
	}
	
	@Test
	public void testExpressionGetId() {
		List<ExpressionParser> parsers = ExpressionParser.createExpressionParser("id=test1/id=test2");
		Assert.assertEquals(1, parsers.size());
		ExpressionParser parser = parsers.get(0);
		
		Assert.assertEquals("test1", parser.getSegmentValue(0));
		Assert.assertEquals("test2", parser.getSegmentValue(1));
	}
	
}