import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


public class DomainDriverDesign {

    enum Core{
        ActorOrRole("角色")
        ,CommandOrAction("命令/动作")
        ,QueryModel("查询模型")
        ,AggregateOrBusinessRule("聚合/业务规则约束")
        ,Domain("领域")
        ,DomainEvent("领域事件")
        ,ExternalSystem("外部系统")
        ;
        public final String desc;
        Core(String d){
            desc = d;
        }
    }
    enum Process{
        ListAllFollow,ListAllDomainEvent,FindCommandByDomainEventOrOtherCommand
        ,AggregationIsFullThenDoneIsNotFullThenReFindMoreCommandAndDomainEvent
    }
}
class Frontend{
    enum CommandType{
        NULL
    }
    interface Command{
        CommandType type();
        Command NULL = () -> CommandType.NULL;
    }
    interface CommandDecoder{
        default CommandType type(){ return CommandType.NULL;}
        Command decode(Object req);
        CommandDecoder NULL = req-> Command.NULL;
    }
    interface Condition{
        default CommandType type(){ return CommandType.NULL;}
        boolean isOk(Command cmd);

        default void lock(){}
        default boolean tryLock(){
            return true;
        }
        default void unlock(){}
        Condition NULL = cmd-> true;
    }
    interface Aggregation{
        default CommandType type(){return CommandType.NULL;}
        void handle(Command cmd);
        Aggregation NULL = (cmd)->{};
    }
    static CommandType from(int req){ return CommandType.NULL;}
    static CommandDecoder from(CommandType type){
        return CommandDecoder.NULL;
    }
    static Condition fromForCond(CommandType type){
        return Condition.NULL;
    }
    static Aggregation fromForAg(CommandType type){
        return Aggregation.NULL;
    }
    void api(Object req){
        CommandType type = from(0);
        CommandDecoder decoder = from(type);
        Command cmd = decoder.decode(req);
        Condition condition = fromForCond(type);
        if(!condition.isOk(cmd))
            return;
        Aggregation ag = fromForAg(type);
        ag.handle(cmd);
    }
    void apiWithLock(Object req){
        CommandType type = from(0);
        CommandDecoder decoder = from(type);
        Command cmd = decoder.decode(req);
        Condition condition = fromForCond(type);
        condition.lock();
        try{
            if(!condition.isOk(cmd))  return;
            fromForAg(type).handle(cmd);
        }finally {
            condition.unlock();
        }

    }
    void apiWithTryLock(Object req){
        CommandType type = from(0);
        CommandDecoder decoder = from(type);
        Command cmd = decoder.decode(req);
        Condition condition = fromForCond(type);
        if(!condition.tryLock()) return;
        try{
            if(!condition.isOk(cmd)) return;
            fromForAg(type).handle(cmd);
        }finally {
            condition.unlock();
        }
    }

    static void sleep(long l, Consumer<InterruptedException> c){
        if(l<1) return;
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
            if(Objects.nonNull(c))
                c.accept(e);
        }
    }
    static void sleep(long l){sleep(l,null);}

}
class Backend{
    interface Domain{}
    enum DomainEventType{

    }
    interface DomainEvent{
        DomainEventType type();
    }
    interface DomainEventListener{
        void onEvent(DomainEvent e);
    }
    interface DomainEventDispatcher{
        default void on(DomainEventType type,DomainEventListener listener){}
        default void off(DomainEventType type,DomainEventListener listener){}
        void emit(DomainEvent e);
    }
    static DomainEventDispatcher getInstance(){
        return (e)->{};
    }

    void apiWithEvent(Object req){
        ConditionEvent event = new ConditionEvent(req);
        ConditionEventDispatcher dispatcher = new ConditionEventDispatcher();
        if(!dispatcher.check(event))
            return;
        //dispatch events
    }
}
class ConditionEvent{
    public ConditionEvent(Object req) {
    }
}
class ConditionListener{
    public boolean pass(ConditionEvent evt){ return true;}
}
class ConditionEventDispatcher{
    List<ConditionListener> listeners = new LinkedList<>();
    boolean check(ConditionEvent event){
        for (ConditionListener listener : listeners) {
            if(!listener.pass(event))
                return false;
        }
        return true;
    }
}