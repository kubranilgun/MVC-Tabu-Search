import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.System;
import java.util.*;
import java.util.List;

public class Main {

    //Public değişkenler tanımlanır.
    public static Random rand = new Random();
    public static char[] vertices;
    public static char[][] edges;

    //Tabu listesi (kısa dönemli hafıza) tanımlanır.
    public static List<Tabu> shortTermMemory = new ArrayList<>();

    //Uzun dönemli hafıza tanımlanır.
    public static List<boolean[]> longTermMemory = new ArrayList<>();

    //En iyi çözüm tanımlanır.
    public static boolean[] bestSolution;

    public static void main(String[] args) throws IOException {

        //Çözülecek problem seçilir.
        selectProblem();

        //Başlangıç çözümü üretilir.
        bestSolution = generateInitialSolution();

        //Başlangıç çözümü uzun dönemli hafızaya alınır.
        longTermMemory.add(bestSolution);

        //Başlangıç çözümünün ceza puanı ekrana basılır.
        int penaltyPoint = evaluateSolution(bestSolution);
        System.out.println("Penalty point of initial solution: " + penaltyPoint + " \n___________________________");

        //Durma şartı sağlanana kadar en iyi çözüm aranır.
        boolean[] candidateSolution;
        boolean [] currentSolution;
        List<boolean[]> neighbours;
        List<Character> markedVertices = new ArrayList<>();
        int iteration = 0;
        while(!checkStopCondition()) {
            iteration++;
            System.out.println("Iteration number: " + (iteration+1));

            //Komşu çözümler belirlenir.
            neighbours = generateNeighbours();

            //En iyi komşu çözüm seçilerek aday çözüm yapılır.
            candidateSolution = selectBestNeighbour(neighbours);

            //Aday çözüm tabu değilse veya tabu yıkma kriterini karşılıyorsa aşağıdaki adımlara geçilir.
            if (!checkTabuCondition(candidateSolution) || checkTabuBreak(candidateSolution)){

                //Aday çözüm mevcut çözüm yapılır.
                currentSolution = candidateSolution;

                //Aday çözüm en az en iyi çözüm kadar iyiyse, en iyi çözüm yapılır.
                if (evaluateSolution(candidateSolution) <= evaluateSolution(bestSolution)) {
                    bestSolution = candidateSolution;

                    //Yeni en iyi çözüm ekrana basılır.
                    System.out.println("New best solution: ");
                    for (int j=0; j < bestSolution.length; j++) {
                        System.out.println(bestSolution[j]);
                    }

                    //Yeni en iyi çözümün işaretlenen düğümleri ekrana basılır.
                    markedVertices = new ArrayList<>();
                    for(int i=0; i<bestSolution.length; i++){
                        if (bestSolution[i]){
                            char vertex = vertices[i];
                            markedVertices.add(vertex);
                        }
                    }
                    System.out.println("Marked vertices of new best solution: ");
                    for(int i=0; i<markedVertices.size(); i++) {
                        System.out.println(markedVertices.get(i));
                    }
                }

                //İterasyondaki en iyi çözümün ceza puanı ekrana basılır.
                penaltyPoint = evaluateSolution(bestSolution);
                System.out.println("Penalty point of best solution: " + penaltyPoint + " \n___________________________");

                //Eklenmesi gereken tabu varsa tabu listesi güncellenir.
                updateTabuList(currentSolution);

                //En iyi çözüm uzun dönemli hafızaya eklenir.
                longTermMemory.add(bestSolution);
            }
        }

        //Nihai çözüm ekrana basılır.
        System.out.println("Ultimate solution:");
        for(int i=0; i<bestSolution.length; i++){
            System.out.println(bestSolution[i]);
        }

        //Nihai en iyi çözümün işaretlenen düğümleri ekrana basılır.
        markedVertices = new ArrayList<>();
        for(int i=0; i<bestSolution.length; i++){
            if (bestSolution[i]){
                char vertex = vertices[i];
                markedVertices.add(vertex);
            }
        }
        System.out.println("Marked vertices of ultimate solution: ");
        for(int i=0; i<markedVertices.size(); i++) {
            System.out.println(markedVertices.get(i));
        }

        //Nihai çözümün ceza puanı ekrana basılır.
        penaltyPoint = evaluateSolution(bestSolution);
        System.out.println("Penalty point of ultimate solution: " + penaltyPoint + " \n___________________________");
    }

    //Her kenarın iki ucundan rastgele bir düğüm seçilerek başlangıç çözümü üretilir.
    public static boolean[] generateInitialSolution(){

        //Problemde verilen grafiğin bütün kenarları dolaşılır, her bir kenarın iki ucundaki düğümlerden bir tanesi rastgele seçilir.
        List<Character> traversedVertices = new ArrayList<>();
        for(int i=0; i<edges.length; i++){
            traversedVertices.add(edges[i][rand.nextInt(2)]);
        }

        //System.out.println("Traversed vertices:");
        //for(int i=0; i<traversedVertices.size(); i++){
        //    System.out.println(traversedVertices.get(i));
        //}

        //Rastgele seçilirken yinelenen düğümlerin çıkarılır, işaretlenecek düğümler belirlenir.
        List<Character> selectedVertices = new ArrayList<>();
        for(int i=0; i<traversedVertices.size(); i++){
            boolean exist = false;
            for (int j=0; j<selectedVertices.size(); j++){
                if(traversedVertices.get(i) == selectedVertices.get(j)){
                    exist=true;
                    break;
                }
            }
            if (!exist)selectedVertices.add(traversedVertices.get(i));
        }

        //İşaretlenecek düğümler true, diğer düğümler false olacak şekilde set edilerek, başlangıç çözümü oluşturulur.
        boolean[] initialSolution = new boolean[vertices.length];
        for(int i=0; i<vertices.length; i++){
            boolean exist = false;
            for(int j=0; j<selectedVertices.size(); j++) {
                if(vertices[i]==selectedVertices.get(j)){
                    exist = true;
                    break;
                }
            }
            if(exist) initialSolution[i] = true;
            else initialSolution[i] = false;
        }

        //Başlangıç çözümü ekrana basılır.
        System.out.println("Initial solution:");
        for(int i=0; i<initialSolution.length; i++){
            System.out.println(initialSolution[i]);
        }

        //Başlangıç çözümünün işaretlenen düğümleri ekrana basılır.
        System.out.println("Marked vertices of initial solution:");
        for(int i=0; i<selectedVertices.size(); i++){
            System.out.println(selectedVertices.get(i));
        }

        return initialSolution;
    }

    //En iyi çözümün rastgele bir düğümü değiştirilerek komşu çözüm listesi oluşturulur.
    public static List<boolean[]> generateNeighbours(){
        boolean[] candidate;
        List<boolean[]> neighbours = new ArrayList<>();
        int randInt;
        for(int i=0; i<vertices.length; i++) {
            candidate = Arrays.copyOf(bestSolution, bestSolution.length);
            randInt = rand.nextInt(vertices.length);
            //candidate[i] = !candidate[i];
            //neighbours.add(candidate);
            candidate[randInt] = !candidate[randInt];
            neighbours.add(candidate);

            //System.out.println("Solution\tCandidate: ");
            //for(int j=0; j<candidate.length; j++) {
            //    System.out.println(bestSolution[j] + "\t\t" + candidate[j]);
            //}

        }

        return neighbours;
    }

    //Komşu çözüm listesindeki en iyi komşu belirlenir.
    public static boolean[] selectBestNeighbour(List<boolean[]> neighbours){
        boolean[] bestNeighbour = neighbours.get(0);
        for(int i=1; i<neighbours.size(); i++) {
            //System.out.println("Penalty point of neighbour solution: " + evaluateSolution(neighbours.get(i)) + " \n___________________________");
            if (evaluateSolution(neighbours.get(i))<evaluateSolution(bestNeighbour)){
                bestNeighbour = neighbours.get(i);
                break;
            }
        }

        return bestNeighbour;
    }

    //Aday çözümdeki değişikliğin tabu listesinde olup olmadığına bakılır.
    public static boolean checkTabuCondition(boolean[] candidateSolution){
        boolean tabu = false;
        for(int i=0; i<shortTermMemory.size(); i++){
            int vertexPlace = shortTermMemory.get(i).getVertexPlace();
            boolean changedTo = shortTermMemory.get(i).getChangedTo();

            //Yapılan değişikliğin tabu listesinde olup olmadığına bakılır.
            if(bestSolution[vertexPlace]==candidateSolution[vertexPlace]){
                if(bestSolution[vertexPlace]==!changedTo && (candidateSolution[vertexPlace])==changedTo)
                    tabu = true;
                break;
            }
        }

        return tabu;
    }

    //Aday çözümün tabu yıkma kriterini sağlayıp sağlamadığına bakılır.
    public static boolean checkTabuBreak(boolean[] candidateSolution){

        //Aday çözüm en az en iyi çözüm kadar iyiyse, tabu yıkma kriterini sağlamış sayılır.
        boolean breakTabu = false;
        if(evaluateSolution(candidateSolution)<evaluateSolution(bestSolution)){
            breakTabu = true;
        }

        return breakTabu;
    }

    //En iyi çözümde değişiklik olduysa, tabu listesi güncellenir.
    public static void updateTabuList(boolean[] newBestSolution){
        for(int i=0; i<vertices.length; i++){
            if(bestSolution[i]!=newBestSolution[i]){

                //Tabu listesindeki eleman sayısı problemdeki düğüm sayısına ulaşmışsa, ilk eleman tabu listesinden çıkarılır.
                if (shortTermMemory.size()==vertices.length) {
                    shortTermMemory.remove(0);
                }

                //Yeni tabu, tabu listesine eklenir.
                Tabu tabu = new Tabu();
                tabu.setVertexPlace(i);
                tabu.setChangedTo(newBestSolution[i]);
                shortTermMemory.add(tabu);
                break;
            }
        }
    }

    //Gelen çözüm grafikteki tüm kenarlara ulaşılma durumu ve örtülmüş düğüm sayısına göre değerlendirilir.
    public static int evaluateSolution (boolean[] solution){

        //Çözümdeki işaretli düğümlerin listesi çıkarılır.
        List<Character> markedVertices = new ArrayList<>();
        for(int i=0; i<vertices.length; i++){
            if (solution[i]){
                char vertex = vertices[i];
                markedVertices.add(vertex);
            }
        }

        //System.out.println("Marked vertices: ");
        //for(int i=0; i<markedVertices.size(); i++) {
        //    System.out.println(markedVertices.get(i));
        //}

        //Çözümün grafikteki tüm kenarlara ulaşıp ulaşmadığına bakılır.
        boolean graphCovered = true;
        for(int i=0; i<edges.length; i++){
            boolean edgeCovered = false;
            for(int j=0; j<markedVertices.size(); j++) {
                if (edges[i][0] == markedVertices.get(j) || edges[i][1] == markedVertices.get(j)){
                    edgeCovered = true;
                    break;
                }
            }
            if (!edgeCovered) {
                graphCovered = false;
                break;
            }
        }

        //Çözüm tüm kenarlara ulaşmıyorsa, çözüme problemdeki düğüm sayısının 10 katı kadar ceza puanı verilir.
        int penaltyPoint;
        if (graphCovered) penaltyPoint = 0;
        else penaltyPoint = vertices.length * 10;

        //Ceza puanı, çözümde seçili düğüm sayısı kadar arttırılır.
        penaltyPoint = penaltyPoint + markedVertices.size();

        return penaltyPoint;
    }

    //Çözülecek problem seçilir.
    public static void selectProblem() throws IOException {

        //Doğru seçim yapılana kadar, çözülecek problemin seçilmesi için kullanıcıdan sayı girmesi istenir.
        boolean selection = false;
        String path = new String();
        while(!selection) {
            System.out.println("Enter a number between 1 and 10 to select the problem to be solved: ");
            Scanner scanner = new Scanner(System.in);
            int problemNo = scanner.nextInt();
            switch (problemNo) {
                case 1:
                    vertices = new char[]{'a', 'b', 'c', 'd', 'e'};
                    edges = new char[][]{{'e', 'd'}, {'a', 'd'}, {'b', 'd'}, {'c', 'd'}};
                    path= "./res/grafik01.png";
                    selection = true;
                    break;
                case 2:
                    vertices = new char[]{'a', 'b', 'c', 'd', 'e'};
                    edges = new char[][]{{'e', 'd'}, {'a', 'd'}, {'b', 'd'}, {'c', 'd'}, {'b', 'c'}};
                    path= "./res/grafik02.png";
                    selection = true;
                    break;
                case 3:
                    vertices = new char[]{'a', 'b', 'c', 'd', 'e'};
                    edges = new char[][]{{'e', 'd'}, {'a', 'd'}, {'b', 'd'}, {'c', 'd'}, {'b', 'c'}, {'a', 'b'}};
                    path= "./res/grafik03.png";
                    selection = true;
                    break;
                case 4:
                    vertices = new char[]{'a', 'b', 'c', 'd', 'e'};
                    edges = new char[][]{{'e', 'd'}, {'a', 'd'}, {'b', 'd'}, {'c', 'd'}, {'b', 'c'}, {'a', 'b'}, {'a', 'c'}};
                    path= "./res/grafik04.png";
                    selection = true;
                    break;
                case 5:
                    vertices = new char[]{'a', 'b', 'c', 'd', 'e', 'f'};
                    edges = new char[][]{{'e', 'd'}, {'a', 'd'}, {'b', 'd'}, {'c', 'd'}, {'a', 'f'}};
                    path= "./res/grafik05.png";
                    selection = true;
                    break;
                case 6:
                    vertices = new char[]{'a', 'b', 'c', 'd', 'e', 'f'};
                    edges = new char[][]{{'e', 'd'}, {'a', 'd'}, {'b', 'd'}, {'c', 'd'}, {'b', 'c'}, {'a', 'f'}};
                    path= "./res/grafik06.png";
                    selection = true;
                    break;
                case 7:
                    vertices = new char[]{'a', 'b', 'c', 'd', 'e', 'f'};
                    edges = new char[][]{{'e', 'd'}, {'a', 'd'}, {'b', 'd'}, {'c', 'd'}, {'b', 'c'}, {'a', 'b'}, {'a', 'f'}};
                    path= "./res/grafik07.png";
                    selection = true;
                    break;
                case 8:
                    vertices = new char[]{'a', 'b', 'c', 'd', 'e', 'f'};
                    edges = new char[][]{{'e', 'd'}, {'a', 'd'}, {'b', 'd'}, {'c', 'd'}, {'b', 'c'}, {'a', 'b'}, {'a', 'c'}, {'a', 'f'}};
                    path= "./res/grafik08.png";
                    selection = true;
                    break;
                case 9:
                    vertices = new char[]{'a', 'b', 'c', 'd', 'e', 'f'};
                    edges = new char[][]{{'e', 'd'}, {'a', 'd'}, {'b', 'd'}, {'c', 'd'}, {'b', 'c'}, {'a', 'b'}, {'a', 'c'}, {'a', 'f'}, {'b', 'f'}};
                    path= "./res/grafik09.png";
                    selection = true;
                    break;
                case 10:
                    vertices = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
                    edges = new char[][]{{'e', 'd'}, {'a', 'd'}, {'b', 'd'}, {'c', 'd'}, {'b', 'c'}, {'a', 'b'}, {'a', 'c'}, {'a', 'f'}, {'b', 'f'}, {'c', 'f'}, {'b', 'g'}, {'g', 'h'}};
                    path= "./res/grafik10.png";
                    selection = true;
                    break;
                default:
                    System.out.println("There is no such number to select a problem.");
            }
        }

        //Seçilen grafiğin düğüm ve kenar sayısı ekrana basılır.
        System.out.println("Number of vertices of the given graph: " + vertices.length);
        System.out.println("Number of edges of the given graph: " + edges.length + " \n___________________________");

        //Seçilen grafiğe ait görsel ekrana basılır.
        BufferedImage image = ImageIO.read(new File(path));
        ImageIcon icon = new ImageIcon(image);
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(image.getWidth(), image.getHeight());
        JLabel label = new JLabel();
        label.setIcon(icon);
        frame.add(label);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    //İterasyonun durma şartı kontrol edilir.
    public static boolean checkStopCondition(){

        //En iyi çözümün ceza puanı, problemdeki düğüm sayısı kadar iterasyon boyunca sabit kaldıysa, durma şartı sağlanmış sayılır.
        boolean stop = true;
        if (longTermMemory.size()>=vertices.length){
            for(int i=longTermMemory.size()-vertices.length; i<longTermMemory.size()-1; i++){
                if(longTermMemory.get(i)!=longTermMemory.get(i+1)) {
                    stop=false;
                    break;
                }
            }
        } else{
            stop = false;
        }

        return stop;
    }
}
