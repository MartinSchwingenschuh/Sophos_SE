package shared;
import java.io.Serializable;
import java.math.BigInteger;


public class Token implements Serializable{
    
    public BigInteger val;
    public int count;

    @Override
    public boolean equals(Object o){
        if(o.getClass() != Token.class){ return false; }
        if(((Token) o).count != this.count){ return false; }
        if(((Token) o).val != this.val){ return false; }
        return true;
    }

}