package shared;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public class Message implements Serializable{
    
    public Type messageType;
    public BigInteger Kw;
    public Token ST;
    public Token UT;
    public BigInteger index;
    public List<BigInteger> indicesResult;

    public Message(Type messageType){
        this.messageType = messageType;
        
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Message Type = ");
        switch (this.messageType) {
            case SEARCH:
                sb.append("SEARCH\t");
                sb.append("ST=");
                sb.append(ST.val);
                break;
            case UPDATE:
                sb.append("UPDATE \t");
                sb.append("UT="+UT.val);
                break;
            default:
                break;
        }
        //TODO: do the rest
        return sb.toString();
    }

    public enum Type{
        UPDATE,
        UPDATE_RESULT,
        SEARCH,
        SEARCH_RESULT,
        INDEX,
        ERROR
    }
}
