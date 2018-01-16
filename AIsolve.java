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
	private int init = 0;
	private String currstrat = "";
	private int removed = 0; //to be used as the alternate label for count; showing 'n' fewer possibilities
	private int reduce = 0; //if we end up using a reduction method 
							//if reduce = 2, it means unsolved 
	private int[][] visited = new int[9][9]; //keeps track of which tiles are to be kept in consideration
	private String[] letter_axis = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
	private ArrayList<String> AIlog = new ArrayList<String>(); //for outputting all AI steps 
	private ArrayList<TripleTup<String, Integer, Tuple<Integer, Integer>>> tile_log = 
			new ArrayList<TripleTup<String, Integer, Tuple<Integer, Integer>>>(); //this keeps a record of all 
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
		if (init == 0){
			init = 1;
			updatePossvals();
			return grid;
		}
		else if (singleOpt() > 0){
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
		else if (nakedQuads() > 0){
			count = removed;
			if (reduce == 0){
				currstrat = "Naked Quadruples Reduction";
				reduce = 1;
			}
			else {
				currstrat = "Naked Quadruples Reduction via " + currstrat;
			}
			return grid;
		}
		else if (hiddenQuads() > 0){
			count = removed;
			if (reduce == 0){
				currstrat = "Hidden Quadruples Reduction";
				reduce = 1;
			}
			else {
				currstrat = "Hidden Quadruples Reduction via " + currstrat;
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
		else if (pointingTrips() > 0){
			count = removed;
			if (reduce == 0){
				currstrat = "Pointing Triples Reduction";
				reduce = 1;
			}
			else {
				currstrat = "Pointing Triples Reduction via "+currstrat;
			}
			return grid;
		}
		else if (boxLinePairs() > 0){
			count = removed;
			if (reduce == 0){
				currstrat = "Box Line Pairs Reduction";
				reduce = 1;
			}
			else {
				currstrat = "Box Line Pairs Reduction via "+currstrat;
			}
			return grid;
		}
		else if (boxLineTrips() > 0){
			count = removed;
			if (reduce == 0){
				currstrat = "Box Line Triples Reduction";
				reduce = 1;
			}
			else {
				currstrat = "Box Line Triples Reduction via "+currstrat;
			}
			return grid;
		}
		else if (xWingRedux() > 0){
			count = removed;
			if (reduce == 0){
				currstrat = "X Wing Reduction";
				reduce = 1;
			}
			else {
				currstrat = "X Wing Reduction bia "+currstrat;
			}
			return grid;
		}
		else {
			unsolvable();
			reduce = 2;
			return grid;
		}
	}
	
	//each algorithm makes up part of the entire AI strategy 
	public int singleOpt(){
		for (int i = 0; i <= 8; i++){
			for (int j = 0; j <= 8; j++){
				if ((sizes[i][j] == 1)&&(grid[i][j] == 0)){
					grid[i][j] = possvals[i][j].get(0);
					AIlog.add("Filled "+letter_axis[j]+","+(i+1)+" with "+possvals[i][j].get(0)+"; only option");
					count++;
				}
			}
		}
		if (count > 0){
			AIlog.add(0, "Strategy: Single Option");
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
						AIlog.add("Filled "+letter_axis[j]+","+(i+1)+" with "+possvals[i][j].get(z)+"; unique in row");
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
						AIlog.add("Filled "+letter_axis[j]+","+(i+1)+" with "+possvals[i][j].get(z)+"; unique in column");
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
								AIlog.add("Filled "+letter_axis[j]+","+(i+1)+" with "+possvals[i][j].get(z)+"; unique in box");
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
		if (count > 0){
			AIlog.add(0, "Strategy: Hidden Singles");
		}
		return count;
	}
	
	//essentially, all reductions are variations of updating possible values 
	
	//Moderate Strategies
	//Naked Groups
	//Hidden Groups
	
	public int nakedPairs(){
		//again, it's easier to do this by row, then column, then box
		//an important thing to note is that there can only be one naked group for each row, col, or box
		//but any naked group can determine the reductions for more than one section (row, col, and/or box).
		ArrayList<Integer> pair = new ArrayList<Integer>();
		int[] pair2 = new int[4];
		boolean flag;
		//first, do it for the rows
		for (int i = 0; i < 9; i++){
			for (int u = 0; u < 4; u++){
				pair2[u] = 0;
			}
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
								pair2[0] = i;
								pair2[1] = k;
								pair2[2] = i;
								pair2[3] = j;
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
				String h; 
				for (int j = 0; j < 9; j++){
					if (visited[i][j] == 0){
						h = "";
						if (possvals[i][j].isIn(pair.get(0))){
							h = h + pair.get(0) + ", ";
							possvals[i][j].deleteObj(pair.get(0));
							sizes[i][j]--;
							removed++;
						}
						if (possvals[i][j].isIn(pair.get(1))){
							h = h + pair.get(1) + ", ";
							possvals[i][j].deleteObj(pair.get(1));
							sizes[i][j]--;
							removed++;
						}
						if (h != ""){
							if (h.endsWith(", ")) {
								h = h.substring(0, h.length()-2);
							}
							AIlog.add("Pair (Row): "+letter_axis[pair2[1]]+","+(pair2[0]+1)+" & "+letter_axis[pair2[3]]+","+(pair2[2]+1));
							AIlog.add("Values: "+pair.get(0)+","+pair.get(1));
							AIlog.add("Removed "+h+" from tile "+letter_axis[j]+","+(i+1));
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
			for (int u = 0; u < 4; u++){
				pair2[u] = 0;
			}
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
								pair2[0] = k;
								pair2[1] = j;
								pair2[2] = i;
								pair2[3] = j;
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
				String h;
				for (int i = 0; i < 9; i++){
					if (visited[i][j] == 0){
						h = "";
						if (possvals[i][j].isIn(pair.get(0))){
							h = h + pair.get(0) + ", ";
							possvals[i][j].deleteObj(pair.get(0));
							sizes[i][j]--;
							removed++;
						}
						if (possvals[i][j].isIn(pair.get(1))){
							h = h + pair.get(1) + ", ";
							possvals[i][j].deleteObj(pair.get(1));
							sizes[i][j]--;
							removed++;
						}
						if (h != ""){
							if (h.endsWith(", ")) {
								h = h.substring(0, h.length()-2);
							}
							AIlog.add("Pair (Column): "+letter_axis[pair2[1]]+","+(pair2[0]+1)+" & "+letter_axis[pair2[3]]+","+(pair2[2]+1));
							AIlog.add("Values: "+pair.get(0)+","+pair.get(1));
							AIlog.add("Removed "+h+" from tile "+letter_axis[j]+","+(i+1));
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
				for (int u = 0; u < 4; u++){
					pair2[u] = 0;
				}
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
												pair2[0] = k;
												pair2[1] = q;
												pair2[2] = r;
												pair2[3] = c;
												flag = true;
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
				//if we found a pair, reduce all other cells in the box
				if (flag){
					String h;
					for (int r = i; r < i+3; r++){
						for (int c = j; c < j+3; c++){
							if (visited[r][c] == 0){
								h = "";
								if (possvals[r][c].isIn(pair.get(0))){
									h = h + pair.get(0) + ", ";
									possvals[r][c].deleteObj(pair.get(0));
									sizes[r][c]--;
									removed++;
								}
								if (possvals[r][c].isIn(pair.get(1))){
									h = h + pair.get(1) + ", ";
									possvals[r][c].deleteObj(pair.get(1));
									sizes[r][c]--;
									removed++;
								}
								if (h != ""){
									if (h.endsWith(", ")) {
										h = h.substring(0, h.length()-2);
									}
									AIlog.add("Pair (Box): "+letter_axis[pair2[1]]+","+(pair2[0]+1)+" & "+letter_axis[pair2[3]]+","+(pair2[2]+1));
									AIlog.add("Values: "+pair.get(0)+","+pair.get(1));
									AIlog.add("Removed "+h+" from tile "+letter_axis[c]+","+(r+1));
								}
							}
						}
					}
				}
			}
		}
		if (removed > 0){
			AIlog.add(0, "Strategy: Naked Pairs");
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
		int[] triple2 = new int[6];
		boolean flag;
		boolean check;
		boolean check2;
		int temp;
		int k;
		for (int i = 0; i < 9; i++){
			for (int u = 0; u < 6; u++){
				triple2[u] = 0;
			}
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
										triple2[0] = i;
										triple2[1] = k;
										triple2[2] = i;
										triple2[3] = temp;
										triple2[4] = i;
										triple2[5] = j;
										flag = true;
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
				for (int u = 0; u < 6; u++){
					triple2[u] = 0;
				}
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
											triple2[0] = i;
											triple2[1] = k;
											triple2[2] = i;
											triple2[3] = temp;
											triple2[4] = i;
											triple2[5] = j;
											flag = true;
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
				String h;
				for (int j = 0; j < 9; j++){
					if (visited[i][j] == 0){
						h = "";
						if (possvals[i][j].isIn(triple.get(0))){
							h = h + triple.get(0) + ", ";
							possvals[i][j].deleteObj(triple.get(0));
							sizes[i][j]--;
							removed++;
						}
						if (possvals[i][j].isIn(triple.get(1))){
							h = h + triple.get(1) + ", ";
							possvals[i][j].deleteObj(triple.get(1));
							sizes[i][j]--;
							removed++;
						}
						if (possvals[i][j].isIn(triple.get(2))){
							h = h + triple.get(2) + ", ";
							possvals[i][j].deleteObj(triple.get(2));
							sizes[i][j]--;
							removed++;
						}
						if (h != ""){
							if (h.endsWith(", ")) {
								h = h.substring(0, h.length()-2);
							}
							AIlog.add("Triple (Row): "+letter_axis[triple2[1]]+","+(triple2[0] + 1)+" & " 
										+ letter_axis[triple2[3]]+","+(triple2[2]+1)+" & "+letter_axis[triple2[5]]+","+(triple2[4]+1));
							AIlog.add("Values: " + triple.get(0)+","+triple.get(1)+","+triple.get(2));
							AIlog.add("Removed "+h+" from tile "+letter_axis[j]+","+(i+1));
						}
					}
				}
			}
		}

		reset_visits();
		//then we do it by column
		for (int j = 0; j < 9; j++){
			for (int u = 0; u < 6; u++){
				triple2[u] = 0;
			}
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
										triple2[0] = k;
										triple2[1] = j;
										triple2[2] = temp;
										triple2[3] = j;
										triple2[4] = i;
										triple2[5] = j;
										flag = true;
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
											triple2[0] = k;
											triple2[1] = j;
											triple2[2] = temp;
											triple2[3] = j;
											triple2[4] = i;
											triple2[5] = j;
											flag = true;
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
				String h;
				for (int i = 0; i < 9; i++){
					if (visited[i][j] == 0){
						h = "";
						if (possvals[i][j].isIn(triple.get(0))){
							h = h + triple.get(0) + ", ";
							possvals[i][j].deleteObj(triple.get(0));
							sizes[i][j]--;
                            removed++;
						}
						if (possvals[i][j].isIn(triple.get(1))){
							h = h + triple.get(1) + ", ";
							possvals[i][j].deleteObj(triple.get(1));
							sizes[i][j]--;
                            removed++;
						}
						if (possvals[i][j].isIn(triple.get(2))){
							h = h + triple.get(2) + ", ";
							possvals[i][j].deleteObj(triple.get(2));
							sizes[i][j]--;
                            removed++;
						}
						if (h != ""){
							if (h.endsWith(", ")) {
								h = h.substring(0, h.length()-2);
							}
							AIlog.add("Triple (Column): "+letter_axis[triple2[1]]+","+(triple2[0] + 1)+" & " 
									+ letter_axis[triple2[3]]+","+(triple2[2]+1)+" & "+letter_axis[triple2[5]]+","+(triple2[4]+1));
							AIlog.add("Values: " + triple.get(0)+","+triple.get(1)+","+triple.get(2));
							AIlog.add("Removed "+h+" from tile "+letter_axis[j]+","+(i+1));
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
				for (int u = 0; u < 6; u++){
					triple2[u] = 0;
				}
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
													triple2[0] = k;
													triple2[1] = q;
													triple2[2] = y1;
													triple2[3] = y2;
													triple2[4] = r;
													triple2[5] = c;
													flag = true;
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
														triple2[0] = k;
														triple2[1] = q;
														triple2[2] = y1;
														triple2[3] = y2;
														triple2[4] = r;
														triple2[5] = c;
														flag = true;
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
					String h;
					for (int r = i; r < i+3; r++){
						for (int c = j; c < j+3; c++){
							if (visited[r][c] == 0){
								h = "";
								if (possvals[r][c].isIn(triple.get(0))){
									h = h + triple.get(0) + ", ";
									possvals[r][c].deleteObj(triple.get(0));
									sizes[r][c]--;
		                            removed++;
								}
								if (possvals[r][c].isIn(triple.get(1))){
									h = h + triple.get(1) + ", ";
									possvals[r][c].deleteObj(triple.get(1));
									sizes[r][c]--;
		                            removed++;
								}
								if (possvals[r][c].isIn(triple.get(2))){
									h = h + triple.get(2) + ", ";
									possvals[r][c].deleteObj(triple.get(2));
									sizes[r][c]--;
		                            removed++;
								}
								if (h != ""){
									if (h.endsWith(", ")) {
										h = h.substring(0, h.length()-2);
									}
									AIlog.add("Triple (Box): "+letter_axis[triple2[1]]+","+(triple2[0] + 1)+" & " 
											+ letter_axis[triple2[3]]+","+(triple2[2]+1)+" & "+letter_axis[triple2[5]]+","+(triple2[4]+1));
									AIlog.add("Values: " + triple.get(0)+","+triple.get(1)+","+triple.get(2));
									AIlog.add("Removed "+h+" from tile "+letter_axis[c]+","+(r+1));
								}
							}
						}
					}
				}
			}
		}
		if (removed > 0){
			AIlog.add(0, "Strategy: Naked Triples");
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
								//AIlog.add("Pair (Row): "+tp[0]+","+tp[1]+" & "+tp[2]+","+tp[3]);
								//AIlog.add("Values: "+pair[0]+","+pair[1]);
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
		//naked quads are rare, but can still appear
		//first, by row
		ArrayList<Integer> list = new ArrayList<Integer>(); // list of empty tiles
		ArrayList<Integer> values = new ArrayList<Integer>(); //holds the values we're testing, looking for size 4
		ArrayList<Integer> quad = new ArrayList<Integer>();//the final four tiles
		boolean flag;
		int q1, q2, q3, q4;
		int z;
		
		for (int i = 0; i < 9; i++){
			list = new ArrayList<Integer>();
			quad = new ArrayList<Integer>();
			//for efficiency, we're going to change up the method for naked groups at this stage
			//first, get all the tiles that haven't been solved yet.
			for (int k = 0; k < 9; k++){
				if (grid[i][k] == 0){
					if (possvals[i][k].get_size() < 5){
						list.add(k);
					}
				}
			}
			//then, iterate through the list and check groups of 4 tiles, seeing if there are only 4 possvals total 
			//without duplicates
			q1 = 0;
			flag = false;
			while ((q1 < list.size())&&(!flag)){
				q2 = q1 + 1;
				while ((q2 < list.size())&&(!flag)){
					q3 = q2 + 1;
					while ((q3 < list.size())&&(!flag)){
						q4 = q3 + 1;
						while ((q4 < list.size())&&(!flag)){
							//if the code gets to this loop, that means we have a group of 4 tiles to test on
							//add all values to the list, without duplicates
							values = new ArrayList<Integer>();
							for (int y = 0; y < possvals[i][list.get(q1)].get_size(); y++){
								if (!values.contains(possvals[i][list.get(q1)].get(y))){
									values.add(possvals[i][list.get(q1)].get(y));
								}
							}
							for (int y = 0; y < possvals[i][list.get(q2)].get_size(); y++){
								if (!values.contains(possvals[i][list.get(q2)].get(y))){
									values.add(possvals[i][list.get(q2)].get(y));
								}
							}
							for (int y = 0; y < possvals[i][list.get(q3)].get_size(); y++){
								if (!values.contains(possvals[i][list.get(q3)].get(y))){
									values.add(possvals[i][list.get(q3)].get(y));
								}
							}
							for (int y = 0; y < possvals[i][list.get(q4)].get_size(); y++){
								if (!values.contains(possvals[i][list.get(q4)].get(y))){
									values.add(possvals[i][list.get(q4)].get(y));
								}
							}
							// if there are only 4 values amongst the four tiles, then we found one
							if (values.size() == 4){
								quad = new ArrayList<Integer>();
								quad.add(list.get(q1)); 
								quad.add(list.get(q2));
								quad.add(list.get(q3));
								quad.add(list.get(q4));//store the tile coordinates (column-indices)
								flag = true;
							}
							q4++;
						}
						q3++;
					}
					q2++;
				}
				q1++;
			}
			// if we found one, reduce all other cells
			if (flag){
				for (int k = 0; k < 9; k++){
					if (!quad.contains(k)){
						z = 0;
						while (z < possvals[i][k].get_size()){
							if (!values.contains(possvals[i][k].get(z))){
								possvals[i][k].deleteIndex(z);
								sizes[i][k]--;
								removed++;
							}
							else {
								z++;
							}
						}
					}
				}
			}
		}
		
		//then by column
		for (int j = 0; j < 9; j++){
			list = new ArrayList<Integer>();
			quad = new ArrayList<Integer>();
			
			for (int k = 0; k < 9; k++){
				if (grid[k][j] == 0){
					if (possvals[k][j].get_size() < 5){
						list.add(k);
					}
				}
			}
			q1 = 0;
			flag = false;
			while ((q1 < list.size())&&(!flag)){
				q2 = q1 + 1;
				while ((q2 < list.size())&&(!flag)){
					q3 = q2 + 1;
					while ((q3 < list.size())&&(!flag)){
						q4 = q3 + 1;
						while ((q4 < list.size())&&(!flag)){
							//if the code gets to this loop, that means we have a group of 4 tiles to test on
							//add all values to the list, without duplicates
							values = new ArrayList<Integer>();
							for (int y = 0; y < possvals[list.get(q1)][j].get_size(); y++){
								if (!values.contains(possvals[list.get(q1)][j].get(y))){
									values.add(possvals[list.get(q1)][j].get(y));
								}
							}
							for (int y = 0; y < possvals[list.get(q2)][j].get_size(); y++){
								if (!values.contains(possvals[list.get(q2)][j].get(y))){
									values.add(possvals[list.get(q2)][j].get(y));
								}
							}
							for (int y = 0; y < possvals[list.get(q3)][j].get_size(); y++){
								if (!values.contains(possvals[list.get(q3)][j].get(y))){
									values.add(possvals[list.get(q3)][j].get(y));
								}
							}
							for (int y = 0; y < possvals[list.get(q4)][j].get_size(); y++){
								if (!values.contains(possvals[list.get(q4)][j].get(y))){
									values.add(possvals[list.get(q4)][j].get(y));
								}
							}
							// if there are only 4 values amongst the four tiles, then we found one
							if (values.size() == 4){
								quad = new ArrayList<Integer>();
								quad.add(list.get(q1)); 
								quad.add(list.get(q2));
								quad.add(list.get(q3));
								quad.add(list.get(q4));//store the tile coordinates (column-indices)
								flag = true;
							}
							q4++;
						}
						q3++;
					}
					q2++;
				}
				q1++;
			}
			// if we found one, reduce all other cells
			if (flag){
				for (int k = 0; k < 9; k++){
					if (!quad.contains(k)){
						z = 0;
						while (z < possvals[k][j].get_size()){
							if (!values.contains(possvals[k][j].get(z))){
								possvals[k][j].deleteIndex(z);
								sizes[k][j]--;
								removed++;
							}
							else {
								z++;
							}
						}
					}
				}
			}			
		}
		
		//finally, by box
		ArrayList<Tuple<Integer, Integer>> list2 = new ArrayList<Tuple<Integer, Integer>>();
		ArrayList<Tuple<Integer, Integer>> quad2 = new ArrayList<Tuple<Integer, Integer>>();
		for (int i = 0; i < 9; i = i+3){
			for (int j = 0; j < 9; j = j+3){
				list2 = new ArrayList<Tuple<Integer, Integer>>();
				quad2 = new ArrayList<Tuple<Integer, Integer>>();
				
				for (int k = i; k < i+3; k++){
					for (int q = j; q < j+3; q++){
						if (grid[k][q] == 0){
							if (possvals[k][q].get_size() < 5){
								list2.add(new Tuple<Integer, Integer>(k, q));
							}
						}
					}
				}

				q1 = 0;
				flag = false;
				while ((q1 < list2.size())&&(!flag)){
					q2 = q1 + 1;
					while ((q2 < list2.size())&&(!flag)){
						q3 = q2 + 1;
						while ((q3 < list2.size())&&(!flag)){
							q4 = q3 + 1;
							while ((q4 < list2.size())&&(!flag)){
								values = new ArrayList<Integer>();
								for (int y = 0; y < possvals[list2.get(q1).first][list2.get(q1).second].get_size(); y++){
									if (!values.contains(possvals[list2.get(q1).first][list2.get(q1).second].get(y))){
										values.add(possvals[list2.get(q1).first][list2.get(q1).second].get(y));
									}
								}
								for (int y = 0; y < possvals[list2.get(q2).first][list2.get(q2).second].get_size(); y++){
									if (!values.contains(possvals[list2.get(q2).first][list2.get(q2).second].get(y))){
										values.add(possvals[list2.get(q2).first][list2.get(q2).second].get(y));
									}
								}
								for (int y = 0; y < possvals[list2.get(q3).first][list2.get(q3).second].get_size(); y++){
									if (!values.contains(possvals[list2.get(q3).first][list2.get(q3).second].get(y))){
										values.add(possvals[list2.get(q3).first][list2.get(q3).second].get(y));
									}
								}
								for (int y = 0; y < possvals[list2.get(q4).first][list2.get(q4).second].get_size(); y++){
									if (!values.contains(possvals[list2.get(q4).first][list2.get(q4).second].get(y))){
										values.add(possvals[list2.get(q4).first][list2.get(q4).second].get(y));
									}
								}
								// if there are only 4 values amongst the four tiles, then we found one
								if (values.size() == 4){
									quad2 = new ArrayList<Tuple<Integer, Integer>>();
									quad2.add(list2.get(q1)); 
									quad2.add(list2.get(q2));
									quad2.add(list2.get(q3));
									quad2.add(list2.get(q4));//store the tile coordinates (column-indices)
									flag = true;
								}
								q4++;
							}
							q3++;
						}
						q2++;
					}
					q1++;
				}
				// if we found one, reduce all other cells
				if (flag){
					for (int r = i; r < i+3; r++){
						for (int c = j; c < j+3; c++){
							Tuple<Integer, Integer> tup = new Tuple<Integer, Integer>(r, c);
							if (!containsTup(quad2, tup)){
								z = 0;
								while(z < possvals[tup.first][tup.second].get_size()){
									if (values.contains(possvals[tup.first][tup.second].get(z))){
										possvals[tup.first][tup.second].deleteIndex(z);
										sizes[tup.first][tup.second]--;
										removed++;
									}
									else{
										z++;
									}
								}
							}
						}
					}
				}
			}
		}
		return removed;
	}
	
	
	public int hiddenQuads(){
		//also rare to find
		//similar to other hidden groups, but we'll change up the algorithm pattern for efficiency
		ArrayList<Integer> list = new ArrayList<Integer>();
		boolean flag;
		int p1, p2, p3, p4; //the 4 values that we'll test
		//by row
		for (int i = 0; i < 9; i++){
			//add all values to list
			for (int k = 0; k < 9; k++){
				list.addAll(possvals[i][k].getEntries());
			}
			p1 = 0;
			flag = false;
			while ((p1 < 9)&&(!flag)){
				p1++;
			}
			
			
			
		}
		
		//by column
		for (int j = 0; j < 9; j++){
			
			for (int k = 0; k < 9; k++){
				list.addAll(possvals[k][j].getEntries());
			}
			
			
		}
		//by box
		for (int i = 0; i < 9; i++){
			for (int j = 0; j < 9; j++){
				
			}
		}
		return removed;
	}
	
	public int pointingPairs(){
		return count;
	}
	
	public int pointingTrips(){
		return count;
	}
	
	public int boxLinePairs(){
		return count;
	}
	
	public int boxLineTrips(){
		return count;
	}
	
	public int xWingRedux(){
		return count;
	}
	
	public int singlesChains(){
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
	public boolean containsTup(ArrayList<Tuple<Integer, Integer>> list, Tuple<Integer, Integer> tup){
		for (int i = 0; i < list.size(); i++){
			if ((list.get(i).first == tup.first)&&(list.get(i).second == tup.second)){
				return true;
			}
		}
		return false;
	}
	
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
	
	public ArrayList<Integer> tupToArray(Tuple<Integer, Integer> tup){
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(tup.first);
		list.add(tup.second);
		return list;
	}
	
	public ArrayList<Integer> tripToArray(TripleTup<Integer, Integer, Integer> trip){
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(trip.first);
		list.add(trip.second);
		list.add(trip.third);
		return list;
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
    
    //getters and setters
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
    
    public boolean check_init(){
    	if (init == 1){
    		return true;
    	}
    	else {
    		return false;
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
    	init = 0;
    }
}
