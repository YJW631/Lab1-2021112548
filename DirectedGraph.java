import javafx.util.Pair;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class DirectedGraph extends JPanel {

    int[][] graph;//有向图邻接矩阵

    HashMap<String, Integer> index;//记录图的顶点名和索引直接的对应关系

    ArrayList<String> singleWords;//记录文本中不重复的单词

    String bridgeWordsSentence;//记录所有桥接词

    List<Integer> shortestPathVertices;//记录最短路径

    private boolean[] dfsVisited;//用于在dfs算法中记录顶点是否被访问

    ArrayList<Pair<String, Integer>> allPath;//记录有向图中两点间所有可能的路径


    DirectedGraph(HashMap<String, Integer> index, int[][] graph, ArrayList<String> singleWords) {
        this.index = index;
        this.graph = graph;
        this.singleWords = singleWords;
        this.bridgeWordsSentence = "";
        this.shortestPathVertices = new ArrayList<>();
        this.dfsVisited = new boolean[graph.length];
        this.allPath = new ArrayList<>();
    }

    public static void main(String[] args) {
        /*用户输入文件名，读入文本并提取文本中的words*/
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入文本文件名：");
        String fileName = scanner.next();
        ArrayList<String> wordArrayList = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(fileName);
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
            if (word != "") {
                wordArrayList.add(word.toLowerCase());
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> singleWordsTemp = new ArrayList<>();
        for (int i = 0; i < wordArrayList.size(); i++) {//将不重复的单词存入singleWordsTemp中，最终传给singleWords。
            if (!singleWordsTemp.contains(wordArrayList.get(i))) {
                singleWordsTemp.add(wordArrayList.get(i));
            }
        }
        //记录单词和索引之间的对应关系
        int wordCount = singleWordsTemp.size();
        HashMap<String, Integer> indexTemp = new HashMap<String, Integer>();
        for (int i = 0; i < singleWordsTemp.size(); i++) {
            indexTemp.put(singleWordsTemp.get(i), i);
        }
        //用邻接矩阵存储有向图
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
        // 创建窗口并设置参数
        JFrame frame = new JFrame("Directed Graph");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(singleWordsTemp.size() * 100, 600);

        // 创建DirectedGraph实例并添加到窗口中
        DirectedGraph directedGraph = new DirectedGraph(indexTemp, graphTemp, singleWordsTemp);
        frame.add(directedGraph);

        //显示菜单栏
        while (true) {
            System.out.println("请选择功能:");
            System.out.println("1.读入文本并生成有向图");
            System.out.println("2.展示有向图");
            System.out.println("3.查询桥接词");
            System.out.println("4.根据桥接词生成新文本");
            System.out.println("5.计算两个单词之间的最短路径");
            System.out.println("51.计算两个单词之间的所有可能的最短路径");
            System.out.println("52.计算一个节点到图中其他任一节点的最短路径");
            System.out.println("6.随机游走");
            System.out.println("7.退出");

            int choice = scanner.nextInt();//记录菜单选项

            switch (choice) {
                case 1:
                    System.out.println("文本已读入并生成有向图！");
                    break;
                case 2:
                    directedGraph.showDirectedGraph(graphTemp, frame);//显示有向图
                    saveImage(frame, "directed_graph.png");//将有向图以图片的形式保存到磁盘
                    break;
                case 3:
                    //查询桥接词
                    String word1, word2;
                    System.out.println("查询桥接词:");
                    System.out.println("请输入word1:");
                    word1 = scanner.next();
                    System.out.println("请输入word2:");
                    word2 = scanner.next();
                    System.out.println(directedGraph.queryBridgeWords(word1, word2));
                    break;
                case 4:
                    //根据桥接词生成新文本
                    String inputText = "";
                    Scanner scanner1 = new Scanner(System.in);
                    System.out.println("请输入文本以生成新文本：");
                    if (scanner1.hasNextLine()) {
                        inputText = scanner1.nextLine();
                    }
                    System.out.println(directedGraph.generateNewText(inputText));
                    break;
                case 5:
                    //计算两个单词之间的最短路径
                    String srcWord, desWords;
                    System.out.println("请输入第一个单词:");
                    srcWord = scanner.next();
                    System.out.println("请输入第二个单词:");
                    desWords = scanner.next();
                    if (directedGraph.calcShortestPath(srcWord, desWords) == "success") {
                        JFrame reFrame = new JFrame("Directed Graph");
                        reFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        reFrame.setSize(singleWordsTemp.size() * 100, 600);
                        reFrame.add(directedGraph);
                        reFrame.setVisible(true);
                        saveImage(reFrame, "re_directed_graph.png");
                    }
                    break;
                case 51:
                    //计算两个单词之间的所有可能的最短路径
                    String srcWord1, desWords1;
                    System.out.println("请输入第一个单词:");
                    srcWord1 = scanner.next();
                    System.out.println("请输入第二个单词:");
                    desWords1 = scanner.next();
                    directedGraph.calcAllShortestPath(srcWord1, desWords1);
                    break;
                case 52:
                    //计算一个节点到图中其他任一节点的最短路径
                    String word;
                    System.out.println("请输入单词:");
                    word = scanner.next();
                    directedGraph.calcShortestPath(word, "");
                    break;
                case 6:
                    //随机游走
                    try {
                        File file = new File("randomText.txt");
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileOutputStream outStream = new FileOutputStream(file);
                        outStream.write(directedGraph.randomWalk().getBytes());
                        outStream.flush();
                        outStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 7:
                    //退出
                    return;
                default:
                    //输入了菜单选项以外的选项
                    System.out.println("无效的选择，请重新选择：");
            }
        }

    }

    //将图片保存到磁盘
    private static void saveImage(JFrame frame, String fileName) {
        Dimension size = frame.getContentPane().getSize();
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);//设置图片参数
        Graphics2D g2 = image.createGraphics();//创建图片对象
        frame.getContentPane().paint(g2);//将窗口frame上的内容绘制在图片上
        g2.dispose();//释放资源
        try {
            ImageIO.write(image, "png", new File(fileName));//将图片写入磁盘
            System.out.println("保存成功：" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //重写paintComponent,用于绘制有向图
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);//调用父类的paintComponent方法
        Graphics2D g2d = (Graphics2D) g;

        // 绘制顶点
        for (String word : index.keySet()) {
            int x = index.get(word) * 100; // x坐标
            int y = 300; // y坐标
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x, y, 30, 30); // 绘制圆形顶点
            g2d.setColor(Color.BLACK);
            g2d.drawString(word, x + 5, y + 20); // 绘制顶点标签
        }

        // 绘制边
        for (String word : index.keySet()) {
            int i = index.get(word);
            for (String targetWord : index.keySet()) {
                int j = index.get(targetWord);
                if (graph[i][j] > 0 && graph[i][j] != Integer.MAX_VALUE) {
                    int startX = i * 100 + 15; // 起始顶点x坐标
                    int endX = j * 100 + 15; // 目标顶点x坐标
                    int startY, endY, ctrlX, ctrlY;
                    if (startX - endX < 0) {
                        startY = 300; // 起始顶点y坐标
                        endY = 300; // 目标顶点y坐标
                        ctrlX = (startX + endX) / 2; // 控制点x坐标
                        ctrlY = (startY + endY) / 2 - Math.abs(endX - startX) / 4; // 控制点y坐标，可以根据需要调整曲线形状
                    } else {
                        startY = 300 + 30;// 起始顶点y坐标
                        endY = 300 + 30;// 目标顶点y坐标
                        ctrlX = (startX + endX) / 2; // 控制点x坐标
                        ctrlY = (startY + endY) / 2 + Math.abs(endX - startX) / 4; // 控制点y坐标，可以根据需要调整曲线形状
                    }
                    g2d.setColor(Color.BLACK);
                    QuadCurve2D curve = new QuadCurve2D.Double(startX, startY, ctrlX, ctrlY, endX, endY);//创建一个二次贝塞尔曲线对象
                    g2d.draw(curve);

                    // 绘制箭头
                    double angle = Math.atan2(endY - ctrlY, endX - ctrlX);//计算出箭头方向的角度
                    int arrowSize = 8;
                    int arrowX = (int) (endX - arrowSize * Math.cos(angle));//箭头尖部x坐标
                    int arrowY = (int) (endY - arrowSize * Math.sin(angle));//箭头尖部y坐标
                    g2d.drawLine(endX, endY, arrowX, arrowY);
                    int dx = (int) (arrowSize * Math.cos(angle + Math.PI / 6));//箭头两侧边缘x坐标偏移量
                    int dy = (int) (arrowSize * Math.sin(angle + Math.PI / 6));//箭头两侧边缘y坐标偏移量
                    g2d.drawLine(arrowX, arrowY, arrowX - dx, arrowY - dy);
                    g2d.drawLine(arrowX, arrowY, arrowX - dy, arrowY - dx);

                    g2d.setColor(Color.RED);

                    if (startX - endX < 0) {
                        g2d.drawString(String.valueOf(graph[i][j]), (startX + endX) / 2, (startY + endY) / 2 - Math.abs(endX - startX) / 10); // 绘制边权重
                    } else {
                        g2d.drawString(String.valueOf(graph[i][j]), (startX + endX) / 2, (startY + endY) / 2 + Math.abs(endX - startX) / 10); // 绘制边权重
                    }
                }
            }
        }

        g2d.setColor(Color.BLUE);
        //如果计算了最短路径，则将最短路径用蓝色标出
        for (int i = 0; i < shortestPathVertices.size() - 1; i++) {
            int startVertexIndex = shortestPathVertices.get(i);
            int endVertexIndex = shortestPathVertices.get(i + 1);

            int startX = startVertexIndex * 100 + 15;// 起始顶点x坐标
            int endX = endVertexIndex * 100 + 15;// 目标顶点x坐标
            int startY, endY, ctrlX, ctrlY;

            if (startX - endX < 0) {
                startY = 300; // 起始顶点y坐标
                endY = 300; // 目标顶点y坐标
                ctrlX = (startX + endX) / 2; // 控制点x坐标
                ctrlY = (startY + endY) / 2 - Math.abs(endX - startX) / 4; // 控制点y坐标，可以根据需要调整曲线形状
            } else {
                startY = 300 + 30;// 起始顶点y坐标
                endY = 300 + 30;// 目标顶点y坐标
                ctrlX = (startX + endX) / 2; // 控制点x坐标
                ctrlY = (startY + endY) / 2 + Math.abs(endX - startX) / 4; // 控制点y坐标，可以根据需要调整曲线形状
            }

            QuadCurve2D curve = new QuadCurve2D.Double(startX, startY, ctrlX, ctrlY, endX, endY);//创建一个二次贝塞尔曲线对象
            g2d.draw(curve);

            // 绘制箭头
            double angle = Math.atan2(endY - ctrlY, endX - ctrlX);//计算出箭头方向的角度
            int arrowSize = 8;
            int arrowX = (int) (endX - arrowSize * Math.cos(angle));//箭头尖部x坐标
            int arrowY = (int) (endY - arrowSize * Math.sin(angle));//箭头尖部y坐标
            g2d.drawLine(endX, endY, arrowX, arrowY);
            int dx = (int) (arrowSize * Math.cos(angle + Math.PI / 6));//箭头两侧边缘x坐标偏移量
            int dy = (int) (arrowSize * Math.sin(angle + Math.PI / 6));//箭头两侧边缘y坐标偏移量
            g2d.drawLine(arrowX, arrowY, arrowX - dx, arrowY - dy);
            g2d.drawLine(arrowX, arrowY, arrowX - dy, arrowY - dx);
        }

    }

    //显示有向图
    private void showDirectedGraph(int[][] graph, JFrame frame) {
        frame.setVisible(true);
    }

    //查询桥接词
    private String queryBridgeWords(String word1, String word2) {
        bridgeWordsSentence = "";
        if ((!singleWords.contains(word1)) && (!singleWords.contains(word2))) {//如果输入了文本外的单词
            return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
        } else if (!singleWords.contains(word1)) {
            return "No \"" + word1 + "\" in the graph!";
        } else if (!singleWords.contains(word2)) {
            return "No \"" + word2 + "\" in the graph!";
        }
        Set<String> bridgeWords = new HashSet<>();//存储所有桥接词
        for (int i = 0; i < graph[index.get(word1)].length; i++) {
            if (graph[index.get(word1)][i] > 0 && graph[index.get(word1)][i] < Integer.MAX_VALUE && graph[i][index.get(word2)] > 0 && graph[i][index.get(word2)] < Integer.MAX_VALUE) {
                bridgeWords.add(singleWords.get(i));
            }
        }
        if (bridgeWords.isEmpty()) {//如果没有桥接词
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        }
        String res = "";
        String[] bridgeWordsList = bridgeWords.toArray(new String[0]);
        if (bridgeWordsList.length == 1) {//如果只有一个桥接词
            res = bridgeWordsList[0];
        } else if (bridgeWordsList.length == 2) {//如果有两个桥接词
            res = bridgeWordsList[0] + " and " + bridgeWordsList[1];
        } else {//如果有三个及以上桥接词
            for (int i = 0; i < bridgeWordsList.length - 2; i++) {
                res += bridgeWordsList[i] + ",";
            }
            res += bridgeWordsList[bridgeWordsList.length - 2] + " and " + bridgeWordsList[bridgeWordsList.length - 1];
        }
        for (int i = 0; i < bridgeWordsList.length - 1; i++) {
            bridgeWordsSentence += bridgeWordsList[i] + " ";
        }
        bridgeWordsSentence += bridgeWordsList[bridgeWordsList.length - 1];
        if (bridgeWordsList.length == 1) {
            return "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" is:" + res + ".";
        }
        return "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are:" + res + ".";
    }

    //根据桥接词生成新文本
    private String generateNewText(String inputText) {
        String[] textList = inputText.split("\\s+");//将输入文本按空格进行分割
        String res = "";
        for (int i = 0; i < textList.length - 1; i++) {
            res += textList[i] + " ";
            queryBridgeWords(textList[i], textList[i + 1]);
            if (bridgeWordsSentence != "") {
                String[] bridgeWordsList = bridgeWordsSentence.split("\\s+");
                Random random = new Random();
                int bridgeWordsIndex=random.nextInt(bridgeWordsList.length);
                res += bridgeWordsList[bridgeWordsIndex]+" ";//插入桥接词
            }
        }
        res += textList[textList.length - 1];
        return res;
    }

    //计算两个单词之间的最短路径
    private String calcShortestPath(String word1, String word2) {
        int src, dst;
        if (word2 != "") {
            if ((!singleWords.contains(word1)) && (!singleWords.contains(word2))) {//如果单词1和单词2都不在文本中
                System.out.println("No \"" + word1 + "\" and \"" + word2 + "\" in the graph!");
                return "";
            } else if (!singleWords.contains(word1)) {//如果单词1不在文本中，单词2在文本中
                System.out.println("No \"" + word1 + "\" in the graph!");
                return "";
            } else if (!singleWords.contains(word2)) {//如果单词1在文本中，单词2不在文本中
                System.out.println("No \"" + word2 + "\" in the graph!");
                return "";
            }
            src = index.get(word1);
            dst = index.get(word2);
        } else {
            if (!singleWords.contains(word1)) {//如果单词1不在文本中
                System.out.println("No \"" + word1 + "\" in the graph!");
                return "";
            }
            src = index.get(word1);
            dst = -1;
        }
        if (dijkstra(src, dst) == -1) {//查找失败
            return "";
        }
        return "success";
    }

    //用dijkstra算法计算两个顶点间的最短路径
    private int dijkstra(int src, int dst) {
        int n = graph.length;
        int[] dist = new int[n];//记录src到各顶点的最短距离
        int[] lastNode = new int[n];//记录最短路径上的倒数第二个顶点
        boolean[] visited = new boolean[n];//顶点访问记录

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(lastNode, src);
        dist[src] = 0;

        for (int i = 0; i < n - 1; i++) {
            int u = minDistanceNodeIndex(dist, visited);
            visited[u] = true;

            for (int v = 0; v < n; v++) {
                if (!visited[v] && graph[u][v] != Integer.MAX_VALUE && dist[u] != Integer.MAX_VALUE && dist[u] + graph[u][v] < dist[v]) {
                    dist[v] = dist[u] + graph[u][v];
                    lastNode[v] = u;
                }
            }
        }
        if (dst >= 0) {//执行功能5，打印两个单词间的任意一条最短历经
            if (dist[dst] != Integer.MAX_VALUE) {
                printSolution(src, dst, lastNode, dist);
                shortestPathVertices.clear();//用于在有向图上用蓝色标注出最短路径
                int i = dst;
                while (i != src) {
                    shortestPathVertices.add(0, i);
                    i = lastNode[i];
                }
                shortestPathVertices.add(0, src);
                return 0;
            } else {
                System.out.println("\"" + singleWords.get(src) + "\" to \"" + singleWords.get(dst) + "\" are inaccessible.");
                return -1;
            }
        } else {//执行功能52，打印一个单词到图中所有其他单词的（任意一条）最短路径
            for (int i = 0; i < n; i++) {
                if (i != src) {
                    if (dist[i] != Integer.MAX_VALUE) {
                        printSolution(src, i, lastNode, dist);
                    } else {
                        System.out.println("\"" + singleWords.get(src) + "\" to \"" + singleWords.get(i) + "\" are inaccessible.");
                    }
                }
            }
        }
        return 0;

    }

    //计算两个单词之间所有可能的最短路径
    private void calcAllShortestPath(String word1, String word2) {
        int src, dst;
        if ((!singleWords.contains(word1)) && (!singleWords.contains(word2))) {//如果单词1和单词2都不在文本中
            System.out.println("No \"" + word1 + "\" and \"" + word2 + "\" in the graph!");
            return;
        } else if (!singleWords.contains(word1)) {//如果单词1不在文本中
            System.out.println("No \"" + word1 + "\" in the graph!");
            return;
        } else if (!singleWords.contains(word2)) {//如果单词2不在文本中
            System.out.println("No \"" + word2 + "\" in the graph!");
            return;
        }
        src = index.get(word1);
        dst = index.get(word2);
        dfs(src, dst, "", 0);
        if (allPath.size() == 0) {//没有路径
            System.out.println("\"" + singleWords.get(src) + "\" to \"" + singleWords.get(dst) + "\" are inaccessible.");
        } else {//打印所有路径中所有的最短路径
            int minLength = Integer.MAX_VALUE;
            for (int i = 0; i < allPath.size(); i++) {//找到最短路径长度
                if (allPath.get(i).getValue() < minLength) {
                    minLength = allPath.get(i).getValue();
                }
            }
            for (int i = 0; i < allPath.size(); i++) {//打印所有最短路径
                if (allPath.get(i).getValue() == minLength) {
                    String[] path = allPath.get(i).getKey().split("\\s+");
                    String aPath = path[1];
                    for (int i1 = 2; i1 < path.length; i1++) {
                        aPath += "->" + path[i1];
                    }
                    System.out.println(aPath + "  最短距离:" + allPath.get(i).getValue());
                }
            }
            allPath.clear();
        }
    }

    //使用深度优先搜索算法计算两个单词间所有可能的最短路径
    private void dfs(int current, int dst, String path, int length) {
        dfsVisited[current] = true;//顶点访问记录
        path += " " + singleWords.get(current);
        if (current == dst) {
            allPath.add(new Pair<>(path, length));//找到一条路径，加入集合中
        } else {
            for (int i = 0; i < graph.length; i++) {
                if (graph[current][i] > 0 && graph[current][i] < Integer.MAX_VALUE && !dfsVisited[i]) {
                    dfs(i, dst, path, length + graph[current][i]);
                }
            }
        }
        dfsVisited[current] = false;
    }

    // 找到未访问顶点中距离起点最近的顶点
    private int minDistanceNodeIndex(int[] dist, boolean[] visited) {
        int min = Integer.MAX_VALUE;
        int minIndex = -1;

        for (int v = 0; v < dist.length; v++) {
            if (!visited[v] && dist[v] <= min) {
                min = dist[v];
                minIndex = v;
            }
        }
        return minIndex;
    }

    // 打印最短路径
    private void printSolution(int src, int dst, int[] lastNode, int[] dist) {
        String path = "";
        int i = dst;
        path = singleWords.get(i) + path;
        while (i != src) {
            i = lastNode[i];
            path = singleWords.get(i) + "->" + path;
        }
        System.out.println(path + "  最短长度:" + dist[dst]);
    }

    //随机游走
    private String randomWalk() {
        Random random = new Random();
        int srcNo = random.nextInt(singleWords.size());//随机选取起始单词
        Set<Pair<String, String>> set = new HashSet<>();
        String reText = "";
        reText += singleWords.get(srcNo);
        while (true) {
            int flag = 0;
            for (int i = 0; i < singleWords.size(); i++) {
                if (graph[srcNo][i] != 0 && graph[srcNo][i] != Integer.MAX_VALUE) {
                    flag = 1;
                }
            }
            if (flag == 0) {//此时没有可达的下一个单词，则结束游走
                break;
            }
            int nextNo = random.nextInt(singleWords.size());//速记选取下一步的单词
            while (graph[srcNo][nextNo] == 0 || graph[srcNo][nextNo] == Integer.MAX_VALUE) {
                nextNo = random.nextInt(singleWords.size());
            }
            if (set.contains(new Pair<String, String>(singleWords.get(srcNo), singleWords.get(nextNo)))) {//如果路径重复，则结束游走
                reText += " " + singleWords.get(nextNo);
                break;
            }
            set.add(new Pair<String, String>(singleWords.get(srcNo), singleWords.get(nextNo)));
            reText += " " + singleWords.get(nextNo);
            srcNo = nextNo;//更新起始单词
            System.out.println("是否提前结束(y/n):");//询问是否提前结束游走
            Scanner scanner2 = new Scanner(System.in);
            String sFlag = scanner2.next();
            if (sFlag.equals("y")) {
                return reText;
            }
        }
        return reText;
    }
}