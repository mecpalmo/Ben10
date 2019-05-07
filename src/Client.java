import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Client {

	private static JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	private boolean isConnected = false;
	
	public Client(String host) {
		isConnected = false;
		serverIP = host;
		chatWindow = new JTextArea();
	}
	
	public void StartRunning() {
		try {
			connectToServer();
			setupStreams();
			whileChatting();
		}catch(EOFException eofException) {
			showMessage("\n Client terminated connection");
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}finally {
			closeCrap();
		}
	}
	
	private void connectToServer() throws IOException{
		showMessage("Attempting connection... \n");
		connection = new Socket(InetAddress.getByName(serverIP),Values.PORT);
		showMessage("Connected to:"+connection.getInetAddress().getHostName());
	}
	
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Dude your stream are now good to go! \n");
	}
	
	private void whileChatting() throws IOException{
		isConnected = true;
		do {
			try {
				message = (String)input.readObject();
				//showMessage("\n" + message);
				readMessage(message);
			}catch(ClassNotFoundException classNotfoundException) {
				showMessage("\n I don't know that object type");
			}
		}while(!message.equals("SERVER - END"));
		
	}
	
	private void closeCrap() {
		showMessage("\n closing crap down...");
		try {
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}
	}
	
	public void sendMessage() {
		if(isConnected) {
		String mess = prepareMessage();
		sendMessage(mess);
		}
	}
	
	private void sendMessage(String message) {
		try {
			output.writeObject(message);
			output.flush();
			//showMessage("\nCLIENT - " + message);
		}catch(IOException ioException) {
			chatWindow.append("\n something messed up sending message hoss");
		}
	}
	
	private void showMessage(final String m) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				chatWindow.append(m);
				System.out.println(m);
			}
		});
	}
	
	private String prepareMessage() {
		String temp = "";
		for(int i=0;i<Values.MyFieldsValues.length;i++) {
			temp = temp + intToString(Values.MyFieldsValues[i]);
		}
		temp = temp + intToString(Values.MyDeckSize);
		temp = temp + intToString(Values.MyHandSize);
		temp = temp + intToString(Values.MyLifeIndex);
		temp = temp + intToString(Values.MyTopRejectedValue);
		temp = temp + intToString(Values.MyTransformationTime);
		System.out.println(temp); /////
		return temp;
	}
	
	private String intToString(int k) {
		Integer x = k;
		String a;
		if(x>=0 && x<=9) {
			a = "0"+ x.toString();
		}else {
			a = x.toString();
		}
		return a;
	}
	
	private int StringToInt(String k) {
		int x;
		if(k.charAt(0)=='0') {
			x = Integer.parseInt(k.substring(1, 2));
		}else {
			x = Integer.parseInt(k);
		}
		return x;
	}
	
	private void readMessage(String mess) {
		int len = Values.OpponentsFieldsValues.length;
		for(int i=0;i<len;i++) {
			Values.OpponentsFieldsValues[i] = StringToInt(mess.substring(i*2, i*2+2));
		}
		Values.OpponentsDeckSize = StringToInt(mess.substring(len*2,len*2+2));
		Values.OpponentsHandSize = StringToInt(mess.substring(len*2+2, len*2+4));
		Values.OpponentsLifeIndex = StringToInt(mess.substring(len*2+4, len*2+6));
		Values.OpponentsTopRejectedValue = StringToInt(mess.substring(len*2+6, len*2+8));
		Values.OpponentsTransformationTime = StringToInt(mess.substring(len*2+8, len*2+10));
	}
	
	public static JTextArea getTextArea() {
		return chatWindow;
	}
}
