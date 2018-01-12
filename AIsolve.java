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
	private String[] letter_axis = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
	private ArrayList<String> AIlog = new ArrayList<String>(); //for outputting all AI steps 
	private ArrayList<TripleTup<Integer, Integer, Tuple<Integer, Integer>>> tile_log = 
			new ArrayList<TripleTup<Integer, Integer, Tuple<Integer, Integer>>>(); //this keeps a record of all 
																//values and their tiles, and the type of action
	
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
			count = removed;
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
			count = removed;
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
			count = removed;
			if (reduce == 0){
				currstrat = "Hidden Triples Reduction";
				reduce = 1;
			}
			else {
				currstrat = "Hidden Triples Reduction via " + currstrat;
			}
			return grid;
		}
		else if (pointingPairs() > 0){
			count = removed;
			if (reduce == 0){
				currstrat = "Pointing Pairs Reduction";
				reduce = 1;
			}
			else {
				currstrat = "Pointing Pairs Reduction via "+currstrat;
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
					grid[i][j] = possvals[i][j].get(0);
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
			list = new ArrayList<Integer>();
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
				while (z < possvals[i][j].get_size()){
					if (kp[possvals[i][j].get(z) - 1] == 1){
						grid[i][j] = possvals[i][j].get(z);
						possvals[i][j].remove_all();
						sizes[i][j] = 0;
						count++;
						updatePossvals();
					}
					z++;
				}
			}
			//reset kp
			for (int q = 0; q < 9; q++){
				kp[q] = 0;
			}
		}
		//now for columns
		for (int j = 0; j < 9; j++){
			list = new ArrayList<Integer>();
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
				while (z < possvals[i][j].get_size()){
					if (kp[possvals[i][j].get(z) - 1] == 1){
						grid[i][j] = possvals[i][j].get(z);
						possvals[i][j].remove_all();
						sizes[i][j] = 0;
						count++;
						updatePossvals();
					}
					z++;
				}
			}
			for (int q = 0; q < 9; q++){
				kp[q] = 0;
			}
		}
		//boxes are slightly trickier
		for (int row = 0; row < 9; row = row+3){
			for (int col = 0; col > 9; col = col+3){
				list = new ArrayList<Integer>();
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
						while (z < possvals[i][j].get_size()){
							if (kp[possvals[i][j].get(z) - 1] == 1){
								grid[i][j] = possvals[i][j].get(z);
								possvals[i][j].remove_all();
								sizes[i][j] = 0;
								count++;
								updatePossvals();
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
								//System.out.println("tile pair: "+i+","+j+" & "+i+","+k);
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
						if (possvals[i][j].isIn(pair.get(0))){
							possvals[i][j].deleteObj(pair.get(0));
							sizes[i][j]--;
							removed++;
							//System.out.println("removed "+pair.get(0)+" from tile "+i+","+j);
						}
						if (possvals[i][j].isIn(pair.get(1))){
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
						if (possvals[i][j].isIn(pair.get(0))){
							possvals[i][j].deleteObj(pair.get(0));
							sizes[i][j]--;
							removed++;
							//System.out.println("removed "+pair.get(0)+" from tile "+i+","+j);
						}
						if (possvals[i][j].isIn(pair.get(1))){
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
								if (possvals[r][c].isIn(pair.get(0))){
									possvals[r][c].deleteObj(pair.get(0));
									sizes[r][c]--;
									removed++;
									//System.out.println("removed "+pair.get(0)+" from tile "+r+","+c);
								}
								if (possvals[r][c].isIn(pair.get(1))){
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
		//similar to naked pairs, but with more cases
		// There are 4 cases, with the sizes of the three possval entries being one of 
		// (3,3,3), (3,3,2), (3,2,2) or (2,2,2)
		// run a search for the first 3 cases, and then finally run a search for the last case if the other 
		// cases weren't found
		// again, at most one triple for each row, column, or box,
		//first, by row
		ArrayList<Integer> triple = new ArrayList<Integer>();
		boolean flag;
		boolean check;
		boolean check2;
		int temp;
		int k;
		for (int i = 0; i < 9; i++){
			while (!triple.isEmpty()){
				triple.remove(0);
			}
			flag = false;
			k = 0;
			while ((k < 9)&&(!flag)){ //for the first 3 cases
				if (possvals[i][k].get_size() == 3){
					triple.addAll(possvals[i][k].getEntries());
					temp = 10; //out of range
					check = false; //true - found a pair
					check2 = false; //a little mechanic to keep the same loop running
					for (int j = 0; j < 9; j++){
						if (j != k){
							if (!check){
								if ((possvals[i][j].get_size() == 2)||(possvals[i][j].get_size() == 3)){
									if (triple.containsAll(possvals[i][j].getEntries())){
										check = true;
										temp = j; //found one
									}
								}
							}
							if (check2){ //
								if ((possvals[i][j].get_size() == 2)||(possvals[i][j].get_size() == 3)){
									if (triple.containsAll(possvals[i][j].getEntries())){
										visited[i][temp] = 1;
										visited[i][j] = 1;
										visited[i][k] = 1;
										flag = true;
										//System.out.println("triple: ");
										//System.out.println("i,temp: "+i+","+temp);
										//System.out.println("i,j: "+i+","+j);
										//System.out.println("i,k: "+i+","+k);
									}
								}
							}
						}
						if (check){
							check2 = true;
						}
					}
					if (!flag){
						while (!triple.isEmpty()){
							triple.remove(0);
						}	
					}
				}
				k++;
			}
			//for the last case (2,2,2), remember that all 3 tiles have to be different sets of values
			if (!flag){ //if no triple has been found yet
				k = 0;
				while (!triple.isEmpty()){
					triple.remove(0);
				}
				while ((k < 9)&&(!flag)){
					if (possvals[i][k].get_size() == 2){
						triple.addAll(possvals[i][k].getEntries());
						temp = 10; 
						check = false;
						check2 = false; 
						for (int j = 0; j < 9; j++){
							if (j != k){
								if (!check){
									if (possvals[i][j].get_size() == 2){
										if (triple.contains(possvals[i][j].get(0))){
											triple.add(possvals[i][j].get(1));
											check = true;
											temp = j;
										}
										if (triple.contains(possvals[i][j].get(1))){
											triple.add(possvals[i][j].get(0));
											check = true;
											temp = j;
										}
									}
								}
								if (check2){
									if (possvals[i][j].get_size() == 2){
										if (triple.containsAll(possvals[i][j].getEntries())){
											visited[i][temp] = 1;
											visited[i][j] = 1;
											visited[i][k] = 1;
											flag = true;
											//System.out.println("triple: ");
											//System.out.println("i,temp: "+i+","+temp);
											//System.out.println("i,j: "+i+","+j);
											//System.out.println("i,k: "+i+","+k);
										}
									}
								}
							}
							if (check){
								check2 = true;
							}
						}
						if (!flag){
							while (!triple.isEmpty()){
								triple.remove(0);
							}
						}
					}
					k++;
				}
			}
			//if we found a triple, we need to reduce all other cells in the row
			if (flag){
				//System.out.println("values: "+triple.get(0)+","+triple.get(1)+","+triple.get(2));
				for (int j = 0; j < 9; j++){
					if (visited[i][j] == 0){
						if (possvals[i][j].isIn(triple.get(0))){
							possvals[i][j].deleteObj(triple.get(0));
							sizes[i][j]--;
							removed++;
							//System.out.println("removed "+triple.get(0)+" from tile "+i+","+j);
						}
						if (possvals[i][j].isIn(triple.get(1))){
							possvals[i][j].deleteObj(triple.get(1));
							sizes[i][j]--;
							removed++;
							//System.out.println("removed "+triple.get(1)+" from tile "+i+","+j);
						}
						if (possvals[i][j].isIn(triple.get(2))){
							possvals[i][j].deleteObj(triple.get(2));
							sizes[i][j]--;
							removed++;
							//System.out.println("removed "+triple.get(2)+" from tile "+i+","+j);
						}
					}
				}
			}
		}

		reset_visits();
		//then we do it by column
		for (int j = 0; j < 9; j++){
			while (!triple.isEmpty()){
				triple.remove(0);
			}
			flag = false;
			k = 0;
			while ((k < 9)&&(!flag)){
				if (possvals[k][j].get_size() == 3){
					triple.addAll(possvals[k][j].getEntries());
					temp = 10; 
					check = false;
					check2 = false; 
					for (int i = 0; i < 9; i++){
						if (i != k){
							if (!check){
								if ((possvals[i][j].get_size() == 2)||(possvals[i][j].get_size() == 3)){
									if (triple.containsAll(possvals[i][j].getEntries())){
										check = true;
										temp = i; //found one
									}
								}
							}
							if (check2){ //
								if ((possvals[i][j].get_size() == 2)||(possvals[i][j].get_size() == 3)){
									if (triple.containsAll(possvals[i][j].getEntries())){
										visited[temp][j] = 1;
										visited[i][j] = 1;
										visited[k][j] = 1;
										flag = true;
										//System.out.println("triple: ");
										//System.out.println("temp,j: "+temp+","+j);
										//System.out.println("i,j: "+i+","+j);
										//System.out.println("k,j: "+k+","+j);
									}
								}
							}
						}
						if (check){
							check2 = true;
						}
					}
					if (!flag){
						while (!triple.isEmpty()){
							triple.remove(0);
						}	
					}
				}
				k++;
			}
			//for the last case
			if (!flag){
				while (!triple.isEmpty()){
					triple.remove(0);
				}	
				k = 0;
				while ((k < 9)&&(!flag)){
					if (possvals[k][j].get_size() == 2){
						triple.addAll(possvals[k][j].getEntries());
						temp = 10; 
						check = false;
						check2 = false; 
						for (int i = 0; i < 9; i++){
							if (i != k){
								if (!check){
									if (possvals[i][j].get_size() == 2){
										if (triple.contains(possvals[i][j].get(0))){
											triple.add(possvals[i][j].get(1));
											check = true;
											temp = i;
										}
										if (triple.contains(possvals[i][j].get(1))){
											triple.add(possvals[i][j].get(0));
											check = true;
											temp = i;
										}
									}
								}
								if (check2){
									if (possvals[i][j].get_size() == 2){
										if (triple.containsAll(possvals[i][j].getEntries())){
											visited[temp][j] = 1;
											visited[i][j] = 1;
											visited[k][j] = 1;
											flag = true;
											//System.out.println("triple: ");
											//System.out.println("temp,j: "+temp+","+j);
											//System.out.println("i,j: "+i+","+j);
											//System.out.println("k,j: "+k+","+j);
										}
									}
								}
							}
							if (check){
								check2 = true;
							}
						}
						if (!flag){
							while (!triple.isEmpty()){
								triple.remove(0);
							}
						}
					}
					k++;
				}
			}
			//if we found a triple, we need to reduce all other cells in the column
			if (flag){
				//System.out.println("values: "+triple.get(0)+","+triple.get(1)+","+triple.get(2));
				for (int i = 0; i < 9; i++){
					if (visited[i][j] == 0){
						if (possvals[i][j].isIn(triple.get(0))){
							possvals[i][j].deleteObj(triple.get(0));
							sizes[i][j]--;
                            removed++;
                            //System.out.println("removed "+triple.get(0)+" from tile "+i+","+j);
						}
						if (possvals[i][j].isIn(triple.get(1))){
							possvals[i][j].deleteObj(triple.get(1));
							sizes[i][j]--;
                            removed++;
                            //System.out.println("removed "+triple.get(1)+" from tile "+i+","+j);
						}
						if (possvals[i][j].isIn(triple.get(2))){
							possvals[i][j].deleteObj(triple.get(2));
							sizes[i][j]--;
                            removed++;
                            //System.out.println("removed "+triple.get(2)+" from tile "+i+","+j);
						}
					}
				}
			}
		}

		reset_visits();
		int q;
		int y1, y2; //temp vars
		//finally, by box
		for (int i = 0; i < 9; i = i+3){
			for (int j = 0; j < 9; j = j+3){
				while (!triple.isEmpty()){
					triple.remove(0);
				}
				flag = false;
				k = i;
				q = j;
				while ((k < i+3)&&(!flag)){
					while ((q < j+3)&&(!flag)){
						if (possvals[k][q].get_size() == 3){
							triple.addAll(possvals[k][q].getEntries());
							y1 = 10;
							y2 = 10;
							check = false;
							check2 = false;
							for (int r = i; r < i+3; r++){
								for (int c = j; c < j+3; c++){
									if ((r != k)&&(c != q)){
										if (!check){
											if ((possvals[r][c].get_size() == 3)||(possvals[r][c].get_size() == 2)){
												if (triple.containsAll(possvals[r][c].getEntries())){
													check = true;
													y1 = r;
													y2 = c;
												}
											}
										}
										if (check2){
											if ((possvals[r][c].get_size() == 3)||(possvals[r][c].get_size() == 2)){
												if (triple.containsAll(possvals[r][c].getEntries())){
													visited[y1][y2] = 1;
													visited[r][c] = 1;
													visited[k][q] = 1;
													flag = true;
													//System.out.println("triple: ");
													//System.out.println("y1,y2: "+y1+","+y2);
													//System.out.println("r,c: "+r+","+c);
													//System.out.println("k,q: "+k+","+q);
												}
											}
										}
									}
									if (check){
										check2 = true;
									}
								}
							}
							if (!flag){
								while (!triple.isEmpty()){
									triple.remove(0);
								}
							}
						}
						q++;
					}
					q = j;
					k++;
				}
				//for the last case
				if (!flag){
					while (!triple.isEmpty()){
						triple.remove(0);
					}
					k = i;
					q = j;
					while ((k < i+3)&&(!flag)){
						while ((q < j+3)&&(!flag)){
							if (possvals[k][q].get_size() == 2){
								triple.addAll(possvals[k][q].getEntries());
								y1 = 10;
								y2 = 10;
								check = false;
								check2 = false;
								for (int r = i; r < i+3; r++){
									for (int c = j; c < j+3; c++){
										if ((r != k)&&(c != q)){
											if (!check){
												if (possvals[r][c].get_size() == 2){
													if (triple.contains(possvals[r][c].get(0))){
														triple.add(possvals[r][c].get(1));
														check = true;
														y1 = r;
														y2 = c;
													}
													if (triple.contains(possvals[r][c].get(1))){
														triple.add(possvals[r][c].get(0));
														check = true;
														y1 = r;
														y2 = c;
													}
												}
											}
											if (check2){
												if (possvals[r][c].get_size() == 2){
													if (triple.containsAll(possvals[r][c].getEntries())){
														visited[y1][y2] = 1;
														visited[r][c] = 1;
														visited[k][q] = 1;
														flag = true;
														//System.out.println("triple: ");
														//System.out.println("y1,y2: "+y1+","+y2);
														//System.out.println("r,c: "+r+","+c);
														//System.out.println("k,q: "+k+","+q);
													}
												}
											}
										}
										if (check){
											check2 = true;
										}
									}
								}
								if (!flag){
									while (!triple.isEmpty()){
										triple.remove(0);
									}
								}
							}
							q++;
						}
						q = j;
						k++;
					}
				}
				//if we found a triple, we need to reduce all other cells in the column
				if (flag){
					//System.out.println("values: "+triple.get(0)+","+triple.get(1)+","+triple.get(2));
					for (int r = i; r < i+3; r++){
						for (int c = j; c < j+3; c++){
							if (visited[r][c] == 0){
								if (possvals[r][c].isIn(triple.get(0))){
									possvals[r][c].deleteObj(triple.get(0));
									sizes[r][c]--;
		                            removed++;
		                            //System.out.println("removed "+triple.get(0)+" from tile "+r+','+c);
								}
								if (possvals[r][c].isIn(triple.get(1))){
									possvals[r][c].deleteObj(triple.get(1));
									sizes[r][c]--;
		                            removed++;
		                            //System.out.println("removed "+triple.get(1)+" from tile "+r+','+c);
								}
								if (possvals[r][c].isIn(triple.get(2))){
									possvals[r][c].deleteObj(triple.get(2));
									sizes[r][c]--;
		                            removed++;
		                            //System.out.println("removed "+triple.get(2)+" from tile "+r+','+c);
								}
							}
						}
					}
				}
			}
		}
		return removed;
	}
	
	public int hiddenPairs(){
		//similar to hidden singles, we need to count the number of instances of each number inside any row, column, or box
		//then find the ones that form pairs in pairs of cells. 
		ArrayList<Tuple<Integer, Integer>> pairlist = new ArrayList<Tuple<Integer, Integer>>();
		ArrayList<Integer> list = new ArrayList<Integer>();
		int[] pair = new int[2];
		int[] kp = new int[9]; //instances
		int[] tp = new int[4]; //store pair coordinates
		boolean flag;
		int w, z, p;
		//first, for each row
		for (int i = 0; i < 9; i++){
			list = new ArrayList<Integer>();
			pairlist = new ArrayList<Tuple<Integer, Integer>>();
			//add all possvals into the list
			for (int k = 0; k < 9; k++){
				list.addAll(possvals[i][k].getEntries());
			}
			// total the instances and adjust kp
			for (int s = 0; s < list.size(); s++){
				kp[(list.get(s) - 1)]++;
			}
			//create a list of pairs of values that have instances of 2 for each value
			for (int a1 = 0; a1 < 9; a1++){
				if (kp[a1] == 2){
					for (int a2 = a1+1; a2 < 9; a2++){
						if (kp[a2] == 2){
							pairlist.add(new Tuple<Integer, Integer>(a1+1, a2+1));
						}
					}
				}
			}
			//iterate through the row to find 2 cells that contain both values in both cells
			w = 0;
			flag = false;
			while ((w < pairlist.size())&&(!flag)){
				z = 0; 
				while ((z < 9)&&(!flag)){
					if (possvals[i][z].contains_tup(pairlist.get(w))){
						int q = z+1; 
						while ((q < 9)&&(!flag)){
							if (possvals[i][q].contains_tup(pairlist.get(w))){
								tp[0] = i;
								tp[1] = z;
								tp[2] = i;
								tp[3] = q; //found the pair of cells
								pair[0] = pairlist.get(w).first;
								pair[1] = pairlist.get(w).second;
								flag = true;
								//System.out.println("type: row");
								//System.out.println("tiles: "+tp[0]+","+tp[1]+" & "+tp[2]+","+tp[3]);
								//System.out.println("values: "+pair[0]+","+pair[1]);
							}
							q++;
						}
					}
					z++;
				}
				w++;
			}
			//reduce both cells if a pair was found
			if (flag){
				p = 0; 
				while (possvals[tp[0]][tp[1]].get_size() > 2){
					if ((possvals[tp[0]][tp[1]].get(p) != pair[0])&&(possvals[tp[0]][tp[1]].get(p) != pair[1])){
						//System.out.println("removed "+possvals[tp[0]][tp[1]].get(p)+" from tile "+tp[0]+","+tp[1]);
						possvals[tp[0]][tp[1]].deleteIndex(p);
						sizes[tp[0]][tp[1]]--;
						removed++;
					}
					else {
						p++;
					}
				}
				p = 0;
				while (possvals[tp[2]][tp[3]].get_size() > 2){
					if ((possvals[tp[2]][tp[3]].get(p) != pair[0])&&(possvals[tp[2]][tp[3]].get(p) != pair[1])){
						//System.out.println("removed "+possvals[tp[2]][tp[3]].get(p)+" from tile "+tp[2]+","+tp[3]);
						possvals[tp[2]][tp[3]].deleteIndex(p);
						sizes[tp[2]][tp[3]]--;
						removed++;
					}
					else {
						p++;
					}
				}
			}
			//reset tp
			for (int x = 0; x < 4; x++){
				tp[x] = 0;
			}
			//reset kp
			for (int x = 0; x < 9; x++){
				kp[x] = 0;
			}
			//reset pair
			pair[0] = 0;
			pair[1] = 0;
		}
		
		// then, by column
		for (int j = 0; j < 9; j++){
			list = new ArrayList<Integer>();
			pairlist = new ArrayList<Tuple<Integer, Integer>>();
			//add all possvals into the list
			for (int k = 0; k < 9; k++){
				list.addAll(possvals[k][j].getEntries());
			}
			// total the instances and adjust kp
			for (int s = 0; s < list.size(); s++){
				kp[(list.get(s) - 1)]++;
			}
			//create a list of pairs of values that have instances of 2 for each value
			for (int a1 = 0; a1 < 9; a1++){
				if (kp[a1] == 2){
					for (int a2 = a1+1; a2 < 9; a2++){
						if (kp[a2] == 2){
							pairlist.add(new Tuple<Integer, Integer>(a1+1, a2+1));
						}
					}
				}
			}
			//iterate through the row to find 2 cells that contain both values in both cells
			w = 0;
			flag = false;
			while ((w < pairlist.size())&&(!flag)){
				z = 0; 
				while ((z < 9)&&(!flag)){
					if (possvals[z][j].contains_tup(pairlist.get(w))){
						int q = z+1; 
						while ((q < 9)&&(!flag)){
							if (possvals[q][j].contains_tup(pairlist.get(w))){
								tp[0] = z;
								tp[1] = j;
								tp[2] = q;
								tp[3] = j; //found the pair of cells
								pair[0] = pairlist.get(w).first;
								pair[1] = pairlist.get(w).second;
								flag = true;
								//System.out.println("type: column");
								//System.out.println("tiles: "+tp[0]+","+tp[1]+" & "+tp[2]+","+tp[3]);
								//System.out.println("values: "+pair[0]+","+pair[1]);
							}
							q++;
						}
					}
					z++;
				}
				w++;
			}
			//reduce both cells if a pair was found
			if (flag){
				p = 0; 
				while (possvals[tp[0]][tp[1]].get_size() > 2){
					if ((possvals[tp[0]][tp[1]].get(p) != pair[0])&&(possvals[tp[0]][tp[1]].get(p) != pair[1])){
						//System.out.println("removed "+possvals[tp[0]][tp[1]].get(p)+" from tile "+tp[0]+","+tp[1]);
						possvals[tp[0]][tp[1]].deleteIndex(p);
						sizes[tp[0]][tp[1]]--;
						removed++;
					}
					else {
						p++;
					}
				}
				p = 0;
				while (possvals[tp[2]][tp[3]].get_size() > 2){
					if ((possvals[tp[2]][tp[3]].get(p) != pair[0])&&(possvals[tp[2]][tp[3]].get(p) != pair[1])){
						//System.out.println("removed "+possvals[tp[2]][tp[3]].get(p)+" from tile "+tp[2]+","+tp[3]);
						possvals[tp[2]][tp[3]].deleteIndex(p);
						sizes[tp[2]][tp[3]]--;
						removed++;
					}
					else {
						p++;
					}
				}
			}
			//reset tp
			for (int x = 0; x < 4; x++){
				tp[x] = 0;
			}
			//reset kp
			for (int x = 0; x < 9; x++){
				kp[x] = 0;
			}
			//reset pair
			pair[0] = 0;
			pair[1] = 0;
		}
		
		int q1, q2, z1, z2;
		//finally, by box
		for (int i = 0; i < 9; i = i+3){
			for (int j = 0; j < 9; j = j+3){
				list = new ArrayList<Integer>();
				pairlist = new ArrayList<Tuple<Integer, Integer>>();
				for (int r = i; r < i+3; r++){
					for (int c = j; c < j+3; c++){
						list.addAll(possvals[r][c].getEntries());
					}
				}
				for (int s = 0; s < list.size(); s++){
					kp[(list.get(s) - 1)]++;
				}
				for (int a1 = 0; a1 < 9; a1++){
					if (kp[a1] == 2){
						for (int a2 = a1+1; a2 < 9; a2++){
							if (kp[a2] == 2){
								pairlist.add(new Tuple<Integer, Integer>(a1+1, a2+1));
							}
						}
					}
				}
				//iterate through the row to find 2 cells that contain both values in both cells
				w = 0;
				flag = false;
				while ((w < pairlist.size())&&(!flag)){
					z1 = i;
					q1 = j;
					while ((z1 < i+3)&&(!flag)){
						while ((q1 < j+3)&&(!flag)){
							if (possvals[z1][q1].contains_tup(pairlist.get(w))){
								z2 = z1;
								q2 = q1+1;
								while ((z2 < i+3)&&(!flag)){
									while ((q2 < j+3)&&(!flag)){
										if (possvals[z2][q2].contains_tup(pairlist.get(w))){
											tp[0] = z1;
											tp[1] = q1;
											tp[2] = z2;
											tp[3] = q2; //found the pair of cells
											pair[0] = pairlist.get(w).first;
											pair[1] = pairlist.get(w).second;
											flag = true;
											//System.out.println("type: box");
											//System.out.println("tiles: "+tp[0]+","+tp[1]+" & "+tp[2]+","+tp[3]);
											//System.out.println("values: "+pair[0]+","+pair[1]);
										}	
										q2++;
									}
									q2 = j;
									z2++;
								}
							}
							q1++;
						}
						q1 = j;
						z1++;
					}
					w++;
				}
				//reduce both cells if a pair was found
				if (flag){
					p = 0; 
					while (possvals[tp[0]][tp[1]].get_size() > 2){
						if ((possvals[tp[0]][tp[1]].get(p) != pair[0])&&(possvals[tp[0]][tp[1]].get(p) != pair[1])){
							//System.out.println("removed "+possvals[tp[0]][tp[1]].get(p)+" from tile "+tp[0]+","+tp[1]);
							possvals[tp[0]][tp[1]].deleteIndex(p);
							sizes[tp[0]][tp[1]]--;
							removed++;
						}
						else {
							p++;
						}
					}
					p = 0;
					while (possvals[tp[2]][tp[3]].get_size() > 2){
						if ((possvals[tp[2]][tp[3]].get(p) != pair[0])&&(possvals[tp[2]][tp[3]].get(p) != pair[1])){
							//System.out.println("removed "+possvals[tp[2]][tp[3]].get(p)+" from tile "+tp[2]+","+tp[3]);
							possvals[tp[2]][tp[3]].deleteIndex(p);
							sizes[tp[2]][tp[3]]--;
							removed++;
						}
						else {
							p++;
						}
					}
				}		
				//reset tp
				for (int x = 0; x < 4; x++){
					tp[x] = 0;
				}
				//reset kp
				for (int x = 0; x < 9; x++){
					kp[x] = 0;
				}
				//reset pair
				pair[0] = 0;
				pair[1] = 0;
			}
		}
		return removed;
	} 
	
	public int hiddenTrips(){
		//again, similar to hidden pairs, only there are more cases to consider
		ArrayList<TripleTup<Integer, Integer, Integer>> triplist = new ArrayList<TripleTup<Integer, Integer, Integer>>();
		ArrayList<Integer> list = new ArrayList<Integer>();
		int[] triple = new int[3];
		int[] kp = new int[9]; //instances
		int[] tp = new int[6]; //store pair coordinates
		boolean flag;
		boolean seek;
		int w, z, p;
		//first, by row
		for (int i = 0; i < 9; i++){
			list = new ArrayList<Integer>();
			triplist = new ArrayList<TripleTup<Integer, Integer, Integer>>();
			//add all possvals into the list
			for (int k = 0; k < 9; k++){
				list.addAll(possvals[i][k].getEntries());
			}
			// total the instances and adjust kp
			for (int s = 0; s < list.size(); s++){
				kp[(list.get(s) - 1)]++;
			}
			//create a list of triplets of values that have instances of 2 or 3 for each value
			for (int a1 = 0; a1 < 9; a1++){
				if ((kp[a1] == 3)||(kp[a1] == 2)){
					for (int a2 = a1+1; a2 < 9; a2++){
						if ((kp[a2] == 3)||(kp[a2] == 2)){
							for (int a3 = a2+1; a3 < 9; a3++){
								if ((kp[a3] == 3)||(kp[a3] == 2)){
									triplist.add(new TripleTup<Integer, Integer, Integer>(a1+1, a2+1, a3+1));
								}
							}
						}
					}
				}
			}
			//iterate through the row to find 3 cells that contain all 3 values in all 3 cells
			w = 0;
			flag = false;
			while ((w < triplist.size())&&(!flag)){
				z = 0; 
				while ((z < 9)&&(!flag)){
					if (possvals[i][z].contains_trip(triplist.get(w))){
						int q = z+1; 
						while ((q < 9)&&(!flag)){
							if (possvals[i][q].contains_trip(triplist.get(w))){
								int v = q+1;
								while ((v < 9)&&(!flag)){
									if (possvals[i][v].contains_trip(triplist.get(w))){
										tp[0] = i;
										tp[1] = z;
										tp[2] = i;
										tp[3] = q; 
										tp[4] = i;
										tp[5] = v; //found a triplet
										triple[0] = triplist.get(w).first;
										triple[1] = triplist.get(w).second;
										triple[2] = triplist.get(w).third;
										//now we need to run one last search to make sure none of the values in the triplet
										//are anywhere else in the same unit
										seek = true;
										for (int b = 0; b < 9; b++){
											if ((b != z)&&(b != q)&&(b != v)){
												if (possvals[i][b].contains_anyTrip(triplist.get(w))){
													seek = false;
												}
											}
										}
										if (seek){
											flag = true;
											//System.out.println("type: row");
											//System.out.println("tiles: "+tp[0]+","+tp[1]+" & "+tp[2]+","+tp[3]+" & "+tp[4]+","+tp[5]);
											//System.out.println("values: "+triple[0]+","+triple[1]+","+triple[2]);
										}
									}
									v++;
								}
							}
							q++;
						}
					}
					z++;
				}
				w++;
			}
			//reduce all 3 cells if a triplet was found
			if (flag){
				p = possvals[tp[0]][tp[1]].get_size() - 1;
				while (p >= 0){
					if ((possvals[tp[0]][tp[1]].get(p) != triple[0])&&(possvals[tp[0]][tp[1]].get(p) != triple[1])
							&&(possvals[tp[0]][tp[1]].get(p) != triple[2])){
						//System.out.println("removed "+possvals[tp[0]][tp[1]].get(p)+" from tile "+tp[0]+","+tp[1]);
						possvals[tp[0]][tp[1]].deleteIndex(p);;
						sizes[tp[0]][tp[1]]--;
						removed++;
						p--;
					}
					else {
						p--;
					}
				}
				p = possvals[tp[2]][tp[3]].get_size() - 1;
				while (p >= 0){
					if ((possvals[tp[2]][tp[3]].get(p) != triple[0])&&(possvals[tp[2]][tp[3]].get(p) != triple[1])
							&&(possvals[tp[2]][tp[3]].get(p) != triple[2])){
						//System.out.println("removed "+possvals[tp[2]][tp[3]].get(p)+" from tile "+tp[2]+","+tp[3]);
						possvals[tp[2]][tp[3]].deleteIndex(p);;
						sizes[tp[2]][tp[3]]--;
						removed++;
						p--;
					}
					else {
						p--;
					}
				}
				p = possvals[tp[4]][tp[5]].get_size() - 1;
				while (p >= 0){
					if ((possvals[tp[4]][tp[5]].get(p) != triple[0])&&(possvals[tp[4]][tp[5]].get(p) != triple[1])
							&&(possvals[tp[4]][tp[5]].get(p) != triple[2])){
						//System.out.println("removed "+possvals[tp[4]][tp[5]].get(p)+" from tile "+tp[4]+","+tp[5]);
						possvals[tp[4]][tp[5]].deleteIndex(p);;
						sizes[tp[4]][tp[5]]--;
						removed++;
						p--;
					}
					else {
						p--;
					}
				}
			}
			//reset tp
			for (int x = 0; x < 6; x++){
				tp[x] = 0;
			}
			//reset kp
			for (int x = 0; x < 9; x++){
				kp[x] = 0;
			}
			//reset triple
			for (int x = 0; x < 3; x++){
				triple[x] = 0;
			}
		}
		
		//then by column
		for (int j = 0; j < 9; j++){
			list = new ArrayList<Integer>();
			triplist = new ArrayList<TripleTup<Integer, Integer, Integer>>();
			//add all possvals into the list
			for (int k = 0; k < 9; k++){
				list.addAll(possvals[k][j].getEntries());
			}
			// total the instances and adjust kp
			for (int s = 0; s < list.size(); s++){
				kp[(list.get(s) - 1)]++;
			}
			//create a list of triplets of values that have instances of 2 or 3 for each value
			for (int a1 = 0; a1 < 9; a1++){
				if ((kp[a1] == 3)||(kp[a1] == 2)){
					for (int a2 = a1+1; a2 < 9; a2++){
						if ((kp[a2] == 3)||(kp[a2] == 2)){
							for (int a3 = a2+1; a3 < 9; a3++){
								if ((kp[a3] == 3)||(kp[a3] == 2)){
									triplist.add(new TripleTup<Integer, Integer, Integer>(a1+1, a2+1, a3+1));
								}
							}
						}
					}
				}
			}
			w = 0;
			flag = false;
			while ((w < triplist.size())&&(!flag)){
				z = 0; 
				while ((z < 9)&&(!flag)){
					if (possvals[z][j].contains_trip(triplist.get(w))){
						int q = z+1; 
						while ((q < 9)&&(!flag)){
							if (possvals[q][j].contains_trip(triplist.get(w))){
								int v = q+1;
								while ((v < 9)&&(!flag)){
									if (possvals[v][j].contains_trip(triplist.get(w))){
										tp[0] = z;
										tp[1] = j;
										tp[2] = q;
										tp[3] = j; 
										tp[4] = v;
										tp[5] = j; //found a triplet
										triple[0] = triplist.get(w).first;
										triple[1] = triplist.get(w).second;
										triple[2] = triplist.get(w).third;
										seek = true;
										for (int b = 0; b < 9; b++){
											if ((b != z)&&(b != q)&&(b != v)){
												if (possvals[b][j].contains_anyTrip(triplist.get(w))){
													seek = false;
												}
											}
										}
										if (seek){
											flag = true;
											//System.out.println("type: column");
											//System.out.println("tiles: "+tp[0]+","+tp[1]+" & "+tp[2]+","+tp[3]+" & "+tp[4]+","+tp[5]);
											//System.out.println("values: "+triple[0]+","+triple[1]+","+triple[2]);
										}
									}
									v++;
								}
							}
							q++;
						}
					}
					z++;
				}
				w++;
			}
			//reduce all 3 cells if a triplet was found
			if (flag){
				p = possvals[tp[0]][tp[1]].get_size() - 1;
				while (p >= 0){
					if ((possvals[tp[0]][tp[1]].get(p) != triple[0])&&(possvals[tp[0]][tp[1]].get(p) != triple[1])
							&&(possvals[tp[0]][tp[1]].get(p) != triple[2])){
						//System.out.println("removed "+possvals[tp[0]][tp[1]].get(p)+" from tile "+tp[0]+","+tp[1]);
						possvals[tp[0]][tp[1]].deleteIndex(p);;
						sizes[tp[0]][tp[1]]--;
						removed++;
						p--;
					}
					else {
						p--;
					}
				}
				p = possvals[tp[2]][tp[3]].get_size() - 1;
				while (p >= 0){
					if ((possvals[tp[2]][tp[3]].get(p) != triple[0])&&(possvals[tp[2]][tp[3]].get(p) != triple[1])
							&&(possvals[tp[2]][tp[3]].get(p) != triple[2])){
						//System.out.println("removed "+possvals[tp[2]][tp[3]].get(p)+" from tile "+tp[2]+","+tp[3]);
						possvals[tp[2]][tp[3]].deleteIndex(p);;
						sizes[tp[2]][tp[3]]--;
						removed++;
						p--;
					}
					else {
						p--;
					}
				}
				p = possvals[tp[4]][tp[5]].get_size() - 1;
				while (p >= 0){
					if ((possvals[tp[4]][tp[5]].get(p) != triple[0])&&(possvals[tp[4]][tp[5]].get(p) != triple[1])
							&&(possvals[tp[4]][tp[5]].get(p) != triple[2])){
						//System.out.println("removed "+possvals[tp[4]][tp[5]].get(p)+" from tile "+tp[4]+","+tp[5]);
						possvals[tp[4]][tp[5]].deleteIndex(p);;
						sizes[tp[4]][tp[5]]--;
						removed++;
						p--;
					}
					else {
						p--;
					}
				}
			}
			//reset tp
			for (int x = 0; x < 6; x++){
				tp[x] = 0;
			}
			//reset kp
			for (int x = 0; x < 9; x++){
				kp[x] = 0;
			}
			//reset triple
			for (int x = 0; x < 3; x++){
				triple[x] = 0;
			}
		}
		
		int q1, q2, q3, w1, w2, w3;
		//finally, by box
		for (int i = 0; i < 9; i = i+3){
			for (int j = 0; j < 9; j = j+3){
				list = new ArrayList<Integer>();
				triplist = new ArrayList<TripleTup<Integer, Integer, Integer>>();
				//add all possvals into the list
				for (int r = i; r < i+3; r++){
					for (int c = j; c < j+3; c++){
						list.addAll(possvals[r][c].getEntries());
					}
				}
				// total the instances and adjust kp
				for (int s = 0; s < list.size(); s++){
					kp[(list.get(s) - 1)]++;
				}
				//create a list of triplets of values that have instances of 2 or 3 for each value
				for (int a1 = 0; a1 < 9; a1++){
					if ((kp[a1] == 3)||(kp[a1] == 2)){
						for (int a2 = a1+1; a2 < 9; a2++){
							if ((kp[a2] == 3)||(kp[a2] == 2)){
								for (int a3 = a2+1; a3 < 9; a3++){
									if ((kp[a3] == 3)||(kp[a3] == 2)){
										triplist.add(new TripleTup<Integer, Integer, Integer>(a1+1, a2+1, a3+1));
									}
								}
							}
						}
					}
				} 
				z = 0;
				flag = false;
				while ((z < triplist.size())&&(!flag)){
					q1 = i;
					w1 = j;
					while ((q1 < i+3)&&(!flag)){
						while ((w1 < j+3)&&(!flag)){
							if (possvals[q1][w1].contains_trip(triplist.get(z))){
								q2 = q1;
								w2 = w1 + 1;
								while ((q2 < i+3)&&(!flag)){
									while ((w2 < j+3)&&(!flag)){
										if (possvals[q2][w2].contains_trip(triplist.get(z))){
											q3 = q2;
											w3 = w2 + 1;
											while ((q3 < j+3)&&(!flag)){
												while ((w3 < j+3)&&(!flag)){
													if (possvals[q3][w3].contains_trip(triplist.get(z))){
														tp[0] = q1;
														tp[1] = w1;
														tp[2] = q2;
														tp[3] = w2; 
														tp[4] = q3;
														tp[5] = w3; //found a triplet
														triple[0] = triplist.get(z).first;
														triple[1] = triplist.get(z).second;
														triple[2] = triplist.get(z).third;
														seek = true;
														for (int b = i; b < i+3; b++){
															for (int t = j; t < j+3; t++){
																if (!(((b == q1)&&(t == w1))||((b == q2)&&(t == w2))||((b == q3)&&(t == w3)))){
																	if (possvals[b][t].contains_anyTrip(triplist.get(z))){
																		seek = false;
																	}
																}
															}
														}
														if (seek){
															flag = true;
															//System.out.println("type: box");
															//System.out.println("tiles: "+tp[0]+","+tp[1]+" & "+tp[2]+","+tp[3]+" & "+tp[4]+","+tp[5]);
															//System.out.println("values: "+triple[0]+","+triple[1]+","+triple[2]);
														}
													}
													w3++;
												}
												w3 = j;
												q3++;
											}
										}
										w2++;
									}
									w2 = j;
									q2++;
								}
							}
							w1++;
						}
						w1 = j;
						q1++;
					}
					z++;
				}
				//reduce all 3 cells if a triplet was found
				if (flag){
					p = possvals[tp[0]][tp[1]].get_size() - 1;
					while (p >= 0){
						if ((possvals[tp[0]][tp[1]].get(p) != triple[0])&&(possvals[tp[0]][tp[1]].get(p) != triple[1])
								&&(possvals[tp[0]][tp[1]].get(p) != triple[2])){
							//System.out.println("removed "+possvals[tp[0]][tp[1]].get(p)+" from tile "+tp[0]+","+tp[1]);
							possvals[tp[0]][tp[1]].deleteIndex(p);;
							sizes[tp[0]][tp[1]]--;
							removed++;
							p--;
						}
						else {
							p--;
						}
					}
					p = possvals[tp[2]][tp[3]].get_size() - 1;
					while (p >= 0){
						if ((possvals[tp[2]][tp[3]].get(p) != triple[0])&&(possvals[tp[2]][tp[3]].get(p) != triple[1])
								&&(possvals[tp[2]][tp[3]].get(p) != triple[2])){
							//System.out.println("removed "+possvals[tp[2]][tp[3]].get(p)+" from tile "+tp[2]+","+tp[3]);
							possvals[tp[2]][tp[3]].deleteIndex(p);;
							sizes[tp[2]][tp[3]]--;
							removed++;
							p--;
						}
						else {
							p--;
						}
					}
					p = possvals[tp[4]][tp[5]].get_size() - 1;
					while (p >= 0){
						if ((possvals[tp[4]][tp[5]].get(p) != triple[0])&&(possvals[tp[4]][tp[5]].get(p) != triple[1])
								&&(possvals[tp[4]][tp[5]].get(p) != triple[2])){
							//System.out.println("removed "+possvals[tp[4]][tp[5]].get(p)+" from tile "+tp[4]+","+tp[5]);
							possvals[tp[4]][tp[5]].deleteIndex(p);;
							sizes[tp[4]][tp[5]]--;
							removed++;
							p--;
						}
						else {
							p--;
						}
					}
				}
				//reset tp
				for (int x = 0; x < 6; x++){
					tp[x] = 0;
				}
				//reset kp
				for (int x = 0; x < 9; x++){
					kp[x] = 0;
				}
				//reset triple
				for (int x = 0; x < 3; x++){
					triple[x] = 0;
				}
			}
		}
		return removed;
	}
	
	public int nakedQuads(){
		return removed;
	}
	
	public int hiddenQuads(){
		return count;
	}
	
	public int pointingPairs(){
		return count;
	}
	
	public int pointingTrips(){
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
					grid[i][j] = possvals[i][j].get(0);
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
    
    public ArrayList<String> get_TextLog(){
    	return this.AIlog;
    }
    
    public void clear_TextLog(){
    	while (!AIlog.isEmpty()){
    		AIlog.remove(0);
    	}
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
