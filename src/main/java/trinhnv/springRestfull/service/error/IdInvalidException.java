package trinhnv.springRestfull.service.error;

import org.aspectj.bridge.IMessage;

public class IdInvalidException  extends  RuntimeException{
    public IdInvalidException(String message){
        super(message);
    }

}
