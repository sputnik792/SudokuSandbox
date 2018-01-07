import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class AIsolve {
    private int[][] grid = new int[9][9];
    private Entry[][] possvals = new Entry[9][9] ;
    private int[][] sizes = new int[9][9]; //sizes of the possvals Entry lists
    private int count = 0;
    private String currstrat = "";
    private int removed = 0; //to be used as the alternate label for count; showing 'n' fewer possibilities
    private int reduce = 0; //if we end up using a reduction method 
                            //if reduce = 2, it means unsolved 
    private int[][] visited = new int[9][9]; //keeps track of which tiles are to be kept in consideration
    
    public AIsolve(int[][] g){
        for (int i = 0; i <= 8; i++){
            for (int j = 0; j <= 8; j++){
                possvals[i][j] = new Entry();
                grid[i][j] = g[i][j];
            }
        }
    }
    

    public int[][] solve(){
        reset_visits();
        count = 0;
        removed = 0;
        if (reduce == 2){
            reduce = 0;
        }
        if (singleOpt() > 0){
            if (reduce == 0){
                currstrat = "Single Option";
            }
            else {
                currstrat = "Single Option via " + currstrat;
                reduce = 0;
            }
            return grid;
        }
        else if (hiddenSingles() > 0){
            if (reduce == 0){
                currstrat = "Hidden Singles";
            }
            else {
                currstrat = "Hidden Singles via " + currstrat;
                reduce = 0;
            }
            return grid;
        }
        else if (nakedPairs() > 0){
            count = removed;
            if (reduce == 0){
                currstrat = "Naked Pairs Reduction";
                reduce = 1;
            }
            else {
                currstrat = "Naked Pairs Reduction via " + currstrat;
            }
            return grid;
        }
        else if (nakedTrips() > 0){
            if (reduce == 0){
                currstrat = "Naked Triples Reduction";
                reduce = 1;
            }
            else {
                currstrat = "Naked Triples Reduction via " + currstrat;
            }
            return grid;
        }
        else if (hiddenPairs() > 0){
            if (reduce == 0){
                currstrat = "Hidden Pairs Reduction";
                reduce = 1;
            }
            else {
                currstrat = "Hidden Pairs Reduction via " + currstrat;
            }
            return grid;
        }
        else if (hiddenTrips() > 0){
            if (reduce == 0){
                currstrat = "Hidden Triples Reduction";
                reduce = 1;
            }
            else {
                currstrat = "Hidden Triples Reduction via " + currstrat;
            }
            return grid;
        }
        else {
            unsolvable();
            reduce = 2;
            return grid;
        }
    }
    
    public int singleOpt(){
        for (int i = 0; i <= 8; i++){
            for (int j = 0; j <= 8; j++){
                if ((sizes[i][j] == 1)&&(grid[i][j] == 0)){
                    grid[i][j] = possvals[i][j].getEntries().get(0);
                    count++;
                }
            }
        }
        return count;
    }
    
    public int hiddenSingles(){ //different from singleOpt, a tile can have more than one possval, but still
                                // contain the only value valid for that box
                                // plan to have all reductions update the possval entries intrinsicallly
        int temp1;
        int z;
        ArrayList<Integer> list = new ArrayList<Integer>();
        int[] kp = new int[9];
        //it's easiest to first do this by row, then by column, then by box
        for (int i = 0; i < 9; i++){
            //first, add collective possvals of the row 
            for (int k = 0; k < 9; k++){
                list.addAll(possvals[i][k].getEntries());
            }
            //next, check how many instances of each value there are
            //specifically, if there is only one instance or more than one instance
            for (int w = 0; w < 9; w++){
                if (list.contains(w+1)){
                    temp1 = list.indexOf(w+1);
                    if (temp1 == list.lastIndexOf(w+1)){
                        kp[w] = 1;
                    }
                }
            }
            //now, we check all possvals of each cell to see if there is a 'single-instance' value
            for (int j = 0; j < 9; j++){
                z = 0;
                while (z < possvals[i][j].getEntries().size()){
                    if (kp[possvals[i][j].getEntries().get(z) - 1] == 1){
                        grid[i][j] = possvals[i][j].getEntries().get(z);
                        possvals[i][j].remove_all();
                        sizes[i][j] = 0;
                        count++;
                    }
                    z++;
                }
            }
            //reset kp
            for (int q = 0; q < 9; q++){
                kp[q] = 0;
            }
        }
        list = new ArrayList<Integer>();
        for (int j = 0; j < 9; j++){
            for (int k = 0; k < 9; k++){
                list.addAll(possvals[k][j].getEntries());
            }
            for (int w = 0; w < 9; w++){
                if (list.contains(w+1)){
                    temp1 = list.indexOf(w+1);
                    if (temp1 == list.lastIndexOf(w+1)){
                        kp[w] = 1;
                    }
                }
            }
            for (int i = 0; i < 9; i++){
                z = 0;
                while (z < possvals[i][j].getEntries().size()){
                    if (kp[possvals[i][j].getEntries().get(z) - 1] == 1){
                        grid[i][j] = possvals[i][j].getEntries().get(z);
                        possvals[i][j].remove_all();
                        sizes[i][j] = 0;
                        count++;
                    }
                    z++;
                }
            }
            for (int q = 0; q < 9; q++){
                kp[q] = 0;
            }
        }
        //boxes are slightly trickier
        list = new ArrayList<Integer>();
        for (int row = 0; row < 9; row = row+3){
            for (int col = 0; col > 9; col = col+3){
                //first two loops indicate the direction we're iterating through
                for (int f = row; f < row + 3; f++){
                    for (int g = col; g < col + 3; g++){
                        list.addAll(possvals[f][g].getEntries());
                    }
                }
                for (int w = 0; w < 9; w++){
                    if (list.contains(w+1)){
                        temp1 = list.indexOf(w+1);
                        if (temp1 == list.lastIndexOf(w+1)){
                            kp[w] = 1;
                        }
                    }
                }
                for (int i = row; i < row + 3; i++){
                    for (int j = col; j < col + 3; j++){
                        z = 0;
                        while (z < possvals[i][j].getEntries().size()){
                            if (kp[possvals[i][j].getEntries().get(z) - 1] == 1){
                                grid[i][j] = possvals[i][j].getEntries().get(z);
                                possvals[i][j].remove_all();
                                sizes[i][j] = 0;
                                count++;
                            }
                            z++;
                        }
                    }
                }
                for (int q = 0; q < 9; q++){
                    kp[q] = 0;
                }
            }
        }
        return count;
    }
    
    //essentially, all reductions are variations of updating possible values 
    public int nakedPairs(){
        //again, it's easier to do this by row, then column, then box
        //an important thing to note is that there can only be one naked group for each row, col, or box
        //but any naked group can determine the reductions for more than one section (row, col, and/or box).
        ArrayList<Integer> pair = new ArrayList<Integer>();
        boolean flag;
        //first, do it for the rows
        for (int i = 0; i < 9; i++){
            if (!pair.isEmpty()){
                pair.remove(1);
                pair.remove(0);
            }
            flag = false;
            int k = 0;
            while ((k < 9)&&(!flag)){
                if (possvals[i][k].get_size() == 2){
                    pair.addAll(possvals[i][k].getEntries());
                    for (int j = k+1; j < 9; j++){
                        if (possvals[i][j].get_size() == 2){
                            if (pair.containsAll(possvals[i][j].getEntries())){
                                // pair keeps the values
                                visited[i][j] = 1; //mark the 2 squares not to be reduced
                                visited[i][k] = 1;
                                //Systeintln("tile pair: "+i+","+j+" & "+i+","+k);
                                flag = true;
                            }
                        }
                    }
                    if (!flag){
                        pair.remove(1);
                        pair.remove(0);
                    }
                }
                k++;
            }
            //if we found a pair, reduce all other cells in the row
            if (flag){
                //System.out.println("values: "+pair.get(0)+","+pair.get(1));
                for (int j = 0; j < 9; j++){
                    if (visited[i][j] == 0){
                        if (possvals[i][j].getEntries().contains(pair.get(0))){
                            possvals[i][j].deleteObj(pair.get(0));
                            sizes[i][j]--;
                            removed++;
                            //System.out.println("removed "+pair.get(0)+" from tile "+i+","+j);
                        }
                        if (possvals[i][j].getEntries().contains(pair.get(1))){
                            possvals[i][j].deleteObj(pair.get(1));
                            sizes[i][j]--;
                            removed++;
                            //System.out.println("removed "+pair.get(1)+" from tile "+i+","+j);
                        }
                    }
                }
            }
        }
        if (!pair.isEmpty()){
            pair.remove(1);
            pair.remove(0);
        }
        reset_visits();
        //next, do it for columns
        for (int j = 0; j < 9; j++){
            if (!pair.isEmpty()){
                pair.remove(1);
                pair.remove(0);
            }
            flag = false;
            int k = 0;
            while ((k < 9)&&(!flag)){
                if (possvals[k][j].get_size() == 2){
                    pair.addAll(possvals[k][j].getEntries());
                    for (int i = k+1; i < 9; i++){
                        if (possvals[i][j].get_size() == 2){
                            if (pair.containsAll(possvals[i][j].getEntries())){
                                // pair keeps the values
                                visited[i][j] = 1; //mark the 2 squares not to be reduced
                                visited[k][j] = 1;
                                //System.out.println("tile pair: "+i+","+j+" & "+k+","+j);
                                flag = true;
                            }
                        }
                    }
                    if (!flag){
                        pair.remove(1);
                        pair.remove(0);
                    }
                }
                k++;
            }
            //if we found a pair, reduce all other cells in the column
            if (flag){
                //System.out.println("values: "+pair.get(0)+","+pair.get(1));
                for (int i = 0; i < 9; i++){
                    if (visited[i][j] == 0){
                        if (possvals[i][j].getEntries().contains(pair.get(0))){
                            possvals[i][j].deleteObj(pair.get(0));
                            sizes[i][j]--;
                            removed++;
                            //System.out.println("removed "+pair.get(0)+" from tile "+i+","+j);
                        }
                        if (possvals[i][j].getEntries().contains(pair.get(1))){
                            possvals[i][j].deleteObj(pair.get(1));
                            sizes[i][j]--;
                            removed++;
                            //System.out.println("removed "+pair.get(1)+" from tile "+i+","+j);
                        }
                    }
                }
            }
        }
        if (!pair.isEmpty()){
            pair.remove(1);
            pair.remove(0);
        }
        reset_visits();
        //finally, do it for all boxes
        //boxes are slightly trickier to handle
        for (int i = 0; i < 9; i = i+3){
            for (int j = 0; j < 9; j = j+3){
                if (!pair.isEmpty()){
                    pair.remove(1);
                    pair.remove(0);
                }
                flag = false;
                int k = i;
                int q = j;
                while ((k < i+3)&&(!flag)){
                    while ((q < j+3)&&(!flag)){
                        if (possvals[k][q].get_size() == 2){
                            pair.addAll(possvals[k][q].getEntries());
                            for (int r = i; r < i+3; r++){
                                for (int c = j; c < j+3; c++){
                                    if ((r != k)&&(c != q)){
                                        if (possvals[r][c].get_size() == 2){
                                            if (pair.containsAll(possvals[r][c].getEntries())){
                                                visited[r][c] = 1;
                                                visited[k][q] = 1;
                                                flag = true;
                                                //System.out.println("tile pair: "+k+","+q+" & "+r+","+c);
                                            }
                                        }
                                    }
                                }
                            }
                            if (!flag){
                                pair.remove(1);
                                pair.remove(0);
                            }
                        }
                        q++;
                    }
                    q = j;
                    k++;
                }
                //if we found a pair, reduce all other cells in the cox
                if (flag){
                    //System.out.println("values: "+pair.get(0)+","+pair.get(1));
                    for (int r = i; r < i+3; r++){
                        for (int c = j; c < j+3; c++){
                            if (visited[r][c] == 0){
                                if (possvals[r][c].getEntries().contains(pair.get(0))){
                                    possvals[r][c].deleteObj(pair.get(0));
                                    sizes[r][c]--;
                                    removed++;
                                    //System.out.println("removed "+pair.get(0)+" from tile "+r+","+c);
                                }
                                if (possvals[r][c].getEntries().contains(pair.get(1))){
                                    possvals[r][c].deleteObj(pair.get(1));
                                    sizes[r][c]--;
                                    removed++;
                                    //System.out.println("removed "+pair.get(1)+" from tile "+r+","+c);
                                }
                            }
                        }
                    }
                }

            }
        }
        return removed;
    }
    
    public int nakedTrips(){

        
        return removed;
    }
    
    public int hiddenPairs(){
        return count;
    }
    
    public int hiddenTrips(){
        return count;
    }
    
    public int pointingGroups(){
        return count;
    }
    
    
    //if we come across a puzzle with multiple solutions, the AI will just fill in a random tile
    public int unsolvable(){
        int i = 0;
        int j = 0;
        boolean flag = false;
        while ((i < 9)&&(!flag)){
            while ((j < 9)&&(!flag)){
                if (possvals[i][j].get_size() != 0){
                    grid[i][j] = possvals[i][j].getEntries().get(0);
                    flag = true;
                }
                j++;
            }
            j = 0;
            i++;
        }
        return count;
    }
    
    //Helper Functions
    public void updatePossvals(){
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 9; j++){
                if (grid[i][j] == 0){
                    list = getPossNumbs(grid, i, j);
                    //here, we don't want updatePossvals to add back in values that were already thrown out
                    //by other reduction algorithms
                    // however, make sure it still functions upon resetting the puzzle
                    if ((!(list.size() > possvals[i][j].get_size()))){
                        possvals[i][j].setEntries(getPossNumbs(grid, i, j));
                    }
                    list = new ArrayList<Integer>();
                }
                if (grid[i][j] > 0){
                    possvals[i][j].remove_all();
                }
            }
        }
    }
    
    public void updateSizes(){
        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 9; j++){
                sizes[i][j] = possvals[i][j].get_size();
                if (grid[i][j] > 0){
                    sizes[i][j] = 0;
                }
            }
        }
    }
    
    
    //no safeguard for already filled cells, needs to be done outside this function
    public ArrayList<Integer> getPossNumbs(int[][] grid, int i, int j){
        ArrayList<Integer> listup = new ArrayList<Integer>();
        for (int k = 1; k <= 9; k++){
            if (!((isinColumn(grid, k, i, j)||isinRow(grid, k, i, j)||isinBox(grid, k, i, j)))){
                listup.add(k);
            }
        }
        return listup;
    }
    
    public boolean noConflictInput(String str, int ycoord, int xcoord) {
        int str_to_int = Integer.parseInt(str);
        if (!(isinColumn(grid, str_to_int, ycoord, xcoord))&&!(isinRow(grid, str_to_int, ycoord, xcoord))&&
                !(isinBox(grid, str_to_int, ycoord, xcoord))){
            return true;
        }
        else{
            return false;
        }
    }
    
    public boolean isinColumn(int[][] grid, int e, int ycoord, int xcoord){
        int i = 0;
        while (i <= 8){
            if (grid[i][xcoord] == e){
                return true;
            }
            i++;
        }
        return false;
    }
    
    public boolean isinRow(int[][] grid, int e, int ycoord, int xcoord){
        int i = 0;
        while (i <= 8){
            if (grid[ycoord][i] == e){
                return true;
            }
            i++;
        }
        return false;
    }
    
    public boolean isinBox(int[][] grid, int e, int ycoord, int xcoord){
        int i = (ycoord / 3)*3;
        int j = (xcoord /3)*3;
        int max = i + 2;
        int max2 = j + 2;
        while (i <= max){
            while (j <= max2){
                if (grid[i][j] == e){
                    return true;
                }
                j++;
            }
            j = j - 3;
            i++;
        }
        return false;
    
    }
    
    public int[][] getGrid(){
        return this.grid;
    }
    
    
    public Entry[][] getPossvals(){
        return this.possvals;
    }
    
    public int getCount(){
        return this.count;
    }
    
    public String get_strat(){
        return this.currstrat;
    }
    
    public void setGrid(int[][] g){
        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 9; j++){
                (this.grid)[i][j] = g[i][j];
            }
        }
    }
    
    
    public void setPossvals(Entry[][] g){
        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 9; j++){
                this.possvals[i][j].setEntries(g[i][j].getEntries());
            }
        }
    }
    
    public int get_total(){
        int total = 0;
        for (int i = 0; i <= 8; i++){
            for (int j = 0; j <= 8; j++){
                if (grid[i][j] > 0){
                    total++;
                }
            }
        }
        return total;
    }
    
    public int get_type(){
        return this.reduce;
    }
    
    public void print_size_to_console(){
        System.out.println("sizes");
        for (int i = 0; i <= 8; i++){
            for (int j = 0; j <= 8; j++){
                System.out.print(sizes[i][j]);
                System.out.print(" ");
            }
            System.out.println("");
        }
    }
    
    public void print_grid_to_console(){
        System.out.println("gridvals");
        for (int i = 0; i <= 8; i++){
            for (int j = 0; j <= 8; j++){
                System.out.print(grid[i][j]);
                System.out.print(" ");
            }
            System.out.println("");
        }
    }
    
    public void print_visits_to_console(){
        System.out.println("visited");
        for (int i = 0; i <= 8; i++){
            for (int j = 0; j <= 8; j++){
                System.out.print(visited[i][j]);
                System.out.print(" ");
            }
            System.out.println("");
        }
    }
    
    public void reset_visits(){
        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 9; j++){
                visited[i][j] = 0;
            }
        }
    }
   
    public void reset_possvals(){
        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 9; j++){
                possvals[i][j].remove_all();
                possvals[i][j].resetEntries();
            }
        }
    }
    
    public void reset_AI(int[][] st){
        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 9; j++){
                visited[i][j] = 0;
                if (st[i][j] == 0){
                    grid[i][j] = 0;
                }
            }
        }
        currstrat = "";
        reduce = 0;
        reset_visits();
        reset_possvals();
        updateSizes();
        count = 0;
        removed = 0;
    }
}
