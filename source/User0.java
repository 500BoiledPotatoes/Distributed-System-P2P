import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.List;


class Message implements Serializable {
    //The format of information used to pass between cs and p2p
    public volatile String senderID;
    //Sender ID
    public volatile String msgText;
    //Message content
    public volatile String kickID;
    //The id of the person who got kicked off
    public volatile String[] count ;
    //Store KickID
    public volatile HashMap<String, Integer> clientPort = new HashMap<>();
    //Port for accessing the client
    public volatile IdentityHashMap<String, String> clientCommand = new IdentityHashMap<>();
    //The command used by each client

}

/***
 * User0
 */


//To realize the p2p function, each client is both a client and a server.
// In this way, the connection between clients can be realized by knowing the ip and port number of each client
class client extends JFrame implements ActionListener, Runnable {
    Socket socket = null;
    //Client socket
    Socket chatSocket = null;
    //P2P socket
    ServerSocket serverSocket;
    //Server on client side
    JLabel name, message, status, receivedMessage, IP, Port, privateMessage, commandline, hint;
    //Client label
    JTextField inputName, inputMessage, inputIP, inputPort;
    //Input box
    TextArea messageArea, command, privateChat;
    //Textbox
	HashMap<String, Integer> clientPort = new HashMap<>();
    //Client ports
    IdentityHashMap<String, String> commands = new IdentityHashMap<>();
    //Client commands
    List<String> IDs= new ArrayList<>();
    //ID of all access clients
    Message msg = new Message();
    //Passed message
    InetAddress host;
    //IP
    int port = 8000;
    //Entered port
    Thread user0 = null;
    //Client thread
    JButton send, connect, disconnect;
    //Buttons
	data record = new data();
    Socket count[] = new Socket[5];
    //Storage clients that are added
    int cj = 5;
    int cl = 0;
    client(String s) {
        super(s);

        myadapter a = new myadapter(this);
        addWindowListener(a);

        IP = new JLabel("Enter IP : ");
        add(IP);
        inputIP = new JTextField(15);
        add(inputIP);
        inputIP.setText("127.0.0.1");
        //IP

        add(new JLabel("            "));

        Port = new JLabel("Enter Port : ");
        add(Port);
        inputPort = new JTextField(15);
        add(inputPort);
        inputPort.setText(String.valueOf(port));
        add(new JLabel(""));
        //Port

        add(new JLabel("      "));

        name = new JLabel("Nick Name :  ");
        add(name);
        inputName = new JTextField(15);
        add(inputName);
        //Client ID

        connect = new JButton("Connect");
        add(connect);
        connect.addActionListener(this);
        //Connect server button

        disconnect = new JButton("Disconnect");
        add(disconnect);
        disconnect.addActionListener(this);
        disconnect.setEnabled(false);
        //Disconnect server button

        add(new JLabel("                                        "));

        message = new JLabel("Message : ");
        add(message);
        inputMessage = new JTextField(34);
        add(inputMessage);
        inputMessage.setEditable(false);
        //Input message text box

        send = new JButton("Send Message");
        add(send);
        send.addActionListener(this);
        send.setEnabled(false);
        add(new JLabel("      "));
        //Send button

        status = new JLabel("Status : ");
        add(status);
        hint = new JLabel("Not connected to the server...");
        add(hint);
        //Client status

        add(new JLabel("                                                                            "));

        receivedMessage = new JLabel("Recieved Messages : ");
        add(receivedMessage);
        messageArea = new TextArea("", 15, 80);
        add(messageArea);
        messageArea.setFont(Font.getFont("verdana"));
        messageArea.setBackground(Color.ORANGE);
        messageArea.setEditable(false);
        //Open chat room

		privateMessage = new JLabel("Recieved Private Messages : ");
		add(privateMessage);
		privateChat = new TextArea("", 15, 80);
		add(privateChat);
		privateChat.setFont(Font.getFont("verdana"));
		privateChat.setBackground(Color.gray);
		privateChat.setEditable(false);
        //Private chat room

        commandline = new JLabel("Command Line : ");
        add(commandline);
        command = new TextArea("", 15, 80);
        add(command);
        command.setFont(Font.getFont("verdana"));
        command.append("\n" + "Please input commend:");
        command.setBackground(Color.white);
        command.setEditable(false);
        command.requestFocus();
        //Command input box

        hint.setText("Not connected to Server, click connect");
    }

    public void actionPerformed(ActionEvent ae) {
        //This method is used to bind events to the button
        try {
            String str = ae.getActionCommand();

            if (str.equals("Disconnect")) {
                try {
                    send.setEnabled(false);
                    inputMessage.setEditable(false);
                    connect.setEnabled(true);
                    disconnect.setEnabled(false);
                    inputIP.setEditable(true);
                    inputPort.setEditable(true);
                    inputName.setEditable(true);
                    //Set the status of the page that is disconnected from the server
                    socket.close();
                    socket = null;
                    //Close the Client
                } catch (Exception e) {
                }
            }
            if (str.equals("Send Message")) {
                msg.senderID = inputName.getText();
                //Get sender ID
                msg.msgText = inputMessage.getText();
                //Get message content
                inputMessage.setText("");
                if (!msg.senderID.equals("") && !msg.msgText.equals("")) {
                    sendData();
                    //Send message
                } else
                    hint.setText("Message was not sent, type a message");
                    //Display sending failure
            }
            if (str.equals("Connect")) {
                //Connect event
                try {
                    host = InetAddress.getByName(inputIP.getText());
                    //Get host IP
                    String p = inputPort.getText();
                    //Get connection port
                    try {
                        if (socket != null) {
                            socket.close();
                            socket = null;
                            //If the client is not closed, close it first
                        }
                    } catch (Exception e) {
                    }
                    if (!inputName.getText().equals("")) {
                        //The id cannot be empty
                        socket = new Socket(host, Integer.parseInt(p));
                        //Initialize client socket
                        ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());
                        msg.senderID = inputName.getText();
                        msg.msgText = " is online at " + new Date().toString();
                        msg.clientPort.put(inputName.getText(), 2000);
                        obj.writeObject(msg);
                        //Send an online notification to the server and other clients
                        inputMessage.setEditable(true);
                        send.setEnabled(true);
                        connect.setEnabled(false);
                        disconnect.setEnabled(true);
                        inputIP.setEditable(false);
                        inputPort.setEditable(false);
                        inputName.setEditable(false);
                        command.setEditable(true);
                        command.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyTyped(KeyEvent e) {
                                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                                    checkCommand();
                                }
                            }
                        });
                        //Set the state in the window
                        hint.setText("Connection established with Server, start chatting");
                        //Display the client status
                        user0 = new Thread(this, "Reading");
                        user0.start();
                        //Start client thread
                    }


                } catch (Exception e) {
                    hint.setText("Could not connect to Server, connect again");
                }
            }
        } catch (Exception e) {
            hint.setText("Action Error");
        }
    }

    public void sendData() {
        //Send message method
        try {
            ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());
            //Output information stream
            if (!msg.senderID.equals("") && !msg.msgText.equals("")) {
                //The sender id and message content cannot be empty
                obj.writeObject(msg);
                hint.setText("Message was sent successfully");
            }
            msg.senderID = "";
            msg.msgText = "";
            msg.kickID = "";
            msg.clientCommand.clear();
        } catch (Exception e) {
            hint.setText("Error occured while sending message");
        }
    }
    public void sendQueryData() {
        //Sends a request to query the commands used by the client
        try {
            ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());
            if (!msg.clientCommand.isEmpty()){
                obj.writeObject(msg);
                hint.setText("Query successfully");
            }
            msg.senderID = "";
            msg.msgText = "";
            msg.kickID = "";
            msg.clientCommand.clear();
        } catch (Exception e) {
            hint.setText("Error occured while querying");
        }
    }
    public void sendKickData() {
        //Send the request to kick
        try {
            ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());
            obj.writeObject(msg);
            msg.senderID = "";
            msg.msgText = "";
            msg.kickID = "";
            msg.clientCommand.clear();
        } catch (Exception e) {
            hint.setText("Error occured while sending message");
        }
    }
    public void sendPrivateData() {
        //Send private message method
        try {
            ObjectOutputStream obj = new ObjectOutputStream(chatSocket.getOutputStream());
            if (!msg.senderID.equals("") && !msg.msgText.equals("")) {
                obj.writeObject(msg);
                hint.setText("Message was sent successfully");
            }
            msg.senderID = "";
            msg.msgText = "";
            msg.kickID = "";
            msg.clientCommand.clear();
        } catch (Exception e) {
            hint.setText("Error occured while sending message");
        }
    }

    public void checkCommand() {
        //Implement command line manipulation
        String[] lines = command.getText().split("\n");
        String input = lines[lines.length - 1];
        String[] after = input.split("_|\\{|\\}|:");
        String[] com = input.split(":");

        List<String> result = new ArrayList<>();
        for (int i = 0; i < after.length; i++) {
            if (!after[i].equals("") && !after[i].equals(" ") && !after[i].equals("Please input commend") && !after[i].equals("Error") && !after[i].equals("commend") && !after[i].equals("\n")) {
                result.add(after[i].trim());
             }
        }
        //Process the input command
        //Read the required information
        try{
            //Execute specific methods based on different commands
            if (result.isEmpty()) {
                command.append("\n" + "Error");
                command.append("\n" + "Please input commend:");
                //No command entered
            }
            else if (result.get(0).equals("BROADCAST")) {
                //Send public message
                //Same as the send message event
                msg.senderID = inputName.getText();
                msg.msgText = result.get(1);
                msg.clientCommand.put(inputName.getText(), com[1]);
                //Record the commands used
                if (!msg.senderID.equals("") && !msg.msgText.equals("")) {
                    sendData();
                } else
                    hint.setText("Message was not sent, type a message");
                command.append("\n" + "Please input commend:");
            } else if (result.get(0).equals("STOP")) {
                //Disconnect from the server
                //Same as the disconnect event
                msg.senderID = inputName.getText();
                msg.clientCommand.put(inputName.getText(), com[1]);
                //Record the commands used
                sendQueryData();
                try {
                    send.setEnabled(false);
                    inputMessage.setEditable(false);
                    connect.setEnabled(true);
                    disconnect.setEnabled(false);
                    inputIP.setEditable(true);
                    inputPort.setEditable(true);
                    inputName.setEditable(true);
                    socket.close();
                    socket = null;
                } catch (Exception e) {
                }
                command.append("\n" + "Please input commend:");
            }
            else if (result.get(0).equals("MESSAGE") && !result.get(1).equals("ID")) {
                //Send private messages
                msg.clientCommand.put(inputName.getText(), com[1]);
                //Record the commands used
                try {
                    int port = clientPort.get(result.get(1));
                    //Locate the client based on its id
                    chatSocket = new Socket(host, port);
                    //Connect to the Client
                    msg.senderID = inputName.getText();
                    msg.msgText = result.get(2);
                    ////Get content from command
                    privateChat.append(msg.senderID + " >> " + msg.msgText + "\n");
                    sendPrivateData();
                    //Send private messages
                } catch (IOException e) {
                    command.append("\n" + "Error");
                    command.append("\n" + "Please input commend:");
                    throw new RuntimeException(e);
                }
                command.append("\n" + "Please input commend:");
            }
            else if (result.get(0).equals("KICK") && !result.get(1).equals("ID")) {
                msg.clientCommand.put(inputName.getText(), com[1]);
                //Record the commands used
                msg.kickID = result.get(1);
                //Get KickID from command
                sendKickData();
                //Sends the id to be kicked off to the server
                command.append("\n" + "Please input commend:");
            }
            else if (result.get(0).equals("LIST")) {
                msg.clientCommand.put(inputName.getText(), com[1]);
                //Record the commands used
                sendQueryData();
                command.append("\n" + IDs.toString());
                //The server that is currently added is displayed
                command.append("\n" + "Please input commend:");
            }
            else if (result.get(0).equals("STATS") && !result.get(1).equals("ID")){
                msg.clientCommand.put(inputName.getText(), com[1]);
                //Record the commands used
                sendQueryData();
                if (!commands.isEmpty()){;
                    for (Map.Entry<String, String> entry : commands.entrySet()) {
                        if (entry.getKey().equals(result.get(1))){
                            command.append(entry.getValue() + "," + "\n");
                            //Display the command used by the id to be queried
                        }
                    }
                }
                command.append("\n" + "Please input commend:");
            }
            else {
                command.append("\n" + "Error");
                command.append("\n" + "Please input commend:");
            }
        }catch (Exception e){
            command.append("\n" + "Error");
            command.append("\n" + "Please input commend:");
        }

    }

    public void getServer() {
        //Create a Server
        //The same way the master server is set up
		try {
			serverSocket = new ServerSocket(2000);
			while (true) {
				if (serverSocket.isClosed())
					return;
                Socket clientSocket = serverSocket.accept();
                //Waiting for the private chat client to access
                if (cl < cj) {
					count[cl] = clientSocket;
					cl++;
                    //Storage clients that are added
				} else {
					Socket temp[] = new Socket[cj];
					for (int i = 0; i < cj; i++) {
						temp[i] = count[i];
					}
                    count = new Socket[cj + 5];
                    //Expand storage array
					for (int i = 0; i < cj; i++) {
						count[i] = temp[i];
					}
					count[cj] = clientSocket;
					cj = cj + 5;
					cl++;
				}
                record.count = cl;
                for (int i = 0; i < record.count; i++) {
					try {
						ObjectOutputStream objw = new ObjectOutputStream(count[i].getOutputStream());
						objw.writeObject(msg);
                        //Output message
					} catch (Exception e) {
					}
				}
                new newclientthread(clientSocket, msg, record, count, this, serverSocket);
                //Create a new thread for each newly accessed client
				}
			} catch(Exception e){
				try {
					serverSocket.close();
				} catch (Exception ey) {
				}

			}
		}


        public void run() {

        try {
            while (true) {
                ObjectInputStream obj = new ObjectInputStream(socket.getInputStream());
                Message msg = (Message) obj.readObject();
                //Read the message from the server
                if (msg.senderID != null && msg.msgText != null && !msg.msgText.equals("") && !msg.msgText.equals(""))
                    messageArea.append(msg.senderID + " >> " + msg.msgText + "\n");
                if (!msg.clientPort.isEmpty())
                    clientPort.putAll(msg.clientPort);
                    //Store ports for all clients accessing the server
                if (msg.count != null){
                    for (int i = 0; i < msg.count.length; i++){
                        if (msg.count[i] != null)
                            IDs.add(msg.count[i]);
                        //Store ID for all clients accessing the server
                        IDs.retainAll(IDs);
                    }
                    HashSet set = new HashSet(IDs);
                    IDs.clear();
                    IDs.addAll(set);
                }
                if (!msg.clientCommand.isEmpty()){
                    commands = msg.clientCommand;
                    //Store commands for all clients accessing the server
                }
            }
        } catch (Exception e) {
            inputMessage.setEditable(false);
            connect.setEnabled(true);
            send.setEnabled(false);
            disconnect.setEnabled(false);
            inputIP.setEditable(true);
            inputPort.setEditable(true);
            inputName.setEditable(true);
            hint.setText("Connection Lost");
        }

    }

}

public class User0 {
    public static void main(String a[]) {
        client f = new client("User0");
        f.setLayout(new FlowLayout());
        f.setSize(600, 970);
        f.setResizable(false);
        f.setVisible(true);
        //Create the client window
        f.getServer();
        //Set up the server inside the client
    }
}

class myadapter extends WindowAdapter {
    client f;

    public myadapter(client j) {
        f = j;
    }

    public void windowClosing(WindowEvent we) {
        f.setVisible(false);
        try {
            f.socket.close();
            f.dispose();
        } catch (Exception e) {
        }
        System.exit(1);
    }
}

//The same way the master server creates a new client thread
class newclientthread implements Runnable {
	Thread t0;
	Socket client;
	Message msg;
	data flag;
	Socket count[];
	client f;
	ServerSocket server;

	newclientthread(Socket client, Message msg, data flag, Socket count[], client f, ServerSocket server) {
		t0 = new Thread(this, "ClientSocket");
		this.server = server;
		this.client = client;
		this.msg = msg;
		this.f = f;
		this.flag = flag;
		this.count = count;
		t0.start();
	}

	public void run() {
		String name = msg.senderID;
		try {
			while (server.isClosed() != true) {
				ObjectInputStream obj = new ObjectInputStream(client.getInputStream());
				msg = (Message) obj.readObject();
				if (msg.senderID != null && msg.msgText != null) {
					f.privateChat.append(msg.senderID + " >> " + msg.msgText + "\n");
				}
				name = msg.senderID;

				for (int i = 0; i < flag.count; i++) {
					try {
						ObjectOutputStream objw = new ObjectOutputStream(count[i].getOutputStream());
						objw.writeObject(msg);
					} catch (Exception e) {
					}
				}
			}

			if (server.isClosed()) {
				for (int i = 0; i < flag.count; i++) {
					try {
						count[i].close();
					} catch (Exception e) {
					}
				}
			}

		} catch (Exception e) {
			f.privateChat.append(name + " is offline\n");
			try {
				msg.msgText = " is offline\n";
				for (int i = 0; i < flag.count; i++) {
					try {
						ObjectOutputStream objw = new ObjectOutputStream(count[i].getOutputStream());
						objw.writeObject(msg);
					} catch (Exception ex) {
					}
				}
				client.close();
			} catch (Exception ex) {

			}
		}
	}
}
