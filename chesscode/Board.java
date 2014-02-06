 
import java.applet.Applet;
import java.awt.*;
import java.util.NoSuchElementException;
import java.util.Vector;

public class Board extends java.awt.Canvas
	implements
		java.awt.event.MouseListener, java.awt.event.MouseMotionListener, 
		java.lang.Runnable 
{
	//The internal representation of the chess board
//****************************Initialise global variable*******************************//	
	//..a temporary array where the moves can be implemented and retraced back
	int [] board = new int [120];	
	
	//..a second board for the graphic..
	//..proceedings on the applet would be from graphboard
	//..it is permanent 
	int [] graphboard = new int [120];
	
	//declaring colors
	//Awesome applet at
	//.....**  http://www.december.com/html/spec/colorpicker.html  **.....
	Color light = new Color (0xFEEAB6);//..light coloured square
	Color dark = new Color (0xBD6214);//..dark coloured square
	Color red = new Color (0xCC0000);//..invalid squares
	Color green = new Color (0x009900);//..valid squares
	Color blue = new Color (0x000099);	//..currently clicked square
	
	//the images of pieces
	Image [] pieces = new Image [18];	

	//a reference of the applet which will display the proceedings
	Applet parent;
	
	//variables for Drag&Drop
	int 	code 	= 0,			//forbid access to the movelist				
			start = 21,		//index of the start field
			temp 	= 21,		//did the mouse move to an other field?
			end	= 21,		//index of the end field
			x	= 0,		//x coordinate
			y 	= 0;		//y coordinate
			
	// dimension of each square of the chess board
	int length = 60;
	//Is the view of the board rotated
	int rotate=0;
	
	// Actual co-ordinates where click has been made
	int r_x;
	int r_y;
	
	// Vector to store all the moves that have taken place
	public Vector <undo_method>undo_list = new Vector<undo_method>();
	undo_method item;
	
	public enum castling{no_castling, little_castling, big_castling};
	
	int black_king = 25;
	int white_king = 95;
	
	//variables required for implementing minimax algorithm
	int [] movelist = new int [250];  	//valid move control
	int movecounter = 0;
	int color = 1;				//color of the player that can move..starting with white
	Thread th = null;			//AI thread.. to compute the best possible move
	int deep = 0;				//actual deep to which the search should go..starting with 0
	int target = 4;				//target depth of search
	float value = 0;			//value returned by evaluation function
	float minimax [] = new float [6]; 		//..declare bounds
	float alphabeta [] = new float [6];	//Alpha Beta pruning 
	boolean ababort = false;				//..abort further search with alpha beta
	int move;				//move of the AI
	
	//variables for the evaluation
	
	//..positional value of each point on chess board
	float [] posvalues = 
		{	0.00f,	0.00f, 	0.00f, 	0.00f, 	0.00f, 	0.00f, 	0.00f, 	0.00f, 	0.00f, 	0.00f,
			0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,
							//	1		2		3		4		5		6		7		8
			0.00f,/*8*//*1*/	0.00f,	0.01f,	0.02f,	0.03f,	0.03f,	0.02f,	0.01f,	0.00f,/*8*/	0.00f,
			0.00f,/*7*//*2*/	0.01f,	0.04f,	0.04f,	0.04f,	0.04f,	0.04f,	0.04f,	0.01f,/*7*/	0.00f,
			0.00f,/*6*//*3*/	0.02f,	0.04f,	0.06f,	0.06f,	0.06f,	0.06f,	0.04f,	0.02f,/*6*/	0.00f,
			0.00f,/*5*//*4*/	0.03f,	0.04f,	0.06f,	0.08f,	0.08f,	0.06f,	0.04f,	0.03f,/*5*/	0.00f,
			0.00f,/*4*//*5*/	0.03f,	0.04f,	0.06f,	0.08f,	0.08f,	0.06f,	0.04f,	0.03f,/*4*/	0.00f,
			0.00f,/*3*//*6*/	0.02f,	0.04f,	0.06f,	0.06f,	0.06f,	0.06f,	0.04f,	0.02f,/*3*/	0.00f,
			0.00f,/*2*//*7*/	0.01f,	0.04f,	0.04f,	0.04f,	0.04f,	0.04f,	0.04f,	0.01f,/*2*/	0.00f,
			0.00f,/*1*//*8*/	0.00f,	0.01f,	0.02f,	0.03f,	0.03f,	0.02f,	0.01f,	0.00f,/*1*/	0.00f,
					//			A		B		C		D		E		F		G		H
			0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f, 
			0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f,	0.00f };

//****************************************************************************************//	

	
	
/*************************Constructor to set reference of the applet
						  Load images to the image array
						  Initialise motionlistener methods*****************************/
	
	public Board (Applet ref) {//..Reference of the applet from chesspartner
	
	// super just gives the reference of the parent applet and its methods
	// not writing this will also suffice but since we have our applet in 
	// another class this gives that indication
	super();
	
	//  Initialise alpha beta zero .. 
	//  starting of alpha-beta for evaluation is from 1 onwards
	alphabeta [0] = -3000.0f;
	
	//Method newgame 
	//..Paints the pieces at the respective positions on the chessboard
	//..also initialises board and graphboard
	newgame ();
	
	//load images using mediatracker control
	MediaTracker controler = new MediaTracker (ref);
	//reference of the applet can be mentioned .. if omitted its alright, but its better to
	//know where the images are to be loaded finally
	
	//using this numbering gives the ability to reference the pieces by 
	// 'position value on board'%10
	// 1 pawn
	// 2 knight
	// 3 bishop
	// 4 rook
	// 5 queen
	// 6 king
	// for the white pieces and the black pieces by (10+number)
	pieces [1] = ref.getImage (ref.getCodeBase (), "images/white_pawn.gif");
	pieces [2] = ref.getImage (ref.getCodeBase (), "images/white_knight.gif");
	pieces [3] = ref.getImage (ref.getCodeBase (), "images/white_bishop.gif");
	pieces [4] = ref.getImage (ref.getCodeBase (), "images/white_rook.gif");
	pieces [5] = ref.getImage (ref.getCodeBase (), "images/white_queen.gif");
	pieces [6] = ref.getImage (ref.getCodeBase (), "images/white_king.gif");
	
	pieces [11] = ref.getImage (ref.getCodeBase (), "images/black_pawn.gif");
	pieces [12] = ref.getImage (ref.getCodeBase (), "images/black_knight.gif");
	pieces [13] = ref.getImage (ref.getCodeBase (), "images/black_bishop.gif");
	pieces [14] = ref.getImage (ref.getCodeBase (), "images/black_rook.gif");
	pieces [15] = ref.getImage (ref.getCodeBase (), "images/black_queen.gif");
	pieces [16] = ref.getImage (ref.getCodeBase (), "images/black_king.gif");
	
	// buffer the images
	// addimage(image, id_number for its reference)
	// image can be referenced by id number however we have to wait until all
	// the pieces have been loaded
	
	for (int i = 1; i < 7; i++)
	{
		controler.addImage (pieces [i], 0);
		controler.addImage (pieces [i + 10], 0); 
	}
	
	//..wait until all the pieces have been loaded and then resume 
	// an alternate method used in internet applications is to check the id number of the image
	// if the pixel rate is changing then image is loading or connection error
	// in case picture is still loading appropiate message such as still loading can be issued
	// here it does not carry meaning since after exception is handled 
	// control will not be resumed back
	try {
		controler.waitForAll ();	//wait until the images are loaded	
	} catch (InterruptedException e) { 
		System.out.println ("Images not successfull loaded - Trying again ...");	
		controler.checkID (0, true);			
	}
	
	//set applet reference
	parent = ref;
	
	//events to be implemented 
	// 'this' signifies the motionlistener from the super classes
	addMouseListener (this);
	addMouseMotionListener (this);
}


//**********************************************************************//
	
	
//******************************New Game*******************************//
@SuppressWarnings("deprecation")
public void newgame () 
{
	if (parent != null)
		parent.getAppletContext ().showStatus ("");
	
	//kill AI thread
	if (th != null)
		th.stop ();
	//..Stop the execution of earlier tree searches
	th = null;	
	
	//generate original position
	/* Digit 1: castling is valid
	 * 			if valid 1 else make it 0
	 * Digit 2: color 
	 * 			1- white
	 * 			2- black
	 * Digit 3: piece information
	 * 			1- pawn
	 * 			2- knight
	 * 			3- bishop
	 * 			4- rook
	 * 			5- queen
	 * 			6- king
	 */
	int [] org = {
		99,	99,	99,	99,	99,	99,	99,	99,	99,	99,
		99,	99,	99,	99,	99,	99,	99,	99,	99,	99,
//		Y  X->
//		|	1	2	3	4	5	6	7	8
		99,	124,22,	23,	25,	126,23,	22,	124,99,
		99,	21,	21,	21,	21,	21,	21,	21,	21,	99,
		99,	00,	00,	00,	00,	00,	00,	00,	00,	99,
		99,	00,	00,	00,	00,	00,	00,	00,	00,	99,
		99,	00,	00,	00,	00,	00,	00,	00,	00,	99,
		99,	00,	00,	00,	00,	00,	00,	00,	00,	99,
		99,	11,	11,	11,	11,	11,	11,	11,	11,	99,
		99,	114,12,	13,	15,	116,13,	12,	114,99,
//			1	2	3	4	5	6	7	8
		
		99,	99,	99,	99,	99,	99,	99,	99,	99,	99,
		99,	99,	99,	99,	99,	99,	99,	99,	99,	99 };

	for (int i=0; i < 120; i++) {
		board [i] = org [i];
		graphboard [i] = org [i];
	}	
	
	repaint ();
	
	//Undo list and textarea
	undo_list.clear();
	
	if(parent!=null)
	 parent.action(null, "Amend textarea");
	
	//King positions
	black_king = 25;
	white_king = 95;
	
	//..Conditions for player one(white) to initiate playing
	movecounter = 0;
	color = 1;	
	deep = 0;
	target = 1;
	rotate = 0;
	genmove ();
	code = 0;
}
//*********************************Undo the move*************************************//
void undo_move(){
	
	undo_method temp_item = new undo_method();
	
	// Taking into account that computer is quite fast and has already moved its piece
	// So undoing two moves at a time EXCEPT castling
	//**great idea here yet again.. no need for full details of castling to store
	
	for(int i=2; i>0 ;i--){//Appropriate colour under consideration is the value of 'i'
		try{
		temp_item = undo_list.lastElement();
		}
		catch(NoSuchElementException e){
			parent.getAppletContext ().showStatus ("Moves cannot be further undone");
			return;
		}
			
		// Rebuild back the positions again
		// Amend both graphboard and board and paint them
		undo_list.remove(undo_list.size()-1);
		board[temp_item.s] = temp_item.gs;
		board[temp_item.e] = temp_item.ge;
		graphboard[temp_item.s] = temp_item.gs;
		graphboard[temp_item.e] = temp_item.ge;
		paintField(temp_item.s);
		paintField(temp_item.e);
		
		//The starting and ending positions are that of king alone
		// For both black and white increments for small and big castling are same
		if(temp_item.c.equals(castling.little_castling))
		{
			board[temp_item.s + 3] = 104+(i*10);
			graphboard[temp_item.s + 3] = 104+(i*10);
			board[temp_item.s + 1] = 0;
			graphboard[temp_item.s + 1] = 0;
			paintField(temp_item.s + 3);
			paintField(temp_item.s + 1);
		}
		else 
		if(temp_item.c.equals(castling.big_castling))	
		{
			board[temp_item.s - 4] = 104+(i*10);
			graphboard[temp_item.s - 4] = 104+(i*10);
			board[temp_item.s - 1] = 0;
			graphboard[temp_item.s - 1] = 0;
			paintField(temp_item.s - 4);
			paintField(temp_item.s - 1);
		}
	}
	
	parent.action(null, "Amend textarea");
	// Prepare the variables for move of the human
	movecounter = 1;
	color = 1;
	deep = 0;
	target = 1;
	genmove();
	code=0;
}


//*********************************Mouse events***************************************//
//Implementing the mouse events
// since the interface is implemented all methods must be mentioned
int x_co_ordinate(int x){
	if(rotate==1)
		x=7-x;
	return x;
}
int y_co_ordinate(int y){
	if(rotate==1)
		y=7-y;
	return y;
}
public void mouseEntered(java.awt.event.MouseEvent e) {
}
public void mouseExited(java.awt.event.MouseEvent e) {
}
public void mouseMoved(java.awt.event.MouseEvent e) {
}
public void mouseClicked(java.awt.event.MouseEvent e) {
}

// This event would make the clicked square blue in color to highlight it
public void mousePressed(java.awt.event.MouseEvent e) 
{
	//the matrix is framed in such a way that x and y co-ordinates can be found from index
	//....generalised formula would be
	//  (current index - [starting position here 21])%(row length)  is x co-ordinate
	//  (current index - [starting position here 21])/(column length)  is y co-ordinate
	
	//..if mouse is outside bounds then remain still at the outermost column
	// similarly for row
	
	/*
	 * r_x and r_y are the field the user has clicked upon
	 * x and y are the original field, the fields on the graphboard
	 * x and y are to be considered for display while r_x and r_y for display
	 */
	r_x = e.getX() / length;
	if (r_x < 0)
		r_x = 0;
	if (r_x > 7)
		r_x = 7;
	x = x_co_ordinate(r_x);

	r_y = e.getY() / length;
	if (r_y < 0)
		r_y = 0;
	if (r_y > 7)
		r_y = 7;	
	y = y_co_ordinate(r_y);
	//...........
	
	start = 21 + y*10 + x;
	//Initialise the starting position and a temporary variable
	temp = start;
	end = start;

	//mark start field
	//first fill the rectangle with blue color and since piece would now be hidden 
	//paint the piece again on the same rectangle
	Graphics g = getGraphics ();
	g.setColor (blue);
	g.fillRect (r_x * length, r_y * length, length, length);
	
	//  drawimage takes the arguments as
	//  (image, co-ordinates- x,y, image size- x,y, referenced applet->here ref/parent)
	
	try {
		g.drawImage (pieces [graphboard [start] % 100 - 10], r_x * length, r_y * length, length, length, parent);
	} catch (ArrayIndexOutOfBoundsException ex) {}	
	
	/* great idea here yet again
	 * since the movement of the mouse can be anywhere and our pieces under
	 * consideration on the graphboard are from 1-6 and 11-16
	 * (%100) to eliminate the one reserved for castling
	 * we have either a value from 11-16 or from 21-26
	 * Since the white signify 1 and black 2
	 * thus (-10)would give the appropriate piece number.
	 * 11-16-21-26 can be kept however increasing the array length of images.. hence (-10)
	*/ 
}
public void mouseDragged(java.awt.event.MouseEvent e) {
	//similar to mouse pressed
	// however at equal intervals of time when dragging is taking place
	//??how to check this intervals is still not understood
	r_x = e.getX() / length;
	if (r_x < 0 )
		r_x = 0;
	if (r_x > 7 )
		r_x = 7;
	x = x_co_ordinate(r_x);
	
	r_y = e.getY() / length;
	if (r_y < 0)
		r_y = 0;
	if (r_y > 7 )
		r_y = 7;
	y = y_co_ordinate(r_y);
	
	end = 21 + y * 10 + x;//..current position on which the pointer is stationed
	
	//If change has occurred in the position where it was earlier(temp)
	if ( end != temp)
	{
		//rebuild old field to its prior state 
		//if starting position is found yet again then no change has taken place still
		if	(temp != start)
			paintField (temp);
			
		if ( end != start)
		{	//mark new field
			Graphics g = getGraphics ();
					
			// if entry to movelist is available and the position is present
			// in valid moves list
			if ( (code != 1) && (isvalid (start * 100 + end) ))
				g.setColor (green);
			else
				g.setColor (red);
			
			g.fillRect (r_x * length, r_y * length, length, length);
			
			try {
				g.drawImage (pieces [graphboard [end] % 100 - 10], r_x * length, r_y * length, length, length, parent);
			} catch (ArrayIndexOutOfBoundsException ex) {}	
		}
		
		temp = end;
	}
}

public void mouseReleased(java.awt.event.MouseEvent e) {
	//give the start and end tiles their initial looks .. remove the colours
	paintField (start);	
	paintField (end);
	
	// execute move if it is valid
	// here too access to movelist should be available since
	// it is taken for granted that execute method does so
	if ((code != 1) && (isvalid (start * 100 + end ) ))
		execute (start, end);
}

//**********************************************************************//


//execute a move given starting and ending positions
@SuppressWarnings("deprecation")
public void execute (int start, int end) 
{
	//prepare the object to be inserted in undo_list
	System.out.println(white_king + " " + black_king);
	item = new undo_method();
	item.s = start;
	item.gs = board[start];
	item.e = end;
	item.ge = board[end];
	item.c = castling.no_castling;
	
	//............ validity is already established by movelist[] ..............//
	// Donot enable castling when the pieces have moved initially
	if(board[start]%10==6 || board[start]%10==4)
	{
		if(board[start]%10 == 6){
			// Amend positions of kings so they are no necessary to be searched 
			// at every point during is_check method
			switch((board[start]%100) / 10){// Color of the king under consideration
			case 1:
				white_king = end;
				break;
			case 2:
				black_king = end;
			}
		}
		board [end] = board [start]%100;
	}
	else
		board[end] = board[start];
	
	
	board [start] = 0;
	//..........................//
	
	//Is the move to do- 'castling'
	if (board [end] % 10 == 6)
	{
		if( end == start + 2)
		{	//little castling
			//Once the castling is done it cannot be performed again
			//so the last two digits of the rook are noted down in board and graphboard
			
			item.c = castling.little_castling; // Record that little castling was performed
			board [start + 1] = board [start + 3] % 100;// no castling hence forth
			board [start + 3] = 0;
		
			graphboard [start + 1] = board [start + 1];
			graphboard [start + 3] = 0;
				
			paintField (start + 3);
			paintField (start + 1);
		}
		if( end == start - 2)
		{	//big castling
			item.c = castling.big_castling; //Keep a record of big castling
			board [start - 1] = board [start - 4] % 100;// no castling henceforth
			board [start - 4] = 0;
		
			graphboard [start - 1] = board [start - 1];
			graphboard [start - 4] = 0;
				
			paintField (start - 4);
			paintField (start - 1);			
		}
	}
	
	//change pawn to queen if it reaches the opposite file
	
	if ( (board [end] % 10 == 1) && ((end < 29) || (end > 90)) )
		board [end] += 4;//Make it queen
	
		//..Amend the array to be displayed..//
	graphboard [start] = board [start];
	graphboard [end] = board [end];
		//..................................//
	
	
	// Keeping reference of the moves in undo_list
	undo_list.add(item);
	
	//Print the details in textarea
	if(undo_list.size()==1)
		parent.action(null,"Amend textarea"); else
		parent.action(null, "TextArea1");
	
	//....Display the pieces...//
	paintField (end);
	paintField (start);
	//.........................//
	
	//change player
	if (color == 1)
	{
		color = 2;
		//..Look for the Best move using the minimax
		th = new Thread (this);
		th.setPriority (10);
		th.start ();

	} else {
		color = 1;
	
		//calculate valid moves by using the target initialised to 1
		movecounter = 0;
		deep = 0;
		target = 1;
		genmove ();
		
		if (movecounter == 0)	
								//no valid moves for white pieces left -> end of game
		{
			if (ischeck ())		//checkmate for white
			{
				parent.getAppletContext ().showStatus ("Black wins!");
				parent.action(null, "Black Wins");
			}
				
			else				//match is draw
			{
				// this is not perfectly right since a set of moves repeated
				// more than four times result in draw	
				parent.getAppletContext ().showStatus ("Game is a draw!");
				parent.action(null, "Draw");
				
			/**** Last four moves in undo list are same then declare draw*****/
				// keep a queue of all moves and if the values 
				// of last two alternate nodes  repeat then declare draw
			}
				
		}
	}
}
/************ Thread of computer with high priority to 
			  not interfere with user interface **************/
//AI thread
public void run() {
	//no access to the movelist
	code = 1;

	deep = 0;
	target = 4;
						
	//look for best move
	movecounter = 0;
	genmove ();			

	if (movecounter == 0)	//no moves -> end of game
	{
		if (ischeck () ){
			parent.getAppletContext ().showStatus ("White Wins!");
			parent.action(null, "White Wins");
		}
		else{
			parent.getAppletContext ().showStatus ("Game is a draw!");
			parent.action(null, "Draw");
		}
			
		return;	
	} 
	//execute move
	/* move is a global with first two digits signifying the starting index
	   and the last two the end index.
	   It is initialised after recursing through four levels
	   and for the optimum value of 'value' in the comparison of
	   minimax[deep] deep->1 section of simulize move
	*/   
	execute ( move / 100, move % 100 );
	//..Execute the best move of the computer
	
	// give access to the movelist while calculating isvalid of human interface
	// which is blocked when computer is processing its moves
	//*****************************//
	code = 0;	
	//*****************************//
}
// checks if a human move is valid
// computer makes its moves taking the values directly from movelist
public boolean isvalid (int move) 
{
	for (int i = 0; i < movecounter; i++) {
		if (movelist [i] == move)
			return true;		
	}	
	return false;
}



//********************Paint methods to draw images on board*********************//

//paint chessboard
public void paint (Graphics g) {
	// All cells of the board
	for ( int i = 21; i < 99; i++)
	{
		paintField (i);		
		if ( i%10 == 8)
			i += 2;
	}
	
	int x_length;
	int y_length;
	g.setColor(new Color(0x620000));
	g.setFont(new Font("Calibri", Font.BOLD, 16));
	//columns
	x_length = (length/2)-2;
	y_length = (length*8)+17;
	for(char i='A'; i<='H'; i++){
		g.drawString(Character.toString(i), x_length, y_length);
		x_length += length;
	}
	
	//rows
	x_length = (length*8)+8;
	y_length = (length/2);
	for( int i=8; i>=1; i--){
		g.drawString(Integer.toString(i), x_length, y_length);
		y_length += length;
	}
}

//paint single cell of the chessboard 
public void paintField (int index) 
{
	//load graphic reference
	Graphics g = getGraphics ();
	
	//calculate x and y by formula 
	int x = (index - 21) % 10;
	int y = (index - 21) / 10;
	
	//Here the symmetrically opposite field is considered
	if(rotate==1){
		x=7-x;
		y=7-y;
	}
	
	//paint the alternating cells of the board..starting from 21
	if ( (x + y) % 2 == 0)//where the black piece rook would start 
		g.setColor( dark );//dark coloured square
	else
		g.setColor( light );//light coloured square

	//colouring symmetrically opposite field
	g.fillRect ( x * length, y * length, length, length);
	
	//paint piece
	//However the piece to be inserted in that place belongs to the index
	//Hence that is to be left untouched
	try {
		g.drawImage (pieces [graphboard [index] % 100 - 10], x * length, y * length, length, length, parent);
	} catch (ArrayIndexOutOfBoundsException e) {}	
}
//***************************************************************************//


//evaluate the current position with respect to color
public float evaluation ( ) {
	float value = 0;
	float piecevalue = 0;
	
	for (int i = 21; i < 99; i++)
	{
		if ( board [i] != 0 )
		{	
			//get which piece
			switch (board [i] % 10)
			{
				case 1://pawn
					piecevalue = 1.0f;
					break;
				case 2://knight
					piecevalue = 2.0f;
					break;
				case 3://bishop
					piecevalue = 3.0f;
					break;
				case 4://rook
					piecevalue = 4.5f;
					break;
				case 5://queen
					piecevalue = 8.0f;
					break;
				case 6://king.. least importance in attack and ischeck method protects it
					piecevalue = 0.5f;
			}
			
			//positional strength is the piece strength + position value
			piecevalue += posvalues [i];
			
			if ( board [i] % 100  / 10 == color)
				value += piecevalue;	// color that is playing
			else
				value -= piecevalue;    // opposite players piece
		}	
		
		if ( i%10 == 8)//..jump to the next row
			i += 2;
	}
	return value;	
	// Thus greater the value greater would be the positional strength of the player
}

//******************************IMPORTANT*****************************************//
//************************Generate valid moves**********************************// 
//generates valid moves
public void genmove () {
	deep++;
	ababort = false;
	//alpha-beta zero is initialised to -infinity

	/* The current player has odd depth(MAXIMIZING EVEN)
	 * since deep is always initialised to zero in both cases
	 * and hence its minmax and alphabeta has to be +infinity
	 * The opposite player has to initialise it to -Infinity
	 * Value after evluation cannot exceed 156.8..so 5000.0f is enough
	*/
	if (deep % 2 != 0)		
	{	//currently playing
		minimax [deep] = 5000.0f;
		alphabeta [deep] = 5000.0f;
	} else {
		//opposite player
		minimax [deep] = -5000.0f;
		alphabeta [deep] = -5000.0f;
	}	
	
	//check for all possible moves by all the pieces of the same colour on the board
	for (int i = 21; i < 99; i++)	
	{
		if (board [i] % 100 / 10 == color)	//check colour of current player
		{
			switch (board [i] % 10) 
			{
				case 1:	//pawn	
					if (color == 1)	//white pawn will move in decreasing index
					{
						if (board [i-10] == 0)//front
							simulize ( i, i-10);										
						if (board [i- 9] % 100 / 10 == 2)//right capture of opposite piece
							simulize ( i, i-9 );								
						if (board [i-11] % 100 / 10 == 2)//left capture of opposite piece
							simulize ( i, i-11);								
						//two places advance from start provided nothing in between
						if ( (i>80) && ( ( board [i-10] == 0) && (board [i-20] == 0))) 
							simulize ( i, i-20);													
					} else {	//black pawn will move in increasing index
						if (board [i+10] == 0)//front
							simulize ( i, i+10);								
						if (board [i+9] % 100 / 10 == 1)//right capture of opposite piece
							simulize (i, i+9);
						if (board [i+11] % 100 / 10 == 1)//left capture of opposite piece
							simulize (i, i+11);								
						//two places advance from start
						if ( (i<39) && ( (board [i+10] == 0) && (board [i+20] == 0)))
							simulize (i, i+20);								
						/* en passant capturing..not implemented yet
						   keep a record of whether a pawn has advanced 2 places
						   like 1 for castling 2 for en passant
						   */
					}					
					break;
				case 2:	//knight	
					/*        (-21)  (-19)
					 *   (-12)			  (-8)	
					 *   			K
					 *   (+8)			  (+12)
					 *		  (+19)  (+21)	
					 */
					simulize (i, i+12);							
					simulize (i, i-12);							
					simulize (i, i+21);							
					simulize (i, i-21);							
					simulize (i, i+19);							
					simulize (i, i-19);						
					simulize (i, i+8 );					
					simulize (i, i-8 );					
					break;
				case 3:	//bishop
					//keep on decrementing the factor at each stage
					/*	(-11) (-9)
					 * 		 B
					 *  (+9)  (+11)
					 */
					multisimulize ( i,  -9);
					multisimulize ( i, -11);
					multisimulize ( i,  +9);
					multisimulize ( i, +11);
					break;
				case 4:	//rook
					/*		 (-10)
					 *    (-1) R (+1)
					 * 		 (+10)
					 */
					multisimulize (i, -10);
				 	multisimulize (i, +10);
					multisimulize (i,  -1);
					multisimulize (i,  +1);
					break;
				case 5:	//queen
					//in is check method this two pieces can be combined if attacking
					//like bishop
					multisimulize ( i,  -9);
					multisimulize ( i, -11);
					multisimulize ( i,  +9);
					multisimulize ( i, +11);
					
					//like rook
					multisimulize (i, -10);
				 	multisimulize (i, +10);
					multisimulize (i,  -1);
					multisimulize (i,  +1);
					break;
				case 6:	//king
					/*
					 * Rule of castling.. 
					 * The king should not have check at the original or the position
					 * adjacent to to it whether left or right depending whether it is 
					 * small or big castling.
					 * The pieces namely king or rook should not be moved
					 * That is taken care of in execute method
					 */
					//castling for king provided no check is incident upon king
					if ((board [i] / 100 == 1) && (! ischeck ()))
					{		
						/* little castling provided castling with rook is valid
						 * Since castling is only with rook,
						 * the rook involved in castling is given third digit as one
						 * Thus castling once performed cannot be done again
						 */
						if (((board [i+1] == 0) && (board [i+2] == 0)) && (board [i+3] / 100 == 1))
						{					
							//King is not in check when moved a piece right
							board [i+1] = board [i] % 100;
							board [i] = 0;
							//..king is not in check when castled
							if (! ischeck ())
							{
								//Take back the king and move the rook to left 2 positions
								board [i] = board [i+1];
																										
								//move rook
								board [i + 1] = board [i + 3] % 100;
								board [i + 3] = 0;
								
								//..simulate castling
								simulize (i, i+2);
								
								//take the rook back to original position and rebuild its position
								board [i + 3] = board [i + 1] + 100;
								//The loop maynot be entered and hence keep the (i+1) position intact
								board [i+1] = board [i];
							}
							
							//rebuild the ith position from (i+1) position
							board [i] = board [i + 1] + 100;
							board [i + 1] = 0;				
						}
						
						if (((board [i-1] == 0) && (board [i-2] == 0)) && ((board [i-3] == 0) && (board [i-4] / 100 == 1)))
						{	//big castling
							//King is not in check when moved a piece left
							board [i-1] = board [i] % 100;
							board [i] = 0;
													
							if (! ischeck ())
							{
								//take the king back to original position
								board [i] = board [i-1];
																										
								//move rook 3 places to the right
								board [i - 1] = board [i - 4] % 100;
								board [i - 4] = 0;
								
								//Simulate castling for the king where ischeck would be processed again
								simulize (i, i-2);
								
								//take the rook back to original position and rebuild its position
								board [i - 4] = board [i - 1] + 100;
								//The loop may not be entered and hence rebuild the (i-1) position
								board [i - 1] = board [i];
							}
							
							//rebuild the ith position from (i-1) position
							board [i] = board [i - 1] + 100;
							board [i - 1] = 0;									
						}
					}				
																
					simulize (i, i+1); 
					simulize (i, i-1);
					simulize (i, i+10);
					simulize (i, i-10);
					simulize (i, i+9);
					simulize (i, i-9);
					simulize (i, i+11);
					simulize (i, i-11);	
			}
		}
		
		if ( i%10 == 8)
			i += 2;
	}
	
	deep--;
	ababort = false;
}

//simulation for queen, rook and bishop
public void multisimulize (int start, int inc) {
	int to = start;
	
	while ((board [to + inc ] != 99) && (board [to + inc] % 100 / 10  != color))
	{
		to += inc;
		
		if (board [to] != 0)
		{
			simulize (start, to);
			return;
		}
		simulize (start, to);
	}
	simulize (start, to);
}
//prepare AI for a new game



//here we simulize the move
public void simulize (int start, int end) {
	if ((board [end] == 99) || (board [end] % 100 / 10 == color))
		return;
		
	if (ababort)	//alpha beta
		return;			
		
	//simulate the move and check for check to king
	int orgstart = board [start];
	int orgend = board [end];
		
	board [end] = board [start];
	
	//King might be moved during simulation..keep the record
	//??Not working in is_check() function
	
	if((board[start] % 10) == 6){// If it is king
		switch((board[start] % 100) / 10){//Depending upon color
		case 1:
			white_king = end;
			break;
		case 2:
			black_king = end;
		}
	}
	board [start] = 0;
	//..........................//
	
	//change pawn to king when it reaches the opposite row
	if ((board [end] % 10 == 1) && ((end < 29) || (end > 90)))
		board [end] += 4;
		
	/* Enter the valid move in movelist[] array if deep equals 1 ie current players move
	 * If target equals deep then compute value .. end of recursion
	 * Else the function is to be called again with opposite players side
	 * Depth zero is to be maximised always hence its alpha-beta is -infinity
	 */
	/*
	 * Depth 
	 * 0 MAX .. alphabeta initialised in newgame()
	 * 1 MIN    minimax and alphabeta are initialised to opposite infinities
	 * 2 MAX
	 * 3 MIN
	 * 4 MAX..so on
	 */
	
	/*IMPORTANT: when the tree establishes for the first time the entire depth
	 * is travelled once and all alpha-beta and minimax are initiaised to the some value
	 * these values are then compared with when further depth first traversals are undertaken
	 */	

	if (! ischeck ())
	{
		if (deep == 1) {// Record the move
			movelist [movecounter] = start * 100 + end;
			movecounter++;			
		}		
	
		//calculate value of this node
		if (target == deep)// termination of recursion
			 value = evaluation ();
		else {
			// Change color
			if (color == 1)
				color = 2; 
			else
				color = 1;
			//............//
			
			// Recurse to the next level for noting the
			// best move from opposite side and note down its value
			genmove ();
			value = minimax [deep + 1];
			
			
			
//********Alpha-Beta breadth wise type**********//			
			//..Value is of one level higher
			//..Comparison with its previous level
			
			if (deep % 2 != 0)// MINimised
			{	
				if (value < alphabeta [deep])
					alphabeta [deep] = value;				
			} else {// MAXimised 
				
				if (value > alphabeta [deep])
					alphabeta [deep] = value;
			}		
								
			// Back to original coloured piece 
			if (color == 1)
				color = 2; 
			else
				color = 1;
//**********************************************//			
			
		}
		
    //..If no recursion..end of recursion..//
//***********Minimax Depth wise type***********//
		if (deep % 2 != 0) // MINimised
		{	
			
			if (value <= minimax [deep] )
			{
				minimax [deep] = value;
				
				/* Initialise the best move at this point
				 * since all levels are travelled and best values are retained
				 */
				if (deep == 1)
					move = start * 100 + end;
			}
			/* alphabeta pruning with aid of one level less
			 * Since the level one less has opposite characteristics
			 * it is wise to discontinue the further search over here
			 */
			if (value < alphabeta [deep - 1])
				ababort = true;	
			
		} else { // MAXimised
			
			if (value > minimax [deep] )
				minimax [deep] = value;
			
			// alphabeta pruning
			if (value > alphabeta [deep - 1])
				ababort = true;

		}	
	}
	//........not check.........//	
	
	//undo move
	// ....Checking for check and evaluating positional strength is done at this point........
	board [start] = orgstart;
	if((orgstart % 10) == 6){// If it is king
		switch((orgstart % 100) / 10){//Depending upon color
		case 1:
			white_king = start;
			break;
		case 2:
			black_king = start;
		}
	}
	board [end] = orgend;
}
//*************************************************************************//



//************************************************************************//
//is king in check..??
public boolean ischeck () {
	int king = 0;
	
	//search kings position
	
	for ( int i = 21; i < 99; i++)
	{
		if ((board [i] % 100 / 10 == color) && (board [i] % 10 == 6))
		{
			king = i;
			break;
		}	
					
		if ( i % 10 == 8)
			i += 2;
	}
	/*
	if(color == 1)
		king = white_king;
	else
		king = black_king;
	*/
	//System.out.println(color + " " + king + " " + white_king + " " + black_king);
	
	//check by knight
	// all eight positions and piece of opposite color
	if ((board [king-21] % 10 == 2) && (board [king-21] % 100 / 10 != color))
		return true;
 	if ((board [king+21] % 10 == 2) && (board [king+21] % 100 / 10 != color))
		return true;
	if ((board [king-19] % 10 == 2) && (board [king-19] % 100 / 10 != color))
		return true; 
	if ((board [king+19] % 10 == 2) && (board [king+19] % 100 / 10 != color))
		return true;
	if ((board [king- 8] % 10 == 2) && (board [king- 8] % 100 / 10 != color))
		return true;
	if ((board [king+ 8] % 10 == 2) && (board [king+ 8] % 100 / 10 != color))
		return true;
	if ((board [king-12] % 10 == 2) && (board [king-12] % 100 / 10 != color))
		return true;
	if ((board [king+12] % 10 == 2) && (board [king+12] % 100 / 10 != color))
		return true;		 
 
   	//Check by bishop or queen
	int j = king;
	while (board [j - 9] != 99)// north-east until end of board
	{		
		j -= 9;
		if (board [j] % 100 / 10 == color)// same coloured piece is encountered
			break;
		if (!(board [j] == 0))
		{
			if ((board [j] % 10  == 3) || (board [j] % 10  == 5))//bishop or queen
				return true;
			else
				break;
			// since there is another piece that has no valid move along that line
			// or it may have blocked another potentially threating piece
		}
	
	}
					
	j = king;
	while (board [j+9] != 99)//south-east until end of board
	{
		j += 9;
		if (board [j] % 100 / 10 == color)
			break;
		if (!(board [j] == 0))
		{
			if ((board [j] % 10 == 3) || (board [j] % 10 == 5))
				return true;
			else
				break;
		}
	}
	
	j = king;
	while (board [j-11] != 99)//North-west until end of board
	{
		j -= 11;
		if (board [j] % 100 / 10 == color)
			break;
		if (!(board [j] == 0))
		{
			if ( (board [j] % 10 == 3) || (board [j] % 10 == 5))
				return true;
			else
				break;
		}	
	}
	
	j = king;
	while (board [j+11] != 99)//North-east until end of board
	{
		j +=11;
		if (board [j] % 100 / 10 == color)
			break;
		if (!(board [j] == 0))
		{
			if ( (board [j] % 10 == 3) || (board [j] % 10 == 5))
				return true;
			else
				break;
		}
	}  
	
	//check by rook or queen
	j = king;
	while (board [j-10] != 99)//Upwards
	{
		j -= 10;
		if (board [j] % 100 / 10 == color)
			break;
		if (!(board [j] == 0))
		{
			if ((board [j] % 10 == 4) || (board [j] % 10 == 5))
				return true;
			else
				break;
		}
		
	}
	
	j = king;
	while (board [j+10] != 99)//Downwards
	{
		j += 10;
		if (board [j] % 100 / 10 == color)
			break;
		if (!(board [j] == 0))
		{
			if ((board [j] % 10 == 4) || (board [j] % 10 == 5))
				return true;
			else
				break;
		}
			
	}
	
	j = king;
	while (board [j-1] != 99)//Leftwards
	{
		j -=1;
		if (board [j] % 100 / 10 == color)
			break;
		if (!(board [j] == 0))
		{
			if ((board [j] % 10 == 4) || (board [j] % 10 == 5))
				return true;
			else
				break;		
		}
						
	}
	
	j = king;
	while (board [j+1] != 99)//rightwards
	{
		j +=1;
		if (board [j] % 100 / 10 == color)
			break;
		if (!(board [j] == 0))
		{
			if ((board [j] % 10 == 4) || (board [j] % 10 == 5))
				return true;
			else
				break;
		}
		
	}
	
	//Threat by pawn
	//En-passant capturing is NOT implemented
	if (color == 1)// white pawn travels in decreasing index
	{
		if ((board [king-11] % 10 == 1) && (board [king-11] % 100 / 10 == 2))
			return true;
		if ((board [king- 9] % 10 == 1) && (board [king- 9] % 100 / 10 == 2))
			return true;	
	} else {// black travels in increasing index
		
		if ((board [king+11] % 10 == 1) && (board [king+11] % 100 / 10 == 1))
			return true;
		if ((board [king+ 9] % 10 == 1) && (board [king+ 9] % 100 / 10 == 1)) 
			return true;
	}
	
	//threat by another's king
	if ( board [king+ 1] % 10 == 6 )  
		return true;
	if ( board [king- 1] % 10 == 6 )   
		return true;
	if ( board [king+10] % 10 == 6 )   
		return true;
	if ( board [king-10] % 10 == 6 )   
		return true;
	if ( board [king+11] % 10 == 6 )   
		return true;
	if ( board [king-11] % 10 == 6 )   
		return true;
	if ( board [king+ 9] % 10 == 6 )   
		return true;
	if ( board [king- 9] % 10 == 6 )   
		return true;

	return false;
}
//***********************************************************************//


//YYYYYYYYYEEEEEEEEEEEEEEESSSSSSSSSSSSSSSSSSSSS FINALLY ITS DONE
}