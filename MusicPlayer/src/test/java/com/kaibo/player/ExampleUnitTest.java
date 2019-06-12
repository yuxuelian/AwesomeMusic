package com.kaibo.player;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }


    @Test
    public void test() {
        String s = "123123123\n12312311661";
        String[] split = s.split("\n");
        for (String s1 : split) {
            System.out.println(s1);
        }
    }
}