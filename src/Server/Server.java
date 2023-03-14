package Server;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.interfaces.RSAPublicKey;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.SealedObject;

import shared.Crypto;
import shared.DB;
import shared.Token;

public class Server extends UnicastRemoteObject implements IRemoteServer{

    private RSAPublicKey publicKey;
    private DB<Token,BigInteger> T; //Token UT -> BigInteger e
    final static String T_DB_NAME = "TokenStorage";
    private DB<BigInteger,SealedObject> D; //BigInteger eind -> File EDOC
    final static String D_DB_NAME = "DocumentStorage";
    final static String DB_PARENT_DIR = "./src/Server/";


    public Server() throws RemoteException{
        //init rocksDB
        T = new DB<Token,BigInteger>(DB_PARENT_DIR, T_DB_NAME);
        D = new DB<BigInteger,SealedObject>(DB_PARENT_DIR, D_DB_NAME);
    }
    
    /***
     * 
     * @param UT
     * @param e
     */
    @Override
    public void update(Token UT, BigInteger e){
        // T.put(UT.val, e);
        System.out.println("[SINFO] update with UT " + UT.val);
        T.save(UT, e);
    }

    /***
     * 
     * @param ST
     * @param Kw
     */
    @Override
    public List<SealedObject> search(Token ST, BigInteger Kw){
        System.out.println("[SINFO] search with ST "+ST.val);
        List<SealedObject> result = new LinkedList<SealedObject>();
        
        for (int i = ST.count; i > 0; i--) {
            
            //calculate UT = H1(Kw,STi)
            Token UT = new Token();
            UT.val = Crypto.hmac(Kw, ST.val);
            System.out.println("[SINFO] search with UT " + UT.val);

            //get the encrypted index
            BigInteger e = T.find(UT);

            if(e != null){
                BigInteger eind = e.xor(UT.val);

                SealedObject edoc = D.find(eind);
                result.add(edoc);

                System.out.println("Server: found word in index: "+eind);
            }
            
            //calculate the next ST
            ST.val = Crypto.tdp(ST.val, publicKey);
        }
        if(result.size() == 0) return null;
        return result;
    }

    @Override
    public void uploadFile(BigInteger ind, SealedObject f) throws RemoteException {
        System.out.println("[INFO] uploadFile accessed");
        D.save(ind, f);   
    }

    @Override
    public SealedObject getFile(BigInteger ind) throws RemoteException {
        System.out.println("[INFO] getFile accessed");        
        return D.find(ind);
    }

    @Override
    public List<BigInteger> getAllFileIndices() throws RemoteException {
        List<byte[]> keyList = D.getAllKeys();
        List<BigInteger> retVal = new LinkedList<BigInteger>();

        for (byte[] key : keyList) {
            retVal.add(new BigInteger(key));
        }

        return retVal;
    }

    @Override
    public void setPublicKey(RSAPublicKey key){
        this.publicKey = key;
    }


    public static void main(String[] args) {
        
        try{     
            Server server = new Server();
            Registry registry = LocateRegistry.createRegistry(5000);
            registry.bind("Sophos:ServerStub", server);

            System.err.println("[INFO] Server ready");
        
        }catch(Exception e){
            e.printStackTrace();
        }
    }    
}