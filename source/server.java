import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


class data {
    public volatile int signal;
    public volatile int count;
}

class ser extends JFrame implements ActionListener, Runnable {
    Thread t;
    //Thread of the server
    JButton start, stop;
    //Buttons that enable and disable the server
    JTextField portNum;
    //The initial server port
    JLabel state;
    //Display of server status
    TextArea message;
    //Display of client information
    ServerSocket server;
    //server
    Message msg = new Message();
    //Information passed between CS
    data record = new data();
    // Recording the number of servers
    HashMap<String, Integer> clientPort = new HashMap<>();
    //Store the server ports of different clients
    IdentityHashMap<String, String> clientCommands = new IdentityHashMap<>();
    //Store the commands used by the client
    Socket count[] = new Socket[5];
    //Initially, six clients can be connected
    String kickID[] = new String[5];
    //Used to store the id of the client
    int cj = 5;
    int cl = 0;
    ser(String s) {
        super(s);

        record.signal = 0;
        record.count = 0;

        JLabel l3 = new JLabel("Enter Port No. : ");
        add(l3);

        portNum = new JTextField(7);
        portNum.setText("8000");
        add(portNum);
        //A text box used to enter a port

        JLabel l1 = new JLabel("Start the Server");
        add(l1);
        start = new JButton("Start");
        start.addActionListener(this);
        add(start);
        //A Button used to start server

        JLabel l2 = new JLabel("Stop the Server");
        add(l2);
        stop = new JButton("Stop");
        stop.addActionListener(this);
        add(stop);
        stop.setEnabled(false);
        //A Button used to stop server

        JLabel l4 = new JLabel("Status : ");
        add(l4);
        add(new JLabel("    "));
        state = new JLabel("Server is not running...");
        add(state);
        //Display server status
        message = new TextArea("", 15, 70);
        message.setEditable(false);
        message.setBackground(Color.WHITE);
        message.setFont(Font.getFont("verdana"));
        add(message);
        //Show message passing between CS
        mywindowadapter a = new mywindowadapter(this);
        addWindowListener(a);
    }

    public void actionPerformed(ActionEvent ae) {
        //This method is used to bind events to the button
        try {
            String str = ae.getActionCommand();
            if (str.equals("Start")) {
                //Start button
                String str2 = portNum.getText();
                if (!str2.equals("")) {
                    try {
                        server = new ServerSocket(Integer.parseInt(str2));
                        //Initialize the server socket
                        state.setText("Server is running....");
                        //Setting Server Status
                        portNum.setEnabled(false);
                        start.setEnabled(false);
                        stop.setEnabled(true);

                        record.count = 0;

                        count = new Socket[5];
                        kickID = new String[5];
                        //Initializing array
                        cj = 5;
                        cl = 0;

                        t = new Thread(this, "Running");
                        t.start();
                        //Starting a new thread
                    } catch (Exception e) {
                        state.setText("Either the port no. is invalid or is in use");
                    }
                } else
                    state.setText("Enter port no.");
            }
            if (str.equals("Stop")) {
                //Stop button
                try {
                    server.close();
                    //close server
                } catch (Exception ee) {
                    state.setText("Error closing server");
                }
                state.setText("Server is closed");
                //Setting Server Status
                portNum.setEnabled(true);
                start.setEnabled(true);
                stop.setEnabled(false);
                //Setting button Status
                server = null;
                t = null;
                //Set the server thread to null
                for (int i = 0; i < record.count; i++) {
                    try {
                        count[i].close();
                        //When the server is shut down, all clients are also shut down
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    public void run() {
        while (true) {
            if (server.isClosed())
                return;
            //If the server is closed, return
            try {

                Socket client = server.accept();
                //Block, waiting for the server to connect

                ObjectInputStream obj = new ObjectInputStream(client.getInputStream());
                msg = (Message) obj.readObject();
                //Read the input stream from the client

                clientPort.putAll(msg.clientPort);
                msg.clientPort = clientPort;
                //Storage ports for accessing clients
                clientCommands.putAll(msg.clientCommand);
                msg.clientCommand = clientCommands;
                //Store the used commands of the access client

                if (cl < cj) {
                    count[cl] = client;
                    kickID[cl] = msg.senderID;
                    cl++;
                    //Storage client and id
                } else {
                    //If the number of access clients exceeds the array length, expand the array
                    Socket temp[] = new Socket[cj];
                    String temp1[] = new String[cj];
                    for (int i = 0; i < cj; i++) {
                        temp[i] = count[i];
                        temp1[i] = kickID[i];
                    }
                    count = new Socket[cj + 5];
                    kickID = new String[cj + 5];
                    //The length plus 5
                    for (int i = 0; i < cj; i++) {
                        count[i] = temp[i];
                        kickID[i] = temp1[i];
                    }
                    count[cj] = client;
                    kickID[cj] = msg.senderID;
                    cj = cj + 5;
                    cl++;
                    //Update the record
                }
                record.count = cl;
                //Update the record
                for (int i = 0; i < record.count; i++) {
                    try {
                        ObjectOutputStream objw = new ObjectOutputStream(count[i].getOutputStream());
                        msg.count = kickID;
                        objw.writeObject(msg);
                        //Pass information to each access client
                    } catch (Exception e) {
                    }
                }
                new newthread(client, msg, record, count, kickID, clientCommands,this, server);
                //Each new client starts a new thread
            } catch (Exception e) {
                //If an error occurs, shut down the server and update the server status
                state.setText("Server is stopped");
                portNum.setEnabled(true);
                try {
                    server.close();
                } catch (Exception ey) {
                    state.setText("Error closing server");
                }

            }
        }


    }

}

class server {
    //Opening a window
    public static void main(String a[]) throws IOException {
        ser f = new ser("Chat Server");
        f.setLayout(new FlowLayout());
        f.setSize(550, 365);
        f.setResizable(false);
        f.setVisible(true);
    }
}


class newthread implements Runnable {
    Thread t;
    Socket client;
    Message msg;
    data record;
    Socket count[];
    String kickID[];
    IdentityHashMap<String, String> clientCommands = new IdentityHashMap<>();
    ser ser;
    ServerSocket server;

    newthread(Socket client, Message msg, data record, Socket count[], String kickID[], IdentityHashMap<String, String> clientCommands, ser ser, ServerSocket server) {
        t = new Thread(this, "Client");
        this.server = server;
        this.client = client;
        this.msg = msg;
        this.ser = ser;
        this.record = record;
        this.count = count;
        this.kickID = kickID;
        this.clientCommands = clientCommands;
        //Get information from the server
        t.start();
        //Start thread
    }

    public void run() {
        String name = msg.senderID;
        try {
            while (server.isClosed() != true) {
                ObjectInputStream obj = new ObjectInputStream(client.getInputStream());
                msg = (Message) obj.readObject();
                //Read the information input stream
                if (msg.senderID != null && msg.msgText != null && !msg.senderID.equals("") && !msg.msgText.equals("")) {
                    ser.message.append(msg.senderID + " >> " + msg.msgText + "\n");
                    //Displays the passed information in a text box
                }

                if (!msg.clientCommand.isEmpty()){
                    clientCommands.putAll(msg.clientCommand);
                    //Store the commands used by each client
                    msg.clientCommand = clientCommands;
                    //Put the stored command into the message used to pass it
                }

                name = msg.senderID;
                for (int i = 0; i < record.count; i++) {
                    try {
                        ObjectOutputStream objw = new ObjectOutputStream(count[i].getOutputStream());
                        objw.writeObject(msg);
                        int position = -1;
                        //When there are id in the message that need to be kicked off
                        if (!msg.kickID.equals("")) {
                            for (int j = 0; j < kickID.length; j++) {
                                if (kickID[j].equals(msg.kickID)) {
                                    position = j;
                                    //Locate the client in the array based on the id
                                    if (position != -1) {
                                        count[position].close();
                                        //Close the client that has been kicked
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
            if (server.isClosed()) {
                for (int i = 0; i < record.count; i++) {
                    try {
                        count[i].close();
                        //When the server is shut down, shut down all clients
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            ser.message.append(name + " is offline\n");
            //After the client is closed, the current user is displayed as offline
            try {
                msg.msgText = " is offline\n";
                for (int i = 0; i < record.count; i++) {
                    try {
                        ObjectOutputStream objw = new ObjectOutputStream(count[i].getOutputStream());
                        objw.writeObject(msg);
                    } catch (Exception ex) {
                    }
                }
                client.close();
                //Shut down the offline server
            } catch (Exception ex) {
            }
        }
    }
}


class mywindowadapter extends WindowAdapter {
    ser ser;
    public mywindowadapter(ser j) {
        ser = j;
    }

    public void windowClosing(WindowEvent we) {
        ser.setVisible(false);
        try {
            ser.server.close();
            //Shut down the server when the window closes
        } catch (Exception e) {
        }
        ser.dispose();
        System.exit(0);
    }
}