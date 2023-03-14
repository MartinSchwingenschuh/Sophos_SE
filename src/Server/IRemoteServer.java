package Server;

import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import javax.crypto.SealedObject;

import shared.Token;

public interface IRemoteServer extends Remote{
    
    /**
     * 
     * @param f
     * @param e
     * @throws RemoteException
     */
    void uploadFile(BigInteger ind, SealedObject file) throws RemoteException;

    /**
     * 
     * @param e
     * @return
     * @throws RemoteException
     */
    SealedObject getFile(BigInteger ind) throws RemoteException;

    /**
     * 
     * @return a list of fileindices which are the encrypted filename
     * @throws RemoteException
     */
    List<BigInteger> getAllFileIndices() throws RemoteException;

    /**
     * 
     * @param ST
     * @param Kw
     * @return
     * @throws RemoteException
     */
    List<SealedObject> search(Token ST, BigInteger Kw) throws RemoteException;


    /**
     * 
     * @param UT
     * @param e
     */
    void update(Token UT, BigInteger e) throws RemoteException;

    /**
     * 
     * @param key
     * @throws RemoteException
     */
    void setPublicKey(RSAPublicKey key) throws RemoteException;

}
