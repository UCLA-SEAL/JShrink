package com.animoto.api;

import junit.framework.TestCase;

public class SongTest extends TestCase {
  public void testAttributes() {
    Song song = new Song();
    song.setSourceUrl("http://cold.play.com/in_my_place.mp3");
    assertEquals("http://cold.play.com/in_my_place.mp3", song.getSourceUrl()); 
    song.setStartTime(new Float(1.23));
    assertEquals(new Float(1.23), song.getStartTime(), 0);
    song.setDuration(new Float(3.45)); 
    assertEquals(new Float(3.45), song.getDuration());
  }
}
