import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

    public class Sudoku{
        private int[][] gridvals = new int[9][9];  //the values
        private int[][] tempgrid = new int[9][9];
        private JLabel[][] gridlabels = new JLabel[9][9]; //the text corresponding to all the values
        private int xval;
        private int yval;
        private int vert_numb;
        private int horiz_numb;
        private int[][] Aigrid = new int[9][9];
        private int[][] colorshift = new int[9][9];
        private int[][] solutiongrid = new int[9][9]; //the answer
        private int[][] startergrid = new int[9][9]; // the grid of values you begin with
        private boolean flag2 = true; // used to check if the puzzle has been finalized
        private int error_status; //0 is default, 1 is for finalize
        private JFrame frame = new JFrame("Sudoku Sandbox");
        private JPanel parent = new JPanel();
        private JPanel parent2 = new JPanel();
        private String[] letter_axis = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
        private final int initx = 160; //position of the frame
        private final int inity = 102;
        private final int fontsize = 50;
        private final int min_req = 25;
        private final int bp1 = 4;
        private final int bp2 = 4;
        //bp1 and bp2 are for buildPuzzle's randval.nextInt(bp1)+bp2
        
        private AIsolve game = new AIsolve(gridvals);
        
        private Entry[][] possvals = new Entry[9][9];
        private JLabel[][][] minilabels = new JLabel[9][9][9];
        private CellPane[][] cellpanes = new CellPane[9][9]; //maybe can be used for some shading
        private ArrayList<JLabel> labellist = new ArrayList<JLabel>();
        private ArrayList<String> logtext = new ArrayList<String>(); //for the TextLog
        private int logtextMarker = 0;
        private ArrayList<TripleTup<Integer, Integer, Tuple<Integer, Integer>>> tile_log = 
                new ArrayList<TripleTup<Integer, Integer, Tuple<Integer, Integer>>>();
        private JPanel dummypanel = new JPanel();
        private JScrollPane scrollPane = new JScrollPane(dummypanel);   
        private int CI = 0;
                
        //start code
        public static void main(String[] args) {
            new Sudoku();
        }

        //Setup Environment
        public Sudoku() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                            UnsupportedLookAndFeelException ex) {
                    }
                    
                    for (int i = 0; i <= 8; i++){
                        for (int j = 0; j <= 8; j++){
                            possvals[i][j] = new Entry(); //to avoid out of bounds error
                            startergrid[i][j] = 0;
                        }
                    }
                    
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setLayout(new BorderLayout());
                    parent.setLayout(new BoxLayout(parent,BoxLayout.LINE_AXIS));                
                    parent2.setLayout(new BoxLayout(parent2, BoxLayout.PAGE_AXIS));
                    
                    //parent2.add((new SidePanel().contentPane));
                    parent2.add((new JLabelStuff().box));
                    parent2.add((new OutputTextPanel()).contentPane);

                    parent2.add((new OutputTextPanel3()).contentPane);
                    parent2.add((new OutputTextPanel2()).contentPane);
                    parent2.add((new OutputTextPanel4()).contentPane);
                    parent2.add(new JLabel("@sputnik7921"));
              
                    frame.add(parent);
                    parent.add(new CellGridPane());
                    parent.add(parent2);
                    //parent2.add(xlabel);
                    //parent2.add(ylabel);
                    //frame.add(new SidePanel().contentPane);
                    //frame.add(new CellGridPane()); 
                    //frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.pack();
                    frame.addComponentListener(new ComponentAdapter() {
                        public void componentResized(ComponentEvent e) {
                            frame.setSize(1030,768);    // or whatever your full size is
                         }
                         public void componentMoved(ComponentEvent e) {
                            frame.setLocation(initx, inity);
                         }
                      });
                    frame.setResizable(false);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                }
            });
        }

        //creates the frame of the grid
        @SuppressWarnings("serial")
        public class CellGridPane extends JPanel {

            public CellGridPane() {
                setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                for (int row = 0; row <= 8; row++) {
                    for (int col = 0; col <= 8; col++) {
                        gbc.gridx = col;
                        gbc.gridy = row;
                        CellPane cellPane = new CellPane(); //JPanels
                        Border border = new MatteBorder(((row % 3 == 0)&&(row != 0) ? 4 : 1), ((col % 3 == 0)&&(col != 0) ? 4 : 1), 
                                (row == 8 ? 1 : 0), (col == 8 ? 1 : 0), Color.GRAY);
                        
                        //face-typing the labels
                        cellPane.setBorder(border);
                        JLabel numb = new JLabel(" ");
                        numb.setFont(new Font("Serif", Font.PLAIN, fontsize));
                        cellPane.add(numb);
                        gridvals[row][col] = 0; //initialize the gridvals grid with all 0's
                        gridlabels[row][col] = numb; // initialize the gridlabels with all " "
                                                    // gridlabels references the labels on the actual GUI
                        cellpanes[row][col] = cellPane;
                        this.add(cellPane, gbc);
                    }
                }
            }
        }
     
        @SuppressWarnings("serial")
        public class JLabelStuff extends JFrame
        {
            private Box box;
            public JLabelStuff()
            {
                box = Box.createVerticalBox();
                setPreferredSize(new Dimension(400, 120));
                add( box );
                JLabel label = new JLabel("<html><BR>Welcome to SudokuSandbox v1.1 <BR><BR></html>");
                JLabel label2 = new JLabel("<html>Click on any square to build a <BR> pre-existing sudoku puzzle, or<BR>"
                        + "press 'Generate Puzzle' to solve<BR> a random sudoku puzzle   "
                        + "<BR><BR><BR> </html>");
                label.setFont(new Font("Serif", Font.BOLD, 20));
                label2.setFont(new Font("Serif", Font.BOLD, 20));
                label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
                box.add(label);
                label2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
                box.add(label2);
            }
        }
        
        @SuppressWarnings("serial")
        public class OutputTextPanel extends JPanel implements ActionListener{
            
            private JPanel contentPane = new JPanel(null);
            Color color = new Color(173,216,230); //powder blue
            JButton finalize = new JButton("Generate Puzzle");
            JButton other = new JButton("Clear Grid");
            
            public OutputTextPanel(){   
                JPanel panel = new JPanel();
                panel.setBackground(color);
                finalize.setFont(new Font("Serif", Font.BOLD, 16));
                other.setFont(new Font("Serif", Font.BOLD, 16));
                panel.add(finalize);
                panel.add(other);
                finalize.addActionListener(this);
                other.addActionListener(this);
                JScrollPane scrollPane = new JScrollPane(panel);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setBounds(0, 0, 330, 90);
                contentPane.add(scrollPane);
                contentPane.setPreferredSize(new Dimension(330, 90));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == finalize){
                    for (int i = 0; i <= 8; i++){
                        for (int j = 0; j <= 8; j++){
                            colorshift[i][j] = 0;
                            startergrid[i][j] = 0;
                        }
                    }
                    randomize();
                    error_status = 1;
                    for (int i = 0; i <= 8; i++){
                        for (int j = 0; j <= 8; j++){
                            colorshift[i][j] = 0;
                        }
                    }
                    logtext = new ArrayList<String>();
                    logtextMarker = 0;
                    dummypanel.removeAll();
                    //print_to_console();
                    flag2 = true;
                    error_status = 0;
                    CI = 0;
                    labellist = new ArrayList<JLabel>();
                }           
                else if (e.getSource() == other) { 
                    for (int i = 0; i <= 8; i++){
                        for (int j = 0; j <= 8; j++){
                            gridvals[i][j] = 0;
                            solutiongrid[i][j] = 0;
                            colorshift[i][j] = 0;
                            startergrid[i][j] = 0;
                            gridlabels[i][j].setForeground(Color.BLACK);
                        }
                    }
                    displayPuzzle(gridvals);
                    logtext = new ArrayList<String>();
                    logtextMarker = 0;
                    dummypanel.removeAll();
                    //print_to_console();
                    flag2 = true;
                    error_status = 0;
                    CI = 0;
                    labellist = new ArrayList<JLabel>();
                }
            }
        }
        
        @SuppressWarnings("serial")
        public class OutputTextPanel2 extends JPanel implements ActionListener{
            
            private JPanel contentPane = new JPanel(null);
            Color color = new Color(173,216,230); //powder blue
            //JButton jb1 = new JButton("Some other button");
            JButton jb2 = new JButton("Use The AI Solver");
            
            public OutputTextPanel2(){
                JPanel panel = new JPanel();
                panel.setBackground(color);
                //jb1.setFont(new Font("Serif", Font.BOLD, 16));
                jb2.setFont(new Font("Serif", Font.BOLD, 16));
                panel.add(jb2);
                jb2.addActionListener(this);
                JScrollPane scrollPane = new JScrollPane(panel);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setBounds(0, 0, 330, 65);
                contentPane.add(scrollPane);
                contentPane.setPreferredSize(new Dimension(330, 65));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == jb2){
                    int z = get_count(gridvals);
                    if (z < min_req){
                        new Popup2(min_req - z);
                    }
                    else if ((z >= min_req)&&(get_count(startergrid) == 0)){
                        new Popup2(min_req - z);
                    }
                    else {
                        new AIStepper();
                    }
                    
                }
            }
        }
       
        @SuppressWarnings("serial")
        public class OutputTextPanel3 extends JPanel implements ActionListener{
            
            private JButton restart = new JButton("Restart Current Puzzle");
            private JButton finset = new JButton("Finalize Puzzle");
            private JPanel contentPane = new JPanel(null);
            Color color = new Color(173,216,230); //powder blue
            JLabel space = new JLabel("       ");
                    
            public OutputTextPanel3(){
                JPanel panel = new JPanel();
                panel.setBackground(color);
                restart.setFont(new Font("Serif", Font.BOLD, 16));
                finset.setFont(new Font("Serif", Font.BOLD, 16));
                panel.add(restart);
                panel.add(finset);
                restart.addActionListener(this);
                finset.addActionListener(this);
                JScrollPane scrollPane = new JScrollPane(panel);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setBounds(0, 0, 330, 65);
                contentPane.add(scrollPane);
                contentPane.setPreferredSize(new Dimension(330, 65));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == restart){
                    for (int i = 0; i < 9; i++){
                        for (int j = 0; j < 9; j++){
                            if (startergrid[i][j] == 0){
                                gridvals[i][j] = 0;
                                Aigrid[i][j] = 0;
                                colorshift[i][j] = 0;
                            }
                        }
                    }
                    game.reset_AI(startergrid);
                    displayPuzzle(startergrid);
                    logtext = new ArrayList<String>();
                    logtextMarker = 0;
                    dummypanel.removeAll();
                    CI = 0;
                    labellist = new ArrayList<JLabel>();
                }
                else {
                    if (flag2){
                        int z = get_count(gridvals);
                        if (z < min_req){
                            new Popup2(min_req - z);
                        }
                        else{
                            flag2 = false;
                            error_status = 1;
                            Color maroon = new Color (170, 0, 0);
                            Font fonty = new Font("Serif", Font.PLAIN, fontsize);
                            String strlabel = new String();
                            for (int i = 0; i <= 8; i++){
                                for (int j = 0; j <= 8; j++){
                                    startergrid[i][j] = gridvals[i][j];
                                    if (startergrid[i][j] == 0){
                                        gridlabels[i][j].setText(" ");
                                        gridlabels[i][j].setForeground(new Color(0, 0, 0));
                                    }
                                    else{
                                        strlabel = Integer.toString(startergrid[i][j]);
                                        gridlabels[i][j].setFont(fonty);                        
                                        gridlabels[i][j].setText(strlabel);
                                        gridlabels[i][j].setForeground(maroon);
                                    }
                                }
                            }
                        }
                    }
                    else{
                        new Popup();
                    }
                }
            }
        }
        
        @SuppressWarnings("serial")
        public class OutputTextPanel4 extends JPanel implements ActionListener{
            
            private JPanel contentPane = new JPanel(null);
            Color color = new Color(173,216,230); //powder blue
            //JButton jb1 = new JButton("Some other button");
            JButton jb1 = new JButton("Import Puzzle");
            JButton jb2 = new JButton("Save Puzzle");
            
            public OutputTextPanel4(){
                JPanel panel = new JPanel();
                panel.setBackground(color);
                jb1.setFont(new Font("Serif", Font.BOLD, 16));
                jb2.setFont(new Font("Serif", Font.BOLD, 16));
                panel.add(jb1);
                panel.add(jb2);
                jb1.addActionListener(this);
                jb2.addActionListener(this);
                JScrollPane scrollPane = new JScrollPane(panel);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setBounds(0, 0, 330, 65);
                contentPane.add(scrollPane);
                contentPane.setPreferredSize(new Dimension(330, 65));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == jb1){
                    new FileLoader();
                }
                if (e.getSource() == jb2) {
                    new FileSaver();
                }
            }
        }
         
        //-------Additional JComponents
        
        @SuppressWarnings("serial")
        public class FileLoader extends JFrame implements ActionListener
        {
            private static final int WHEN_ANCESTOR_OF_FOCUSED_COMPONENT = 0;
            private JLabel label;
            private JTextField field;
            private JButton btn = new JButton("");
            
            public FileLoader()
            {
                super("Enter File Name To Import (include .txt)");
                //setDefaultCloseOperation(EXIT_ON_CLOSE);
                setPreferredSize(new Dimension(450, 100));
                ((JPanel) getContentPane()).setBorder(new EmptyBorder(13, 13, 13, 13) );
                setLayout(new FlowLayout());       
                btn.setText("Import File");
                btn.setActionCommand("myButton");
                btn.addActionListener(this);
                label = new JLabel("");
                field = new JTextField(10);
                field.setFont(new Font("Arial", Font.PLAIN, 14));
                add(field);
                add(btn);
                add(label);
                pack();
                setLocationRelativeTo(null);
                setVisible(true);
                setResizable(false);      
                
                InputMap im = btn.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                ActionMap am = btn.getActionMap();

                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "spaced");
                am.put("spaced", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doButtonPressed(e);                  
                    }

                    private void doButtonPressed(ActionEvent e) {
                        actionPerformed(e);
                    }

                });                
            }
            
            public void actionPerformed(ActionEvent e){
                
                if(e.getActionCommand().equals("myButton"))
                {
                    String str = field.getText();
                    String caution = "File not found";
                    File file = new File(str);
                    BufferedReader reader = null;
                    String text;
                    String[] parts;
                    
                    if (!file.isFile()){
                        label.setText(caution);
                    }
                    else{
                        try {
                            reader = new BufferedReader(new FileReader(file));
                            for (int i = 0; i < 9; i++){
                                text = reader.readLine();
                                parts = text.split(" ");
                                for (int j = 0; j < 9; j++){
                                    gridvals[i][j] = Integer.parseInt(parts[j]);
                                    startergrid[i][j] = Integer.parseInt(parts[j]);
                                }
                            }
                            reader.close();
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                        }
                        displayPuzzle(gridvals);
                        super.dispose();
                    }
                }
            }
            
            @Override
            public void addNotify() {
                super.addNotify();
                SwingUtilities.getRootPane(btn).setDefaultButton(btn);
            }
            
        }
        
        @SuppressWarnings("serial")
        public class FileSaver extends JFrame implements ActionListener
        {
            private static final int WHEN_ANCESTOR_OF_FOCUSED_COMPONENT = 0;
            private JLabel label;
            private JTextField field;
            private JButton btn = new JButton("");
            
            public FileSaver()
            {
                super("Enter File Name To Save To (include .txt)");
                //setDefaultCloseOperation(EXIT_ON_CLOSE);
                setPreferredSize(new Dimension(450, 100));
                ((JPanel) getContentPane()).setBorder(new EmptyBorder(13, 13, 13, 13) );
                setLayout(new FlowLayout());       
                btn.setText("Save File");
                btn.setActionCommand("myButton");
                btn.addActionListener(this);
                label = new JLabel("");
                field = new JTextField(10);
                field.setFont(new Font("Arial", Font.PLAIN, 14));
                add(field);
                add(btn);
                add(label);
                pack();
                setLocationRelativeTo(null);
                setVisible(true);
                setResizable(false);      
                
                InputMap im = btn.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                ActionMap am = btn.getActionMap();

                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "spaced");
                am.put("spaced", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doButtonPressed(e);                  
                    }

                    private void doButtonPressed(ActionEvent e) {
                        actionPerformed(e);
                    }

                });
                
            }
            
            public void actionPerformed(ActionEvent e){
                
                if(e.getActionCommand().equals("myButton"))
                {
                    String str = field.getText();
                    File file = new File(str);

                    try{
                        if (!file.isFile()){
                            File file2 = new File(str);
                            file2.createNewFile();
                        }
                        try{
                            PrintWriter writer = new PrintWriter(str, "UTF-8");
                            for (int i = 0; i < 9 ; i++){
                                for (int j = 0; j < 8; j++){
                                    writer.print(gridvals[i][j]+" ");
                                }
                                writer.println(gridvals[i][8]);
                            }                           
                            writer.close();
                        }
                        catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                    catch(Exception ex2){
                        ex2.printStackTrace();
                    }
                    super.dispose();
                    new Popup3();
                }

            }
            
            @Override
            public void addNotify() {
                super.addNotify();
                SwingUtilities.getRootPane(btn).setDefaultButton(btn);
            }
            
        }
        
        //need a jpanel to hold jlabels
        @SuppressWarnings("serial")
        public class DummyPane extends JPanel{
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(65, 65);
            }
        }
        
        //for the minigrid
        @SuppressWarnings("serial")
        public class CellGridPane2 extends JPanel {
            int minifont = 15;
            public CellGridPane2() {
                setLayout(new GridBagLayout());

                GridBagConstraints gbc = new GridBagConstraints();
                for (int row = 0; row <= 8; row++) {
                    for (int col = 0; col <= 8; col++) {
                        gbc.gridx = col;
                        gbc.gridy = row;
                        DummyPane cellPane = new DummyPane(); //JPanels
                        Border border = new MatteBorder(((row % 3 == 0)&&(row != 0) ? 4 : 1), ((col % 3 == 0)&&(col != 0) ? 4 : 1), 
                                (row == 8 ? 1 : 0), (col == 8 ? 1 : 0), Color.GRAY);
                        
                        cellPane.setBorder(border);
                        for (int q = 1; q < 10; q++){
                            JLabel numb = new JLabel("" + q);
                            numb.setFont(new Font("Serif", Font.PLAIN, minifont));
                            cellPane.add(numb);
                            minilabels[row][col][q-1] = numb;
                        }
                        this.add(cellPane, gbc);
                    }
                }
            }
        }
        
        @SuppressWarnings("serial")
        public class Popup extends JFrame {         
            public Popup(){ 
                super("Error");
                setPreferredSize(new Dimension(300, 80));
                ((JPanel) getContentPane()).setBorder(new EmptyBorder(13, 13, 13, 13) );
                setLayout(new FlowLayout()); 
                JLabel txt = new JLabel("This feature is not available");
                JLabel txt2 = new JLabel("Cannot finalize puzzle more than once");
                if (!flag2){
                    if (error_status == 1){
                        this.add(txt2);
                    }
                    else {
                        this.add(txt);
                    }
                }
                else {
                    this.add(txt);
                } 
                pack();
                setLocationRelativeTo(null);
                setVisible(true);
                setResizable(false);    
            }
        }
     
        @SuppressWarnings("serial")
        public class Popup2 extends JFrame {            
            public Popup2(int n){
                super("Error");
                setPreferredSize(new Dimension(400, 80));
                ((JPanel) getContentPane()).setBorder(new EmptyBorder(13, 13, 13, 13));
                setLayout(new FlowLayout()); 
                if (n > 0){
                    JLabel txt = new JLabel("You need at least "+(n)+" more inputs before finalizing the puzzle");
                    this.add(txt);
                }
                else {
                    JLabel txt = new JLabel("Puzzle needs to be finalized before using AIsolver");
                    this.add(txt);
                }
                pack();
                setLocationRelativeTo(null);
                setVisible(true);
                setResizable(false);    
            }
        }
        
        @SuppressWarnings("serial")
        public class Popup3 extends JFrame {            
            public Popup3(){ //1 for the saved message, 0 for all else
                super("");
                setPreferredSize(new Dimension(300, 80));
                ((JPanel) getContentPane()).setBorder(new EmptyBorder(13, 13, 13, 13) );
                setLayout(new FlowLayout()); 
                JLabel txt = new JLabel("Puzzle Has Been Saved");
                this.add(txt);
                pack();
                setLocationRelativeTo(null);
                setVisible(true);
                setResizable(false);    
            }
        }
        
        @SuppressWarnings("serial")
        public class TextLog extends JFrame { 
            
            public TextLog(){ 
                super("Log");
                setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                setPreferredSize(new Dimension(300, 560));
                dummypanel.setLayout(new BoxLayout(dummypanel, BoxLayout.PAGE_AXIS));
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setBounds(0, 0, 330, 65);
                ((JPanel) getContentPane()).setBorder(new EmptyBorder(1, 1, 1, 1) );
                scrollPane.setLayout(new ScrollPaneLayout()); 
                add(scrollPane);
                setLocation(initx + 1320, inity + 132);
                pack();
                setVisible(true);
                setResizable(false);     
            }
            
            public void print_log(){
                logtext.addAll(game.get_TextLog());
                game.clear_TextLog();
                while (logtextMarker < logtext.size()){
                    JLabel output = new JLabel();
                    output.setText(logtext.get(logtextMarker));
                    labellist.add(output);
                    scrollPane.add(labellist.get(logtextMarker));
                    dummypanel.add(labellist.get(logtextMarker));
                    logtextMarker++;
                }
                logtext.add("Break"); //separate the different stages of the solving algorithms
                labellist.add(new JLabel("---------------------------"));
                dummypanel.add(labellist.get(logtextMarker));
                logtextMarker++;
                scrollPane.revalidate();
                scrollPane.repaint();
                dummypanel.revalidate();
                dummypanel.repaint();
                super.pack();
            }
            
        }
        
        @SuppressWarnings("serial")
        public class MiniGrid extends JFrame {          
            public MiniGrid(){
                super("AI View");
                setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                setPreferredSize(new Dimension(600+40, 623+20+20+20));
                ((JPanel) getContentPane()).setBorder(new EmptyBorder(1, 1, 1, 1));
                setLayout(new FlowLayout());
                JPanel parent3 = new JPanel(); //labeling the axis
                JPanel parent4 = new JPanel();
                JPanel subpar1 = new JPanel();
                JPanel subpar2 = new JPanel();
                parent4.setLayout(new BoxLayout(parent4, BoxLayout.PAGE_AXIS));
                parent3.setLayout(new BoxLayout(parent3, BoxLayout.LINE_AXIS));
                subpar1.setLayout(new BoxLayout(subpar1, BoxLayout.LINE_AXIS)); //horiz labels
                subpar2.setLayout(new BoxLayout(subpar2, BoxLayout.PAGE_AXIS)); //vert labels
                subpar1.setPreferredSize(new Dimension(600+40, 15));
                
                String haxis = "   A             B              C            "
                        + "  D             E              F              G        "
                        + "      H              I            "; //Not the best way, but multiple
                                                                // labels leave too much spacing
                JLabel temp = new JLabel(haxis);
                temp.setFont(new Font("Serif", Font.PLAIN, 15));
                temp.setForeground(new Color (170, 0, 0));
                subpar1.add(temp);
                subpar2.add(new JLabel(" "));
                subpar2.add(new JLabel(" "));
                for (int a = 0; a < 2; a++){
                    JLabel temp2 = new JLabel(""+(a+1)+"       ");
                    temp2.setFont(new Font("Serif", Font.PLAIN, 15));
                    temp2.setForeground(new Color (170, 0, 0));
                    subpar2.add(temp2);
                    subpar2.add(new JLabel(" "));
                    subpar2.add(new JLabel(" "));
                    subpar2.add(new JLabel(" "));   
                }
                JLabel temp3 = new JLabel(""+(3)+"       ");
                temp3.setFont(new Font("Serif", Font.PLAIN, 15));
                temp3.setForeground(new Color (170, 0, 0));
                subpar2.add(temp3);
                subpar2.add(new JLabel(" "));
                subpar2.add(new JLabel(" "));
                //subpar2.add(new JLabel(" "));
                for (int a = 3; a < 5; a++){
                    JLabel temp2 = new JLabel(""+(a+1)+"       ");
                    temp2.setFont(new Font("Serif", Font.PLAIN, 15));
                    temp2.setForeground(new Color (170, 0, 0));
                    subpar2.add(temp2);
                    subpar2.add(new JLabel(" "));
                    subpar2.add(new JLabel(" "));
                    subpar2.add(new JLabel(" "));
                }
                JLabel temp4 = new JLabel(""+(6)+"       ");
                temp4.setFont(new Font("Serif", Font.PLAIN, 15));
                temp4.setForeground(new Color (170, 0, 0));
                subpar2.add(temp4);
                subpar2.add(new JLabel(" "));
                subpar2.add(new JLabel(" "));
                //subpar2.add(new JLabel(" "));
                for (int a = 6; a < 8; a++){
                    JLabel temp2 = new JLabel(""+(a+1)+"       ");
                    temp2.setFont(new Font("Serif", Font.PLAIN, 15));
                    temp2.setForeground(new Color (170, 0, 0));
                    subpar2.add(temp2);
                    subpar2.add(new JLabel(" "));
                    subpar2.add(new JLabel(" "));
                    subpar2.add(new JLabel(" "));
                }
                JLabel temp2 = new JLabel(""+(9)+"       ");
                temp2.setFont(new Font("Serif", Font.PLAIN, 15));
                temp2.setForeground(new Color (170, 0, 0));
                subpar2.add(temp2);
                subpar2.add(new JLabel(" "));
                subpar2.add(new JLabel(" "));
                //subpar2.add(new JLabel(" "));
                //add(new CellGridPane2());
                parent4.add(subpar1);
                parent3.add(new CellGridPane2());
                parent3.add(subpar2);
                parent4.add(parent3);
                add(parent4);
                setLocation(initx + 657, inity + 222);
                pack();
                setVisible(true);
                setResizable(false); 
            }
            
        }
        
        //iterate through the AI strategy step by step
        @SuppressWarnings("serial")
        public class AIStepper extends JFrame implements ActionListener
        {
            private static final int WHEN_ANCESTOR_OF_FOCUSED_COMPONENT = 0;
            private JLabel label = new JLabel("");
            private JLabel label2 = new JLabel("");
            private JButton step = new JButton("Step Through >>");
            private JButton restart = new JButton("Restart Puzzle");
            //open minigrid
            MiniGrid min = new MiniGrid();
            TextLog tl = new TextLog();
            
            public AIStepper()
            {
                super("Solver");
                //setDefaultCloseOperation(EXIT_ON_CLOSE);
                addWindowListener(new WindowAdapter()
                {
                    @Override
                    public void windowClosing(WindowEvent e) 
                    {
                        tl.dispose();
                        min.dispose();
                        e.getWindow().dispose();
                    }
                });
                min.addComponentListener(new ComponentAdapter() {
                    public void componentMoved(ComponentEvent e) {
                       min.setLocation(initx + 657, inity + 222);
                    }
                 });
                setPreferredSize(new Dimension(465, 100));
                ((JPanel) getContentPane()).setBorder(new EmptyBorder(13, 13, 13, 13) );
                setLayout(new FlowLayout());       
                step.setActionCommand("myButton");
                step.addActionListener(this);
                restart.setActionCommand("restart");
                restart.addActionListener(this);
                add(label);
                add(restart);
                add(step);
                add(label2);
                pack();
                setLocation((initx + 659), (inity + 106));
                //setLocationRelativeTo(null);
                setVisible(true);
                setResizable(false); 
                
                //load game state into AIsolve
                game.setGrid(gridvals);
                game.reset_AI(startergrid);
                
                InputMap im = step.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                ActionMap am = step.getActionMap();

                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "spaced");
                am.put("spaced", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doButtonPressed(e);                  
                    }

                    private void doButtonPressed(ActionEvent e) {
                        actionPerformed(e);
                    }
                });
                
                
            }
            
            public void actionPerformed(ActionEvent e)
            {
                if(e.getActionCommand().equals("myButton"))
                {//AI stepper
                    if (get_count(gridvals) == 81){
                        label.setText("Puzzle is completed");
                        clearMini();
                        label2.setText("");
                    }
                    else{
                        for (int i = 0; i < 9; i++){
                            for (int j = 0; j < 9; j++){
                                colorshift[i][j] = 0;
                            }
                        }
                        displayPuzzle2(gridvals);
                        game.updatePossvals();
                        updateMainPV();
                        displayMinigrid(minilabels);
                        game.updateSizes();
                        //this is where you get the possvals, and display them on the minigrid
                        game.solve();
                        tempgrid = game.getGrid();
                        for (int i = 0; i <= 8; i++){
                            for (int j = 0; j <= 8; j++){
                                gridvals[i][j] = tempgrid[i][j];  
                                if (startergrid[i][j] == 0){
                                    if (Aigrid[i][j] == 0){
                                        colorshift[i][j] = 1;
                                    }
                                    Aigrid[i][j] = tempgrid[i][j];
                                }
                            }
                        }
                        if (game.get_type() == 1){
                            label.setText("Removed "+game.getCount()+" values");
                        }
                        else if (game.get_type() == 2){
                            label.setText("Filled in 1 tile");
                        }
                        else if (game.getCount() == 0){
                            label.setText("Initializing mini grid");
                        }
                        else{
                            label.setText("Found "+game.getCount()+" values");
                        }
                        if (game.get_type() != 2){
                            label2.setText("Current Strategy: "+game.get_strat()); //display current method in use
                        }
                        else {
                            label2.setText("Multiple Solutions Found");
                        }
                        displayPuzzle2(gridvals);
                        if (game.get_type() == 1){ 
                            updateMainPV();
                            displayMinigrid(minilabels);
                            game.updateSizes();
                        }
                        //print to scrollpane
                        if (CI == 0){
                            CI = 1;
                        }
                        else {
                            tl.print_log();
                        }
                    }
                }
                else if(e.getActionCommand().equals("restart")) { 
                    for (int i = 0; i < 9; i++){
                        for (int j = 0; j < 9; j++){
                            if (startergrid[i][j] == 0){
                                gridvals[i][j] = 0;
                                Aigrid[i][j] = 0;
                                colorshift[i][j] = 0;
                            }
                        }
                    }
                    game.reset_AI(startergrid);
                    label.setText("");
                    label2.setText("");
                    displayPuzzle(gridvals);
                    restartMini();
                    logtext = new ArrayList<String>();
                    logtextMarker = 0;
                    dummypanel.removeAll();
                    dummypanel.revalidate();
                    dummypanel.repaint();
                    scrollPane.revalidate();
                    scrollPane.repaint();
                    tl.pack();
                    CI = 0;
                    labellist = new ArrayList<JLabel>();
                }
                
            }
            
            @Override
            public void addNotify() {
                super.addNotify();
                SwingUtilities.getRootPane(step).setDefaultButton(step);
            }
            
        }
                
        
        //GUI tools-------------
        //shading for hovering cursor tool
        @SuppressWarnings("serial")
        public class CellPane extends JPanel {
            
            private Color defaultBackground;
            
            public CellPane() {
                addMouseListener(new MouseAdapter() {
                    
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        defaultBackground = getBackground();
                        setBackground(Color.GRAY);
                    }
                    
                    @Override
                    public void mouseClicked(MouseEvent e){
                        
                        setBackground(Color.LIGHT_GRAY);
                        {
                            PointerInfo a = MouseInfo.getPointerInfo();
                            Point b = a.getLocation();
                            xval = (int) b.getX();
                            yval = (int) b.getY();
                        }
                        new TextEdit();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        setBackground(defaultBackground);
                    }
                });
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(70, 70);
            }
        }
        
        @SuppressWarnings("serial")
        public class TextEdit extends JFrame implements ActionListener
        {
            private static final int WHEN_ANCESTOR_OF_FOCUSED_COMPONENT = 0;
            private JLabel label;
            private JButton delete = new JButton("delete");
            private JTextField field;
            private JButton btn = new JButton("");
            private ArrayList<String> numblist = new ArrayList<String>(Arrays.asList("1", "2", "3", "4", "5", 
                    "6", "7", "8", "9"));
            
            public TextEdit()
            {
                super("Enter A Number Into The Square");
                //setDefaultCloseOperation(EXIT_ON_CLOSE);
                setPreferredSize(new Dimension(450, 130));
                ((JPanel) getContentPane()).setBorder(new EmptyBorder(13, 13, 13, 13) );
                setLayout(new FlowLayout());       
                btn.setText("ADD NUMBER");;
                btn.setActionCommand("myButton");
                btn.addActionListener(this);
                delete.setActionCommand("delButton");
                delete.addActionListener(this);
                label = new JLabel("");
                field = new JTextField(4);
                field.setFont(new Font("Arial", Font.PLAIN, 30));
                add(field);
                add(btn);
                add(delete);
                add(label);
                pack();
                setLocationRelativeTo(null);
                setVisible(true);
                setResizable(false);      
                
                InputMap im = btn.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                ActionMap am = btn.getActionMap();

                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "spaced");
                am.put("spaced", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doButtonPressed(e);                  
                    }

                    private void doButtonPressed(ActionEvent e) {
                        actionPerformed(e);
                    }

                });
                
            }
            
            public void actionPerformed(ActionEvent e)
            //after a button is pressed, check which one it was and 
            //the conditions of the tile the cursor was on
            {
                int temp = 0;
                horiz_numb = ((xval - (initx + 19)) / 70);
                vert_numb = ((yval - (inity + 83)) / 70);
                if(e.getActionCommand().equals("myButton"))
                {
                    String tempstr = field.getText();
                    String caution = "Not a valid input";
                    
                    //safeguarding for changing Ai-produced values as well
                    if ((startergrid[vert_numb][horiz_numb] > 0)||(Aigrid[vert_numb][horiz_numb] > 0)){
                        caution = "Cannot change number" ;
                    }
                    else if (numblist.contains(tempstr)){
                        boolean test = noConflictInput(tempstr, vert_numb, horiz_numb);
                        if (test){
                            label.setText(tempstr);
                            JLabel templabel = new JLabel("");  
                            templabel = gridlabels[vert_numb][horiz_numb];
                            templabel.setText(field.getText());
                            temp = Integer.parseInt(field.getText());
                            gridvals[vert_numb][horiz_numb] = temp;
                            super.dispose();
                        }
                        else {
                            caution = "Conflicting input";
                        }
                    }
                
                    label.setText(caution);
                }
                else if(e.getActionCommand().equals("delButton")) { // for deleting user-added numbers 
                    if (startergrid[vert_numb][horiz_numb] == 0){
                        JLabel templabel = new JLabel("");
                        gridvals[vert_numb][horiz_numb] = 0;
                        templabel = gridlabels[vert_numb][horiz_numb];
                        templabel.setText("");
                        super.dispose();
                    }
                    else {
                        label.setText("Cannot remove number");
                    }
                }
            }
            
            @Override
            public void addNotify() {
                super.addNotify();
                SwingUtilities.getRootPane(btn).setDefaultButton(btn);
            }
            
        }

        //These check for valid number placements
        public boolean noConflictInput(String str, int ycoord, int xcoord) {
            int str_to_int = Integer.parseInt(str);
            if (!(isinColumn(gridvals, str_to_int, ycoord, xcoord))&&!(isinRow(gridvals, str_to_int, ycoord, xcoord))&&
                    !(isinBox(gridvals, str_to_int, ycoord, xcoord))){
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
        
        public void randomize(){
            for (int i = 0; i <= 8; i++){
                for (int j = 0; j <= 8; j++){
                    solutiongrid[i][j] = 0;
                    gridvals[i][j] = 0;
                }
            }
            int[][] usegrid = randSeqGen(gridvals);
            gridvals = buildPuzzle(usegrid);
            displayPuzzle(gridvals);
        
        }

        // the algorithm behind the random sudoku builder is this:
        // at each square, we check to see if there are possible values that we
        // can put into the square. If there aren't then we remove the previous square, and try a different path
        // each square has to keep track of all the squares that they've tried, so that the algorithm 
        // knows when to move back to the previous square once all possibilities have been tried
        public int[][] randSeqGen(int[][] grid){
            Random randval = new Random();
            Entry[][] triedvals = new Entry[9][9]; //used to check if all possibilities for each square has been checked
                                            //i.e. eliminate all numbers that have been tried
            ArrayList<Integer> temparr = new ArrayList<Integer>();
            int tempval = 0;
            int i = 0;
            int j = 0;
            int tempstoreval = 0;
            //initiliaze the triedvals 2D array
            for (i = 0; i <= 8; i++){
                for (j = 0; j <= 8; j++){
                    triedvals[i][j] = new Entry();
                }
            }
            
            boolean[][] flag = new boolean[9][9]; //used to determine if we have backtracked to this square or not
            for (i = 0; i <= 8; i++){
                for (j = 0; j <= 8; j++){
                    flag[i][j] = false;
                }
            }
            i = 0;
            j = 0;
            
            while (i <= 8){
                while (j <= 8){
                    if (!flag[i][j]){ // is this the first time this square has been visited on this path
                        (triedvals[i][j]).setEntries(getPossNumbs(grid, i, j));
                        flag[i][j] = true;
                    }
                    if (!((triedvals[i][j]).getEntries().isEmpty())){ // if we can place a number in this square
                        temparr = (triedvals[i][j]).getEntries();
                        int size = temparr.size();
                        int index = randval.nextInt(size);
                        tempval = temparr.get(index);
                        grid[i][j] = tempval;
                        (triedvals[i][j]).deleteObj(tempval);
                        j++;
                    }
                    //assumptions: going into the backtracking algorithm, we assume that once we reset the previous square to
                    //0, we are only dealing with the numbers that are left 
                    else { // there are no valid numbers for this square. now we need to backtrack
                        flag[i][j] = false;
                        triedvals[i][j].resetEntries();
                        j--;
                        if (j < 0){
                            j = 8;
                            i--;
                        }
                        // if the current square is out of options, clear the previous square and try a different path
                        tempstoreval = grid[i][j];
                        triedvals[i][j].deleteObj(tempstoreval);
                        grid[i][j] = 0;
                    }
                } 
                i++;
                j = 0;
            }
            for (i = 0; i <= 8; i++){
                for (j = 0; j <= 8; j++){
                    solutiongrid[i][j] = grid[i][j];
                }
            }
            return grid;    
        }
        
        
        // other helper functions
        
        public boolean outOfOptions(int[][] grid, int i, int j){
            int k = 1;
            while (k <= 9){
                if (!((isinBox(grid, k, i, j))||(isinColumn(grid, k, i, j))||(isinRow(grid, k, i, j)))){
                    return false;
                }
                k++;
            }
            return true;
        }
        
        //creates the random puzzle
        //determines which cells to remove from the solution grid
        public int[][] buildPuzzle(int[][] grid){
            Random randval = new Random(); 
            int count; //how many to remove from each row
            int rangeval; //index value
            for (int i = 0; i <= 8; i++){
                ArrayList<Integer> checklist = new ArrayList<Integer>();
                count = randval.nextInt(bp1)+bp2;
                for (int k = 1; k <= count; k++){
                    rangeval = randval.nextInt(9);
                    while (checklist.contains(rangeval)){
                        rangeval = randval.nextInt(9);
                    }
                    checklist.add(rangeval);
                }
                
                for (int j = 0; j <= 8; j++){
                    if (checklist.contains(j)){
                        grid[i][j] = 0;
                    }
                }
            }
            
            for (int i = 0; i <= 8; i++){
                for (int j = 0; j <= 8; j++){
                    startergrid[i][j] = grid[i][j]; 
                }
            }
            return grid;
        }
        
        //print the puzzle after buildpuzzle is finished
        public void displayPuzzle(int[][] grid){
            Color maroon = new Color (170, 0, 0);
            Font fonty = new Font("Serif", Font.PLAIN, fontsize);
            String strlabel = new String();
            for (int i = 0; i <= 8; i++){
                for (int j = 0; j <= 8; j++){
                    if (grid[i][j] == 0){
                        gridlabels[i][j].setText(" ");
                        gridlabels[i][j].setForeground(new Color(0, 0, 0));
                    }
                    else{
                        strlabel = Integer.toString(grid[i][j]);
                        gridlabels[i][j].setFont(fonty);                        
                        gridlabels[i][j].setText(strlabel);
                        gridlabels[i][j].setForeground(maroon);
                    }
                }
            }
        }
        
        //secondary print function
        public void displayPuzzle2(int[][] grid){
            Color powblue = new Color(70,130,195);
            Color cr_green = new Color(34,150,34);  
            Font fonty = new Font("Serif", Font.PLAIN, fontsize);
            String strlabel = new String();
            for (int i = 0; i <= 8; i++){
                for (int j = 0; j <= 8; j++){
                    if (grid[i][j] > 0){
                        if ((startergrid[i][j] == 0)&&(grid[i][j] > 0)){
                            strlabel = Integer.toString(grid[i][j]);
                            gridlabels[i][j].setFont(fonty); 
                            if (colorshift[i][j] == 0){
                                gridlabels[i][j].setForeground(powblue);
                            }
                            else{
                                gridlabels[i][j].setForeground(cr_green);
                            }
                            gridlabels[i][j].setText(strlabel);
                        }
                    }
                }
            }
        }
        
        //print function for the minigrid
        public void displayMinigrid(JLabel[][][] grid){
            for (int i = 0; i < 9; i++){
                for (int j = 0; j < 9; j++){
                    for (int k = 1; k <= 9; k++){
                        if (!(possvals[i][j].isIn(k))){
                            minilabels[i][j][k-1].setText(" ");
                        }
                    }
                    if (gridvals[i][j] > 0){
                        for (int k = 0; k < 9; k++){
                            minilabels[i][j][k].setText(" ");
                        }
                    }
                }
            }
            
        }
        
        public void updateMainPV(){
            Entry[][] temp = game.getPossvals();
            for (int i = 0; i < 9; i++){
                for (int j = 0; j < 9; j++){
                    possvals[i][j].setEntries(temp[i][j].getEntries());
                }
            }
        }
        
        public ArrayList<Integer> getPossNumbs(int[][] grid, int i, int j){
            ArrayList<Integer> listup = new ArrayList<Integer>();
            for (int k = 1; k <= 9; k ++){
                if (!((isinColumn(grid, k, i, j)||isinRow(grid, k, i, j)||isinBox(grid, k, i, j)))){
                    listup.add(k);
                }
            }
            return listup;
        }
        
        public int get_count(int[][] grid){
            int count = 0;
            for (int i = 0; i <= 8; i++){
                for (int j = 0; j <= 8; j++){
                    if (grid[i][j] > 0){
                        count++;
                    }
                }
            }
            return count;
        }
        
        public void restartMini(){
            for (int i = 0; i < 9; i++){
                for (int j = 0; j < 9; j++){
                    for (int k = 0; k < 9; k++){
                        minilabels[i][j][k].setText((k+1)+"");
                    }
                }
            }
        }
        
        public void clearMini(){
            for (int i = 0; i < 9; i++){
                for (int j = 0; j < 9; j++){
                    for (int k = 0; k < 9; k++){
                        minilabels[i][j][k].setText(" ");
                    }
                }
            }
        }
        
        public void print_to_console(){
            System.out.println("gridvals");
            for (int i = 0; i <= 8; i++){
                for (int j = 0; j <= 8; j++){
                    System.out.print(gridvals[i][j]);
                    System.out.print(" ");
                }
                System.out.println("");
            }
            System.out.println("startervals");
            for (int i = 0; i <= 8; i++){
                for (int j = 0; j <= 8; j++){
                    System.out.print(startergrid[i][j]);
                    System.out.print(" ");
                }
                System.out.println("");
            }
        }

    }
