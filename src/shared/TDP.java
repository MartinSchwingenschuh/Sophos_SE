package shared;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class TDP {
    
    private KeyPair keyPair;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    TDP() throws NoSuchAlgorithmException{
        
        KeyPairGenerator RSAGenerator = KeyPairGenerator.getInstance("RSA");
        this.keyPair = RSAGenerator.generateKeyPair();

        //TODO; think about proper key storage
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }


    /**
     * 
     * @param input
     * @return
     */
    public BigInteger tdp(BigInteger input){

        if(this.publicKey == null){ return null; }

        BigInteger m = this.publicKey.getModulus();
        BigInteger e = this.publicKey.getPublicExponent();

        return input.modPow(e, m);
    }

    /**
     * 
     * @param input
     * @return
     */
    public BigInteger itdp(BigInteger input){

        if(this.privateKey == null){ return null; }

        BigInteger m = this.privateKey.getModulus();
        BigInteger d = this.privateKey.getPrivateExponent();

        return input.modPow(d, m);
    }

    public RSAPublicKey getPublicKey(){
        return this.publicKey;
    }

    public void setRSAKeys(RSAPrivateKey privateKey, RSAPublicKey publicKey){
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

}