package Client;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SealedObject;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import Server.IRemoteServer;
import shared.Crypto;
import shared.DB;
import shared.Token;

public class Client {

    //Crypto functionality
    private Crypto crypto;

    //persistent Token Storage
    private DB<String,Token> W;

    //communication with server
    IRemoteServer serverStub;
    private String serverIP;
    static final String DEFAULT_SERVER_IP = "localhost";
    private String serverPort;
    static final int DEFAULT_SERVER_PORT = 5000;


    //GUI
    GUI gui;

    public Client(){
        //init the token storage
        this.W = new DB<String,Token>("./src/Client/", "TokenStorage");

        try {
            this.crypto = new Crypto("./src/Client/","password");
        } catch (Exception e) {
            e.printStackTrace();
        }        

        //RMI communication setup with server
        try {
            Registry registry = LocateRegistry.getRegistry(DEFAULT_SERVER_IP,DEFAULT_SERVER_PORT);
            serverStub = (IRemoteServer) registry.lookup("Sophos:ServerStub");
            serverStub.setPublicKey(crypto.publicKey);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }

        //gui stuff
        gui = new GUI(this);
    }

    /**
     * 
     * @param word
     * @param docInd
     */
    void update(String word, BigInteger eind){

        //TODO: build a logger
        if(word == null){ return; }
        if(eind == null){return; }

        //read ST from W[w]
        // Token ST = W.get(word);
        Token ST = W.find(word);

        //if first entry for the word create a new random ST
        //if not compute the next ST for this word
        if(ST == null){
            ST = new Token();
            ST.val = crypto.getRandomST();
            ST.count = 1;
        }else{
            ST.val = Crypto.itdp(ST.val, crypto.privateKey);
            ST.count++;
        }

        //store STc+1
        // W.put(word, ST);
        W.save(word, ST);
        
        //KW <- F(Ks,w) //TODO: check if that is really a PRF
        BigInteger Kw = Crypto.hmac(new BigInteger(word.getBytes()), crypto.symmetricKey);
        
        //calculate UT
        Token UT = new Token();
        UT.val = Crypto.hmac(Kw,ST.val);
        UT.count = -1;    

        //TODO: is the ind really the docname/path?
        //TODO: replace this with a proper index infrastructure
        // BigInteger ind = new BigInteger(eind);

        //calculate the encrypted index
        BigInteger e = eind.xor(UT.val);

        //send UTc, e to server
        try {
            serverStub.update(UT, e);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 
     * @param word
     */
    public List<File> search(String word){
        
        // Token ST = W.get(word);
        Token ST = W.find(word);
        if(ST == null){ 
            System.out.println("[INFO]" + word + "not found");
            return null; 
        }       

        BigInteger Kw = Crypto.hmac(new BigInteger(word.getBytes()), crypto.symmetricKey);
        
        List<File> retVal = new ArrayList<File>();
        try {
            List<SealedObject> encrFiles = serverStub.search(ST, Kw);
            for (SealedObject sealedObject : encrFiles) {
                File file = (File) Crypto.decryptObject(
                    Crypto.DEFAULT_SYM_ALGORITHM, 
                    sealedObject, 
                    crypto.symmetricKey, 
                    crypto.IV_privKeyEncr
                );
                retVal.add(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retVal;
    }

    /**
     * 
     * @return
     */
    public RSAPublicKey getPublicKey(){
        return crypto.getPublicKey();
    }

    /**
     * 
     * @param filePath
     */
    public void addDocument(Path filePath){
        
        //encrypt document and send it to server for storage
        File document = filePath.toFile();
        BigInteger eind = uploadFile(document);
        

        //extract each word of the document and process them
        Pattern PDFpattern = Pattern.compile(".pdf", Pattern.CASE_INSENSITIVE);
        Matcher PDFmatcher = PDFpattern.matcher(document.getName());

        Pattern TXTpattern = Pattern.compile(".txt", Pattern.CASE_INSENSITIVE);
        Matcher TXTmatcher = TXTpattern.matcher(document.getName());
    
        List<String> wordList = new ArrayList<String>();

        if(PDFmatcher.find()){
            //extract words from pdf document
            try {
                PDDocument PDF = PDDocument.load(document);
                if (!PDF.isEncrypted()) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(PDF);
                    wordList = Arrays.asList(text.split("\\s"));                    
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(TXTmatcher.find()){
            try{
                Scanner scanner = new Scanner(document);
                while(scanner.hasNext()){
                    String word = scanner.next();
                    wordList.add(word);
                }
                scanner.close(); 
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        //pre process the word list
        List<String> filteredWordList = new ArrayList<String>();
        wordList.stream().distinct().filter(w -> {
            //remove single digits
            Pattern pattern = Pattern.compile("[0-9]", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(w.trim());
            return !matcher.matches();
        }).forEach(w -> {

            //remove whitespaces
            w = w.trim();

            //remove '.' and ',' at the end of the word
            if(w.endsWith(".")){
                w = w.replace(".", "");
            }else if(w.endsWith(",")){
                w = w.replace(",", "");
            }

            if (!w.isBlank()) {
                filteredWordList.add(w);
            }            
        });

        //call an update for each found word
        for (String word : filteredWordList) {
            update(word, eind);
        }
    }

    BigInteger uploadFile(File document){

        //encrypt index
        String filename = document.getName();
        BigInteger eind = null;

        try {
            String encrInd = Crypto.encrypt(
                Crypto.DEFAULT_SYM_ALGORITHM, 
                filename, 
                crypto.symmetricKey, 
                crypto.IV_privKeyEncr
            );
            eind = new BigInteger(encrInd.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //encrypt file
        SealedObject encrFile = null;
        try {
            encrFile = Crypto.encryptObject(
                Crypto.DEFAULT_SYM_ALGORITHM,
                document,
                crypto.symmetricKey, 
                crypto.IV_privKeyEncr
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        //upload file with encrypted index as key
        try {
            serverStub.uploadFile(eind, encrFile);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return eind;
    }

    File downloadFile(BigInteger eind){

        File retVal = null;

        try {
            SealedObject encrFile = serverStub.getFile(eind);
            retVal = (File) Crypto.decryptObject(
                Crypto.DEFAULT_SYM_ALGORITHM, 
                encrFile, 
                crypto.symmetricKey, 
                crypto.IV_privKeyEncr
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retVal;
    }

    File downloadFile(String filename){

        BigInteger eind = null;

        try {
            String encrInd = Crypto.encrypt(
                Crypto.DEFAULT_SYM_ALGORITHM, 
                filename, 
                crypto.symmetricKey, 
                crypto.IV_privKeyEncr
            );
            eind = new BigInteger(encrInd.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return downloadFile(eind);
    }

    public static void main(String[] args) {
        System.out.println("[INFO] Client started");
        Client client = new Client();
    }
}