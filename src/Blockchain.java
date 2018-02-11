/*----------------------------------------------------------
* File: Blockchain.java
* Compilation: javac Blockchain.java
* Usage (in different shell window):
* Files needed to run:
*   - Blockchain.java
----------------------------------------------------------*/

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

class Blockchain {
    public static void main(String[] args) {
        int q_len = 6; // queue length
        int pid = ((args.length < 1) ? 0 : Integer.parseInt(args[0]));
        System.out.println("pid: " + pid);
        new BlockchainNode(pid);
        System.out.println("Scott Friedrich's blockchain framework.");
        System.out.println("Using processID: " + pid + "\n");
    }
}

class BlockchainNode {
    private UnverifiedBlockServer unverifiedBlockServer; // server to receive in new block
    private UnverifiedBlockConsumer unverifiedBlockConsumer; // consumer to do "work"
    private Stack<BlockchainBlock> blockchainStack; // stack to store full blockchain
    private BlockingQueue<String> unverifiedQueue; // queue of unverified blocks
    private int numProcesses = 3; // number of processes
    private int privateKey; // private key for server
    private int pid;
    private int updatedBlockchainPort;
    private int unverifiedBlockPort;
    private int publicKeyServerPort;

    BlockchainNode(int pid) {
        // set pid of BlockchainNode
        setPid(pid);

        // privateKey = Keys.getInstance.getPrivateKey();
        unverifiedBlockServer = new UnverifiedBlockServer(pid);
        unverifiedBlockConsumer = new UnverifiedBlockConsumer(Ports.getInstance().getUnverifiedBlockPort(pid));
        blockchainStack = new Stack<>();

        // intialize threads
        new Thread(unverifiedBlockServer).start();
        new Thread(unverifiedBlockConsumer).start();

        // get port numbers
        setPorts();

        // tell BlockchainNodeMulticast the number of processes
        BlockchainNodeMulticast.setNumProcesses(numProcesses);
    }

    private void setPorts() {
        // get required port numbers, stored in BlockchainNode instance
        unverifiedBlockPort = Ports.getInstance().getUnverifiedBlockPort(pid);
        updatedBlockchainPort = Ports.getInstance().getUpdatedBlockchainPort(pid);
        publicKeyServerPort = Ports.getInstance().getPublicKeyServerPort(pid);
    }

    private void setPid(int pnum) {
        pid = pnum;
    }

    public int getPid() {
        return pid;
    }
}

class BlockchainBlock {
    private String previousBlockHash;
    private String currentBlockHash;
    private String currentBlockContents;
    private String randSolution;

    BlockchainBlock(String prevHash, String newBlockHash, String newBlockContents) {
        previousBlockHash = prevHash;
        currentBlockHash = newBlockHash;
        currentBlockContents = newBlockContents;
    }
}

class BlockchainNodeMulticast {
    // CLIENT
    // singleton
    // multicast for all blockchain nodes
    private static int numProcesses;
    private String serverName = "localhost";
    private int q_len = 6;
    private String newBlock;

    public BlockchainNodeMulticast(String input) {
        newBlock = input;
        new Thread(new MulticastWorker(input)).start();
    }

    public static void setNumProcesses(int num) {
        numProcesses = num;
    }

    class MulticastWorker implements Runnable {
        private String message;
        private Socket sock;
        private int port;

        private MulticastWorker(String input) {
            message = input;
        }

        public void run() {
            try {
                for (int i = 0; i < numProcesses; i++) {
                    // multicast to all blockchain servers
                    port = Ports.getInstance().getUnverifiedBlockPort(i);
                    System.out.println("multicastworker port = " + port);
                    System.out.println("num processes: " + numProcesses);
                    sock = new Socket(serverName, port);
                    PrintStream out = new PrintStream(sock.getOutputStream());
                    out.println(message);
                    sock.close();
                }
            } catch (IOException ex) {
                System.out.println("multicast worker error");
                System.out.println(ex);
            }
        }
    }
}

class UnverifiedBlockServer implements Runnable {
    // read data in from text file
    // tell BlockchainNodeList class to multicast to everyone
    // UnverifiedBlockWorker does "work" on new block
    // once verified, UnverifiedBlockWorker tells BlockChainNodeList to multicast
    private int pid;

    public UnverifiedBlockServer(int p) {
        pid = p;
    }

    public void run() {
        //run method
        System.out.println("hello from unverifiedBlockServer");
        // read data in from text file
        StringBuilder sb = new StringBuilder();
        try {
            String file = "./BlockInput" + pid + ".txt";
            Thread.sleep(5);
            BufferedReader fr = new BufferedReader(new FileReader(file));
            String line = fr.readLine();
            sb.append(line);
            new BlockchainNodeMulticast(line.toString());
        } catch (IOException ex) {
            System.out.println("File not found.");
        } catch (Exception e) {
            System.out.println("interruped exception " + e);
        }
    }

}

class UnverifiedBlockConsumer implements Runnable {
    // SERVER
    // class to do "work" on new block
    private int port;
    Socket sock;
    int q_len = 6;

    UnverifiedBlockConsumer(int p) {
        port = p;
        System.out.println("starting unverified block consumer");
    }

    public void run() {
        // run method
        // do work in this thread
        try {
            System.out.println("unverified block consumer port: " + port);
            ServerSocket servSock = new ServerSocket(port, q_len);
            while (true) {
                sock = servSock.accept(); // blocks
                new Thread(new UnverifiedBlockWorker(sock)).start();
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    class UnverifiedBlockWorker implements Runnable {
        Socket sock;

        public UnverifiedBlockWorker(Socket s) {
            sock = s;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String input = "";
                input = in.readLine();
                System.out.println("unverified block worker: " + input);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }
}

class Keys {
    // singleton
    // provide public key to all clients
    // calculate and return new private key to BlockchainNode
    private static Keys instance = null;
    private ArrayList<String> publicKeyList;

    private Keys() {
        publicKeyList = new ArrayList<>();
    }

    public static synchronized Keys getInstance() {
        if (instance == null) {
            instance = new Keys();
        }
        return instance;
    }

    public int getPrivateKey() {
        // calculate public and private key here
        // add public key to publicKeyList
        // return private key to BlockchainNode
        return 0;
    }
}

class Ports {
    // singleton
    private static Ports instance = null;
    private int publicKeyServerBasePort;
    private int unverifiedBlockBasePort;
    private int updatedBlockchainBasePort;

    private Ports() {
        publicKeyServerBasePort = 4701;
        unverifiedBlockBasePort = 4820;
        updatedBlockchainBasePort = 4930;
    }

    public static synchronized Ports getInstance() {
        if (instance == null) {
            instance = new Ports();
        }
        return instance;
    }

    public int getPublicKeyServerPort(int pid) {
        return publicKeyServerBasePort + pid;
    }

    public int getUnverifiedBlockPort(int pid) {
        return unverifiedBlockBasePort + pid;
    }

    public int getUpdatedBlockchainPort(int pid) {
        return updatedBlockchainBasePort + pid;
    }
}
