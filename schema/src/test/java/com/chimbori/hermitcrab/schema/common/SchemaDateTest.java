package com.chimbori.hermitcrab.schema.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SchemaDateTest {
  @Test
  public void testFromString() {
    assertEquals(new SchemaDate(2017, 12, 31), SchemaDate.fromString("2017-12-31"));
  }

  @Test
  public void testToString() {
    assertEquals("2017-12-31", new SchemaDate(2017, 12, 31).toString());
  }
}
