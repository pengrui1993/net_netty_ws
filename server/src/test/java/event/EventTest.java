package event;
/*
#include<iostream>
#include<exception>
enum class EventType{
    Null
};
struct Obj{
    virtual void hello(){};
};
struct Event{
    virtual EventType type(){return EventType::Null;};
};
struct NullEventHandler{
    virtual void hello() const { std::cout << "nothing" << std::endl;}
};
struct NullEvent:public Event,public NullEventHandler{
    virtual void hello() const override{ std::cout << "null hello" << std::endl;};
};
void handle(Event& e){
    auto& evt = static_cast<NullEvent&>(e);
    auto& handler = static_cast<NullEventHandler&>(evt);
    handler.hello();
}
void dynamic(Event& e){
    auto& handler = dynamic_cast<NullEventHandler&>(e);
    handler.hello();
}
void no(Event& e){
    auto& handler = dynamic_cast<Obj&>(e);
    handler.hello();
}

int main(){
    auto ex = [](){
        std::cerr << "error in callback" << std::endl;
        std::abort();
    };
    auto old = std::set_terminate(ex);
    //old();//terminate called without an active exception
    NullEvent evt;
    handle(evt);
    dynamic(evt);
    try{
        no(evt);
    }
    // catch(std::bad_cast& e){}
    catch(std::exception& e){
        std::cout << "error with exception" << e.what() << std::endl;
        throw e;
    }catch(...){
        std::cerr << "unknown catched" << std::endl;
    }
    return 0;
}
 */

public class EventTest {
    public static void main(String[] args) {
        Event e = new NullEvent();
        handle(e);//null hello in java
    }
    static void handle(Event e){
        NullHandler.class.cast(e).hello();
    }
}
enum EventType{
    Null
}
class Event{
    EventType type(){return EventType.Null;}
}
interface NullHandler{
    void hello();
}
class NullEvent extends Event implements NullHandler{
    @Override
    public void hello() {
        System.out.println("null hello in java");
    }
}