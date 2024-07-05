public class GenericAction {
    static class Request{
        int version;//version of client
        int id;//message id for client
        int user;//user id
        int flow;//flow
        long time;//when:client time
        int rw;//read or write
        //who what when where why how
        int who;//role
        int what;//action
        int when;//seconds from 1900
        int where;//view:index home config
        int why;//
        Object data;
    };
}
