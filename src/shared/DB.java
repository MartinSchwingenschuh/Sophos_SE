package shared;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.SealedObject;

import org.apache.commons.lang3.SerializationUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class DB<K extends Serializable,V extends Serializable>{
    private File dbDir;
    private RocksDB db;

    public DB(String parentDir, String DBname){
        this.dbDir = new File(parentDir, DBname);
        initialize();
    }

    void initialize() {
        RocksDB.loadLibrary();
        final Options options = new Options();
        options.setCreateIfMissing(true);
        try {
          Files.createDirectories(dbDir.getParentFile().toPath());
          Files.createDirectories(dbDir.getAbsoluteFile().toPath());
          db = RocksDB.open(options, dbDir.getAbsolutePath());
        } catch(IOException | RocksDBException e) {
            e.printStackTrace();
        }
    }


    /**
     * T = UT -> e     ||| Token -> BigInteger
     * For token keys only the field val is used as key for the db
     * D = eind -> Doc ||| BigInteger -> SealedObject  
     * W = word -> ST  ||| String -> Token
     * @param key
     * @param value
     */
    public void save(K key, V value){
        
        try {
            if(key.getClass() == Token.class){
                //Token key
                db.put(
                    ((Token) key).val.toByteArray(),
                    SerializationUtils.serialize(value)
                );
            }else if(key.getClass() == BigInteger.class){
                //BigInteger key
                db.put(
                    ((BigInteger)key).toByteArray(),
                    SerializationUtils.serialize(value)
                );
            }else{
                //String or anything else
                db.put(
                    SerializationUtils.serialize(key),
                    SerializationUtils.serialize(value)
                );
            }
        }catch(RocksDBException e){
            e.printStackTrace();
        }
    }

    public V find(K key){
        V retVal = null;
        
        try {
            if(key.getClass() == Token.class){
                //Token key
                byte[] val = db.get(((Token) key).val.toByteArray());
                if(val != null){
                    retVal = SerializationUtils.deserialize(val);
                }
            }else if(key.getClass() == BigInteger.class){
                //BigInteger key
                byte[] val = db.get((((BigInteger) key)).toByteArray());
                if(val != null){
                    retVal = SerializationUtils.deserialize(val);
                }
            }else{
                //String or anything else
                byte[] val = db.get(SerializationUtils.serialize(key));
                if(val != null){
                    retVal = SerializationUtils.deserialize(val);
                }
            }
        }catch(ClassCastException|RocksDBException e){
            e.printStackTrace();
        }

        return retVal;
    }

    public void delete(K key){
        //TODO
    }

    public List<byte[]> getAllKeys(){
        List<byte[]> retVal = new LinkedList<byte[]>();


        RocksIterator iterator = db.newIterator();
        iterator.seekToFirst();

        while (iterator.isValid()) {
            retVal.add(iterator.key());
            iterator.next();
        }

        return retVal;
    }

    /*************************************************************/

    // public void save(Token key, byte[] value) {
    //     try {
    //         db.put(key.val.toByteArray(), value);
    //     } catch (RocksDBException e) {
    //         e.printStackTrace();
    //     }
    // }

    // public void save(String key, Token value){
    //     try {
    //         db.put(key.getBytes(), value.val.toByteArray());
    //     } catch (RocksDBException e) {
    //         e.printStackTrace();
    //     }
    // }

    // public void save(BigInteger key, SealedObject value){
    //     try {
    //         db.put(key.toByteArray(), SerializationUtils.serialize(value));
    //     } catch (RocksDBException e) {
    //         e.printStackTrace();
    //     }
    // }

    // public byte[] find(Token key) {
    //     byte[] data = null;
    //     try {
    //         data = db.get(key.val.toByteArray());
    //     } catch (RocksDBException e) {
    //         e.printStackTrace();
    //     }
    //     return data;
    // }

    // public byte[] find(String key) {
    //     byte[] data = null;
    //     try {
    //         data = db.get(key.getBytes());
    //     } catch (RocksDBException e) {
    //         e.printStackTrace();
    //     }

    //     return data;
    // }

    // public SealedObject find(BigInteger key) {
    //     SealedObject data = null;
    //     try {
    //         data = SerializationUtils.deserialize(db.get(key.toByteArray()));
    //     } catch (RocksDBException e) {
    //         e.printStackTrace();
    //     }

    //     return data;
    // }

    // public void delete(Token key) {
    //     try {
    //         db.delete(SerializationUtils.serialize(key));
    //     } catch (RocksDBException e) {
    //         e.printStackTrace();
    //     }
    // }

    // public void delete(String key) {
    //     try {
    //         db.delete(key.getBytes());
    //     } catch (RocksDBException e) {
    //         e.printStackTrace();
    //     }
    // }

    // public List<BigInteger> getAllFileIndices(){
    //     return null;
    // }
}