
import java.applet.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//**Swings don't work in applet??
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ChessApplet extends Applet implements ActionListener
{
	//reference object of class board 
	Board chess_game;

	//the elements of the GUI
	Button newgame = new Button ("Start New Game");
	Button undo = new Button ("Undo move");
	Button rotate=new Button("Rotate View");
	TextArea ta = new TextArea(5,22);
	JOptionPane message = new JOptionPane();
	//ClickListener click=new CickListener();
	//Event object and retrieved string arguments does the function of clicklistener

	//Recognising event..NOT Working Applet AND Swings cannot be mixed??
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==newgame)
			chess_game.newgame();
	}
	
//event recognition via text arguments given to buttons and call the methods
public boolean action (Event evt, Object arg) {
	if ( ((String) arg).equals ("Start New Game"))
	{
		chess_game.newgame ();	
		JOptionPane.showMessageDialog(null, "New Game Started");
	}
	else
		if ( arg.toString().equals ("Rotate View"))
		{
			if(chess_game.rotate==1)
				chess_game.rotate=0;
			else
				chess_game.rotate=1;
			chess_game.repaint();
		}
	else
		if(arg.toString().equals("Undo move")){
			if(chess_game.undo_list.size()>0)
				chess_game.undo_move();
			else
				JOptionPane.showMessageDialog(null, "Moves cannot be further Undone");
		}
	else
		/*Reference the textarea with value that signify
		 * how many positions have to be enlisted.. it can be used somewhere
		 */
		if(arg.toString().equals("TextArea1")){
			
			String str="";
			undo_method temp = new undo_method();
			
			//Take the parameter of how many values are to be inserted
			//int how_many = Integer.parseInt((arg.toString().charAt(arg.toString().length()-1)));
			int how_many = 1;
			System.out.println(how_many);
			int x, y;
			String from, to;
			for(int i=chess_game.undo_list.size()-1, j=how_many ; i>=0 && j>0 ; i--, j--){
				temp = chess_game.undo_list.elementAt(i);
				str = get_color((temp.gs % 100)/10)+"[";
				str += get_piece(temp.gs%10)+"] ";
				
				while(str.length()!=15)
					str += " ";
				x = ((temp.s)-21)%10;
				y = 8 - (((temp.s)-21)/10);
				from = get_col(x)+"" + Integer.toString(y) + " TO ";
				
				x = ((temp.e)-21)%10;
				y = 8 - (((temp.e)-21)/10);
				to = get_col(x) + "" + Integer.toString(y);
				
				str += from+to;
				
				ta.append(str+"\n");
			}
		}
		else
			if(arg.toString().equals("Amend textarea")){
				ta.setText("");
				String str="";
				undo_method temp = new undo_method();
				
				int x, y;
				String from, to;
				for(int i=0; i < chess_game.undo_list.size() ; i++){
					temp = chess_game.undo_list.elementAt(i);
					str = get_color((temp.gs % 100)/10)+"[";
					str += get_piece(temp.gs%10)+"] ";
					
					while(str.length()!=15)
						str += " ";
					x = ((temp.s)-21)%10;
					y = 8 - (((temp.s)-21)/10);
					from = get_col(x)+"" + Integer.toString(y) + " TO ";
					
					x = ((temp.e)-21)%10;
					y = 8 - (((temp.e)-21)/10);
					to = get_col(x) + "" + Integer.toString(y);
					
					str += from+to;
					
					ta.append(str+"\n");
				}
			}
			else
				if(arg.toString().equals("Black Wins")){
					JOptionPane.showMessageDialog(null, "Black Wins");
				}
			else
				if(arg.toString().equals("White Wins")){
					JOptionPane.showMessageDialog(null, "White Wins");
				}
			else
				if(arg.toString().equals("Draw")){
					JOptionPane.showMessageDialog(null, "Game is Draw");
				}
					
	return true;
}
String get_color(int c){
	if(c==1) return "White";
	if(c==2) return "Black";
	return "";
}
String get_col(int c){
	//Depending upon the retrieved x co-ordinate
	switch(c){
	case 0:return "A";
	case 1:return "B";
	case 2:return "C";
	case 3:return "D";
	case 4:return "E";
	case 5:return "F";
	case 6:return "G";
	case 7:return "H";
	}
	return "";
}

String get_piece(int c){
	switch(c){
	case 1:return "Pawn";
	case 2:return "Knight";
	case 3:return "Bishop";
	case 4:return "Rook";
	case 5:return "Queen";
	case 6:return "King";
	}
	return  "";
}

//initialise the applet
public void init() {
	super.init();// call the parent methods of the classes extended and initialise them

	//initialise the board where the computations will take place
	chess_game = new Board (this);
	
	//build GUI
	setBackground (new Color(0xCAD0A3));

	setLayout (new BorderLayout (10,10));// For center and south orientation
	
	ta.setEditable(false);
	ta.setText("");
	
	Panel button_panel=new Panel();// Default FlowLayout is used in Panel
	button_panel.add(newgame);
	button_panel.add(undo);
	button_panel.add(rotate);
	add ("East", ta);
	add ("Center", chess_game);
	add ("South", button_panel);
}

public void change_ta(){
	
}

//make a border with 20/30 pixels width
public Insets insets () {
	return new Insets (20,30,20,30);//Top->Left->Bottom->Right
}

}