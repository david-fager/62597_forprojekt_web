package Javalin;

import brugerautorisation.data.Bruger;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java_common.rmi.IConnectionHandlerRMI;
import org.eclipse.jetty.http.HttpStatus;

import java.rmi.Naming;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Server {
    private final boolean DEBUGMODE = false;
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
            String sesID;
            if (ctx.cookie("sessionID") != null) {
                sesID = ctx.cookie("sessionID");
            } else {
                sesID = "0";
            }
            System.out.println(getTime() + "Received '" + ctx.method() + "' by Session#" + sesID + " on URL:" + ctx.url()
                    + " containing pathparams:" + ctx.pathParamMap() + " queryparams:" + ctx.queryParamMap()
                    + " formparams:" + ctx.formParamMap() + " cookies:" + ctx.cookieMap());
        });
    }

    public void webUserPaths() {

        // PAGE: MAIN (LOGIN) - skips the login screen if the user is recognised by cookie
        app.get("/", context -> {
            if (!DEBUGMODE) {
                if (!recognize(context)) {
                    context.redirect("/login");
                } else {
                    System.out.println(getTime() + "User recognized as sessionID: " + context.cookie("sessionID") + " redirecting to /menu");
                    context.redirect("/menu");
                }
            } else {
                context.redirect("/login");
            }
        });


        // PAGE: LOGIN - the login screen is rendered if the user is not recognised by cookie
        app.get("/login", context -> {
            if (!DEBUGMODE) {
                if (!recognize(context)) {
                    context.render("webapp/login.html");
                } else {
                    System.out.println(getTime() + "User recognized as sessionID: " + context.cookie("sessionID") + " redirecting to /menu");
                    context.redirect("/menu");
                }
            } else {
                context.render("webapp/login.html");
            }
        });

        // BUTTON: LOGIN - checks credentials and gives id via cookie if success
        app.get("/login/:username", context -> {
            int sesID;
            if (!recognize(context)) {
                sesID = javaprogram.informConnect();
            } else {
                sesID = Integer.parseInt(context.cookie("sessionID"));
            }

            String username = context.pathParam("username");
            String password = context.queryParam("password");

            boolean success = javaprogram.login(sesID, username, password);
            if (success) {
                System.out.println(getTime() + "Login success");
                context.cookieStore("galgelegCookieStore");
                context.cookie("sessionID", String.valueOf(sesID));
                context.status(HttpStatus.ACCEPTED_202);
                context.render("webapp/startside.html");
            } else {
                System.out.println(getTime() + "Login failed");
                context.status(HttpStatus.UNAUTHORIZED_401);
            }
        });

        // PAGE: FORGOT PASSWORD - loads/renders the page for forgotten password
        app.get("/login/forgot", context -> {
            context.render("webapp/glemtLogin.html");
        });

        // BUTTON: FORGOT PASSWORD - sends the request for forgotten password
        app.get("/login/forgot/:username", context -> {
            String username = context.pathParam("username");
            String message = context.queryParam("message");
            if (message == null) {
                message = "";
            }

            boolean success = javaprogram.forgotPassword(username, message);
            if (success) {
                context.status(HttpStatus.OK_200);
            } else {
                context.status(HttpStatus.SERVICE_UNAVAILABLE_503);
            }
        });


        // PAGE: MENU - if the user is authorized to view it, then it renders the menu page otherwise a 401 error
        app.get("/menu", context -> {
            if (!recognize(context)) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
                return;
            }

            context.render("webapp/startside.html");
        });


        // PAGE: HANGMAN MODE - a page to show the game modes
        app.get("/hangman", context -> {
            if (!recognize(context)) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
                return;
            }

            context.render("webapp/modeSpil.html");
        });

        // PAGE: HANGMAN GAME
        app.get("/hangman/:mode", context -> {
            if (!recognize(context)) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
                return;
            }

            int sesID = Integer.parseInt(context.cookie("sessionID"));
            boolean success = false;

            String mode = context.pathParam("mode");
            if (mode.equals("dr")) {
                success = javaprogram.startGame(sesID, 1);
                if (success) {
                    context.render("webapp/spil.html");
                }
            } else if (mode.equals("standard")) {
                success = javaprogram.startGame(sesID, 2);
                if (success) {
                    context.render("webapp/spil.html");
                }
            } else {
                context.status(HttpStatus.BAD_REQUEST_400).result("<h1>400 Bad Request</h1>Check the request vs the servers expectation.").contentType("text/html");
            }

            if (!success) {
                context.status(HttpStatus.SERVICE_UNAVAILABLE_503).result("<h1>503 Service Unavailable</h1>The server experienced and error. Contact system admin").contentType("text/html");
            }
        });

        // CALL: GAMEINFO - collect and receive the info about the game
        app.get("/hangman/:mode/info", context -> {
            if (!recognize(context)) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
                return;
            }

            int sesID = Integer.parseInt(context.cookie("sessionID"));
            String visibleWord = javaprogram.getVisibleWord(sesID);
            String actualWord = javaprogram.getWord(sesID);
            String numberWrongGuesses = String.valueOf(javaprogram.numberWrongGuesses(sesID));
            String isGameOver = String.valueOf(javaprogram.isGameOver(sesID));
            String didPlayerWin = String.valueOf(javaprogram.didPlayerWin(sesID));

            // Build the string of used letters (a, b, c)
            ArrayList<String> letters = javaprogram.getUsedLetters(sesID);
            String usedletters = "";
            if (letters.size() > 0) {
                for (int i = 0; i < letters.size() - 1; i++) {
                    usedletters += (letters.get(i) + ", ");
                }
                usedletters += letters.get(letters.size() - 1);
            }

            String[] info = {visibleWord, usedletters, actualWord, numberWrongGuesses, isGameOver, didPlayerWin};
            context.json(info);
        });

        // PAGE: HANGMAN RESULT - gets the result of the game
        app.get("/hangman/:mode/result", context -> {
            if (!recognize(context)) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
                return;
            }

            context.render("webapp/endGame.html");
        });

        // BUTTON: GUESS -
        app.get("/hangman/:mode/guess/:letter", context -> {
            if (!recognize(context)) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
                return;
            }

            int sesID = Integer.parseInt(context.cookie("sessionID"));
            String guess = context.pathParam("letter");
            boolean success = javaprogram.guessLetter(sesID, guess);
            if (success) {
                System.out.println(getTime() + "User successfully guessed on: " + guess);
                context.status(HttpStatus.ACCEPTED_202);
            } else {
                context.status(HttpStatus.CONFLICT_409);
            }
        });


        // PAGE: ACCOUNT - a page to view account info and send a change password request
        app.get("/account", context -> {
            if (!recognize(context)) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
                return;
            }

            context.render("webapp/kontoIndstillinger.html");
        });

        // CALL: ACCOUNT INFO - get the information on the given account
        app.get("/account/info", context -> {
            if (!recognize(context)) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
                return;
            }

            int sesID = Integer.parseInt(context.cookie("sessionID"));
            Bruger fullAccount = javaprogram.getFullUser(sesID);
            Bruger pubAccount = javaprogram.getPublicUser(sesID);
            Bruger[] users = {fullAccount, pubAccount};
            context.json(users);
        });

        // BUTTON: CHANGE PASSWORD REQUEST - the path to send a form to change password
        app.get("/account/changePassword/:oldPassword", context -> {
            if (!recognize(context)) {
                context.status(HttpStatus.UNAUTHORIZED_401).result("<h1>401 Unauthorized</h1>You are not authorized to see this page.").contentType("text/html");
                return;
            }

            int sesID = Integer.parseInt(context.cookie("sessionID"));
            String oldPassword = context.pathParam("oldPassword");
            String newPassword = context.queryParam("newPassword");

            Bruger bruger = javaprogram.changePassword(sesID, oldPassword, newPassword);
            if (bruger != null) {
                context.removeCookie("sessionID", "/");
                context.status(HttpStatus.ACCEPTED_202);
            } else {
                context.status(HttpStatus.SERVICE_UNAVAILABLE_503);
            }
        });

    }

    private boolean recognize(Context context) {
        int sesID = -1;
        if (context.cookie("sessionID") != null) {
            try {
                sesID = Integer.parseInt(context.cookie("sessionID"));
                if (javaprogram.idRecognized(sesID)) {
                    System.out.println(getTime() + "Session#" + sesID + " recognized");
                    return true;
                }
            } catch (Exception e) {}
        }
        if (sesID > 0) {
            System.out.println(getTime() + "Session#" + sesID + " not recognized");
        }
        return false;
    }

}