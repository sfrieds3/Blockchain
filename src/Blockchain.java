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

// XML libraries
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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

@XmlRootElement
class BlockchainBlock {
    private String SHA256String;
    private String signedSHA256;
    private String blockId;
    private String verificationProcessId;
    private String creatingProcessId;
    private String prevHash;
    private String firstName;
    private String lastName;
    private String dob;
    private String ssNum;
    private String diagnosis;
    private String treatment;
    private String prescription;

    public String getSHA256String() {
        return SHA256String;
    }

    @XmlElement
    public void setSHA256String(String sHA256String) {
        SHA256String = sHA256String;
    }

    public String getSignedSHA256() {
        return signedSHA256;
    }

    @XmlElement
    public void setSignedSHA256(String signedSHA256) {
        this.signedSHA256 = signedSHA256;
    }

    public String getBlockId() {
        return blockId;
    }

    @XmlElement
    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public String getVerificationProcessId() {
        return verificationProcessId;
    }

    @XmlElement
    public void setVerificationProcessId(String verificationProcessId) {
        this.verificationProcessId = verificationProcessId;
    }

    public String getCreatingProcessId() {
        return creatingProcessId;
    }

    @XmlElement
    public void setCreatingProcessId(String creatingProcessId) {
        this.creatingProcessId = creatingProcessId;
    }

    public String getPrevHash() {
        return prevHash;
    }

    @XmlElement
    public void setPrevHash(String prevHash) {
        this.prevHash = prevHash;
    }

    public String getFirstName() {
        return firstName;
    }

    @XmlElement
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @XmlElement
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDob() {
        return dob;
    }

    @XmlElement
    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getSsNum() {
        return ssNum;
    }

    @XmlElement
    public void setSsNum(String ssNum) {
        this.ssNum = ssNum;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    @XmlElement
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    @XmlElement
    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getPrescription() {
        return prescription;
    }

    @XmlElement
    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

}

class UnverifiedBlock {
    // create new XML representation of unverified block, return to BlockchainNodeMulticast
}

class CreateXml {
    // class to parse and create XML
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
            String input = "";
            String file = "./BlockInput" + pid + ".txt";
            Thread.sleep(5);
            BufferedReader userInput;
            userInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter R to read file, q to quit> ");
            BufferedReader fr = new BufferedReader(new FileReader(file));
            do {
                input = userInput.readLine();
                if (input.equals("R")) {
                    String line = fr.readLine();
                    sb.append(line);
                    new BlockchainNodeMulticast(line.toString());
                }
            } while (input.indexOf("quit") == -1);
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
    private Socket sock;
    int q_len = 6;
    private static BlockingQueue<String> unverifiedQueue; // queue of unverified blocks

    UnverifiedBlockConsumer(int p) {
        port = p;
        unverifiedQueue = new PriorityBlockingQueue<>();
        System.out.println("starting unverified block consumer");
    }

    public void run() {
        // run method
        // do work in this thread
        try {
            System.out.println("unverified block consumer port: " + port);
            ServerSocket servSock = new ServerSocket(port, q_len);
            while (true) {
                // infinite loop- keep waiting for multicast client to connect
                sock = servSock.accept(); // blocks
                // once connected, spawn unverifiedblockworker thread to handle
                new Thread(new UnverifiedBlockWorker(sock, unverifiedQueue)).start();
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    class UnverifiedBlockWorker implements Runnable {
        Socket sock;
        private BlockingQueue<String> unverifiedQueue; // queue of unverified blocks

        public UnverifiedBlockWorker(Socket s, BlockingQueue<String> queue) {
            sock = s;
            unverifiedQueue = queue;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String input = "";
                // get the next block from multicast
                input = in.readLine();
                // add to queue
                unverifiedQueue.add(input);
                printQueue();
                System.out.println("unverified block worker: " + input);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

        private void printQueue(){
            System.out.println(unverifiedQueue.toString());
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
