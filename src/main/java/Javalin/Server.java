package Javalin;

import io.javalin.Javalin;
import java_common.rmi.IConnectionHandlerRMI;
import org.eclipse.jetty.http.HttpStatus;

import java.rmi.Naming;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Server {
    private Javalin app = null;
    private IConnectionHandlerRMI javaprogram = null;
    private DateFormat df = new SimpleDateFormat("[dd-MM-yyyy HH:mm:ss] ");

    private String getTime() {
        return df.format(Calendar.getInstance().getTimeInMillis());
    }

    public void setupJavalin() {
        if (app != null) {
            return;
        }

        // This is the connection via RMI to the javaprogram
        try {
            javaprogram = (IConnectionHandlerRMI) Naming.lookup("rmi://localhost:9920/hangman_local");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Starts the server
        app = Javalin.create(javalinConfig -> javalinConfig.addStaticFiles("webapp")).start(42069);

        // This happens before every call to the REST backend
        app.before(ctx -> {
            int sesID;
            if (ctx.cookieStore("sessionID") != null) {
                sesID = ctx.cookieStore("sessionID");
            } else {
                sesID = 0;
            }
            System.out.println(getTime() + "Received '" + ctx.method() + "' by Session#" + sesID + " on URL:" + ctx.url()
                    + " containing pathparams:" + ctx.pathParamMap() + " queryparams:" + ctx.queryParamMap()
                    + " formparams:" + ctx.formParamMap() + " cookies:" + ctx.cookieMap());
        });
    }

    public void webUserPaths() {

        // The login page - gives the session id via a cookie
        app.get("/", context -> {
            int sesID;
            if (context.cookieStore("sessionID") != null) {
                sesID = context.cookieStore("sessionID");
            } else {
                sesID = javaprogram.informConnect();
            }
            context.cookieStore("sessionID", sesID);
            context.render("webapp/index.html");
        });

        // The path for when user clicks the 'login' button
        app.get("/login/:username", context -> {
            if (context.cookieStore("sessionID") == null) {
                context.status(HttpStatus.BAD_REQUEST_400);
                return;
            }

            int sesID = context.cookieStore("sessionID");
            String username = context.pathParam("username");
            String password = context.queryParam("password");
            boolean success = javaprogram.login(sesID, username, password);
            if (success) {
                System.out.println(getTime() + "login success");
                context.status(HttpStatus.OK_200);
            } else {
                System.out.println(getTime() + "login failed");
                context.status(HttpStatus.UNAUTHORIZED_401);
            }
        });

        // For an authorized user to see the menu
        app.get("/menu", context -> {
            if (context.cookieStore("sessionID") == null) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
                return;
            }

            context.render("webapp/index2.html");
        });

        //
        app.get("/hangman", context -> {

        });

        //
        app.get("/account", context -> {

        });

    }

    // Examples of how to use the javascript fetch and javalin app
    public void examples() {
        // A test GET to send a message with REST
        String hej = "Hej fra java-delen (javalin)";
        app.get("/hej", context -> {
            //String temp = javaprogram.getWord(0);
            context.json(hej);
        });

        // A test GET to change site
        app.get("/send", context -> {
            System.out.println("Rendered index2.html");
            context.render("webapp/index2.html");
        });

        // A test to GET with parameters
        app.get("/send/:brugernavn", context -> {
            String dab = context.pathParam("brugernavn");
            String dab2 = context.queryParam("adgangskode");
            if (dab != null) {
                System.out.println(dab);
            }
            if (dab2 != null) {
                System.out.println(dab2);
            }
        });
    }

}