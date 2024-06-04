import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class DirectedGraphTest {

    private DirectedGraph directedGraph;

    @Before
    public void setUp() {
        ArrayList<String> wordArrayList = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream("text.txt");
            int data;
            char letter, preLetter;
            String word = "";
            preLetter = ' ';
            while ((data = fis.read()) != -1) {
                letter = (char) data;
                if (Character.isLetter(letter)) {
                    word += letter;
                } else {
                    if (Character.isLetter(preLetter)) {
                        wordArrayList.add(word.toLowerCase());
                    }
                    word = "";
                }
                preLetter = letter;
            }
            if (!word.isEmpty()) {
                wordArrayList.add(word.toLowerCase());
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> singleWordsTemp = new ArrayList<>();
        for (String s : wordArrayList) {
            if (!singleWordsTemp.contains(s)) {
                singleWordsTemp.add(s);
            }
        }
        int wordCount = singleWordsTemp.size();
        HashMap<String, Integer> indexTemp = new HashMap<>();
        for (int i = 0; i < singleWordsTemp.size(); i++) {
            indexTemp.put(singleWordsTemp.get(i), i);
        }
        int[][] graphTemp = new int[wordCount][wordCount];
        String preWords = wordArrayList.get(0);
        for (int i = 1; i < wordArrayList.size(); i++) {
            graphTemp[indexTemp.get(preWords)][indexTemp.get(wordArrayList.get(i))]++;
            preWords = wordArrayList.get(i);
        }
        for (int i = 0; i < graphTemp.length; i++) {
            for (int j = 0; j < graphTemp.length; j++) {
                if (i != j && graphTemp[i][j] == 0) {
                    graphTemp[i][j] = Integer.MAX_VALUE;
                }
            }
        }
        directedGraph = new DirectedGraph(indexTemp, graphTemp, singleWordsTemp);
    }

    /*@Test
    public void testQueryBridgeWords(){
        Method method = null;
        try {
            method = DirectedGraph.class.getDeclaredMethod("queryBridgeWords", String.class, String.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        String result = null;
        try {
            result = (String) method.invoke(directedGraph, "please", "life");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertEquals("The bridge words from \"please\" to \"life\" is:love.", result);
        try {
            result = (String) method.invoke(directedGraph, "love", "life");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertEquals("The bridge words from \"love\" to \"life\" are:of and life.", result);
        try {
            result = (String) method.invoke(directedGraph, "so", "love");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertEquals("No bridge words from \"so\" to \"love\"!", result);
        try {
            result = (String) method.invoke(directedGraph, "love", "me");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertEquals("No \"me\" in the graph!", result);
        try {
            result = (String) method.invoke(directedGraph, "about", "me");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertEquals("No \"about\" and \"me\" in the graph!", result);
    }*/

    @Test
    public void testCalcShortestPath() {
        Method method = null;
        try {
            method = DirectedGraph.class.getDeclaredMethod("calcShortestPath", String.class, String.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        String result = null;
        try {
            result = (String) method.invoke(directedGraph, "about", "me");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertEquals("No \"about\" and \"me\" in the graph!", result);
        try {
            result = (String) method.invoke(directedGraph, "me", "love");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertEquals("No \"me\" in the graph!", result);
        try {
            result = (String) method.invoke(directedGraph, "love", "me");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertEquals("No \"me\" in the graph!", result);
        try {
            result = (String) method.invoke(directedGraph, "love", "please");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertEquals("No shortest path between \"love\" and \"please\" in the graph!", result);
        try {
            result = (String) method.invoke(directedGraph, "love", "life");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertEquals("success", result);
        try {
            result = (String) method.invoke(directedGraph, "me", "");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertEquals("No \"me\" in the graph!", result);
        try {
            result = (String) method.invoke(directedGraph, "please", "");
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        assertEquals("success", result);
    }

}
