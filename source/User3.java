import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.*;

/***
 * User3
 * Copy of User0
 */
class client3 extends JFrame implements ActionListener, Runnable {
    Socket socket = null;

    Socket chatSocket = null;
    ServerSocket serverSocket;
    JLabel name, message, status, receivedMessage, IP, Port, privateMessage, commandline, hint;
    JTextField inputName, inputMessage, inputIP, inputPort;
    TextArea messageArea, command, privateChat;
	HashMap<String, Integer> clientPort = new HashMap<>();

    IdentityHashMap<String, String> commands = new IdentityHashMap<>();
    List<String> IDs= new ArrayList<>();
    Message msg = new Message();
    InetAddress host;
    int port = 8000;
    Thread user3 = null;
    JButton send, connect, disconnect;
	data record = new data();
    Socket count[] = new Socket[5];
    int cj = 5;
    int cl = 0;

    client3(String s) {
        super(s);

        myadapter3 a = new myadapter3(this);
        addWindowListener(a);

        IP = new JLabel("Enter IP : ");
        add(IP);
        inputIP = new JTextField(15);
        add(inputIP);
        inputIP.setText("127.0.0.1");

        add(new JLabel("            "));

        Port = new JLabel("Enter Port : ");
        add(Port);
        inputPort = new JTextField(15);
        add(inputPort);
        inputPort.setText(String.valueOf(port));
        add(new JLabel(""));


        add(new JLabel("      "));

        name = new JLabel("Nick Name :  ");
        add(name);
        inputName = new JTextField(15);
        add(inputName);


        connect = new JButton("Connect");
        add(connect);
        connect.addActionListener(this);

        disconnect = new JButton("Disconnect");
        add(disconnect);
        disconnect.addActionListener(this);
        disconnect.setEnabled(false);

        add(new JLabel("                                        "));

        message = new JLabel("Message : ");
        add(message);
        inputMessage = new JTextField(34);
        add(inputMessage);
        inputMessage.setEditable(false);

        send = new JButton("Send Message");
        add(send);
        send.addActionListener(this);
        send.setEnabled(false);
        add(new JLabel("      "));

        status = new JLabel("Status : ");
        add(status);
        hint = new JLabel("Not connected to the server...");
        add(hint);

        add(new JLabel("                                                                            "));

        receivedMessage = new JLabel("Recieved Messages : ");
        add(receivedMessage);
        messageArea = new TextArea("", 15, 80);
        add(messageArea);
        messageArea.setFont(Font.getFont("verdana"));
        messageArea.setBackground(Color.ORANGE);
        messageArea.setEditable(false);

		privateMessage = new JLabel("Recieved Private Messages : ");
		add(privateMessage);
		privateChat = new TextArea("", 15, 80);
		add(privateChat);
		privateChat.setFont(Font.getFont("verdana"));
		privateChat.setBackground(Color.gray);
		privateChat.setEditable(false);


        commandline = new JLabel("Command Line : ");
        add(commandline);
        command = new TextArea("", 15, 80);
        add(command);
        command.setFont(Font.getFont("verdana"));
        command.append("\n" + "Please input commend:");

        command.setBackground(Color.white);
        command.setEditable(false);
        command.requestFocus();


        hint.setText("Not connected to Server, click connect");
    }

    public void actionPerformed(ActionEvent ae1) {
        try {
            String str = ae1.getActionCommand();

            if (str.equals("Disconnect")) {
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
            }

            if (str.equals("Send Message")) {
                msg.senderID = inputName.getText();
                msg.msgText = inputMessage.getText();
                inputMessage.setText("");

                if (!msg.senderID.equals("") && !msg.msgText.equals("")) {
                    sendData();
                } else
                    hint.setText("Message was not sent, type a message");
            }

            if (str.equals("Connect")) {
                try {
                    host = InetAddress.getByName(inputIP.getText());
                    String p = inputPort.getText();
                    try {
                        if (socket != null) {
                            socket.close();
                            socket = null;
                        }
                    } catch (Exception e) {
                    }

                    if (!inputName.getText().equals("")) {
                        socket = new Socket(host, Integer.parseInt(p));

                        ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());
                        msg.senderID = inputName.getText();
                        msg.msgText = " is online at " + new Date().toString();
                        msg.clientPort.put(inputName.getText(), 2003);
                        obj.writeObject(msg);

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
                        hint.setText("Connection established with Server, start chatting");
                        user3 = new Thread(this, "Reading1");
                        user3.start();
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
        try {
            ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());
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

    public void sendKickData() {
        try {
            ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());
            obj.writeObject(msg);
            hint.setText("Kick successfully");
            msg.senderID = "";
            msg.msgText = "";
            msg.kickID = "";
            msg.clientCommand.clear();
        } catch (Exception e) {
            hint.setText("Error occured while sending message");
        }
    }
    public void sendQueryData() {
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
    public void sendPrivateData() {
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
        try{

            if (result.isEmpty()) {
                command.append("\n" + "Error");
                command.append("\n" + "Please input commend:");
            }
            else if (result.get(0).equals("BROADCAST")) {
                msg.senderID = inputName.getText();
                msg.msgText = result.get(1);
                msg.clientCommand.put(inputName.getText(), com[1]);
                if (!msg.senderID.equals("") && !msg.msgText.equals("")) {
                    sendData();
                } else
                    hint.setText("Message was not sent, type a message");
                command.append("\n" + "Please input commend:");
            } else if (result.get(0).equals("STOP")) {
                msg.senderID = inputName.getText();
                msg.clientCommand.put(inputName.getText(), com[1]);
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
                msg.clientCommand.put(inputName.getText(), com[1]);
                try {
                    int port = clientPort.get(result.get(1));
                    chatSocket = new Socket(host, port);
                    msg.senderID = inputName.getText();
                    msg.msgText = result.get(2);
                    privateChat.append(msg.senderID + " >> " + msg.msgText + "\n");
                    sendPrivateData();
                } catch (IOException e) {
                    command.append("\n" + "Error");
                    command.append("\n" + "Please input commend:");
                    throw new RuntimeException(e);

                }

                command.append("\n" + "Please input commend:");
            }
            else if (result.get(0).equals("KICK") && !result.get(1).equals("ID")) {
                msg.clientCommand.put(inputName.getText(), com[1]);
                msg.kickID = result.get(1);
                sendKickData();
                command.append("\n" + "Please input commend:");
            }
            else if (result.get(0).equals("LIST")) {
                msg.clientCommand.put(inputName.getText(), com[1]);
                sendQueryData();
                command.append("\n" + IDs.toString());
                command.append("\n" + "Please input commend:");
            }
            else if (result.get(0).equals("STATS") && !result.get(1).equals("ID")){
                msg.clientCommand.put(inputName.getText(), com[1]);
                sendQueryData();
                if (!commands.isEmpty()){;
                    for (Map.Entry<String, String> entry : commands.entrySet()) {
                        if (entry.getKey().equals(result.get(1))){
                            command.append(entry.getValue() + "," + "\n");
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
        try {
            serverSocket = new ServerSocket(2003);
			while (true) {
				if (serverSocket.isClosed())
					return;

				Socket clientSocket = serverSocket.accept();
                if (cl < cj) {
					count[cl] = clientSocket;
					cl++;
				} else {
					Socket temp[] = new Socket[cj];
					for (int i = 0; i < cj; i++) {
						temp[i] = count[i];
					}

					count = new Socket[cj + 5];
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
					} catch (Exception e) {
					}
				}

                new newclientthread3(clientSocket, msg, record, count, this, serverSocket);
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
                Message msg = new Message();
                msg = (Message) obj.readObject();
                if (msg.senderID != null && msg.msgText != null&& !msg.msgText.equals("") && !msg.msgText.equals(""))
                    messageArea.append(msg.senderID + " >> " + msg.msgText + "\n" );
                if (msg.clientPort != null)
                    clientPort.putAll(msg.clientPort);
                if (msg.count != null){
                    for (int i = 0; i < msg.count.length; i++){
                        if (msg.count[i] != null)
                            IDs.add(msg.count[i]);
                        IDs.retainAll(IDs);
                    }
                    HashSet set = new HashSet(IDs);
                    IDs.clear();
                    IDs.addAll(set);
                }
                if (!msg.clientCommand.isEmpty()){
                    commands = msg.clientCommand;
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

public class User3 {
    public static void main(String a[]) {
        client3 f = new client3("User3");
        f.setLayout(new FlowLayout());
        f.setSize(600, 970);
        f.setResizable(false);
        f.setVisible(true);
        f.getServer();
    }
}

class myadapter3 extends WindowAdapter {
    client3 f;

    public myadapter3(client3 j) {
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
class newclientthread3 implements Runnable {
	Thread t3;
	Socket client;
	Message msg;
	data flag;
	Socket count[];
	client3 f;
	ServerSocket server;

	newclientthread3(Socket client, Message msg, data flag, Socket count[], client3 f, ServerSocket server) {
		t3 = new Thread(this, "ClientSocket3");
		this.server = server;
		this.client = client;
		this.msg = msg;
		this.f = f;
		this.flag = flag;
		this.count = count;
		t3.start();
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
