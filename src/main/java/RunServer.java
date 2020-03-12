import Javalin.Server;

public class RunServer {

    public static void main(String[] args) {
        Server server = new Server();
        server.setupJavalin();
        server.webUserPaths();

        // Initialises the examples of javascript and javalin communication
        server.examples();
    }

}
