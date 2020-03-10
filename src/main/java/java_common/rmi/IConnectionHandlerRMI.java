package java_common.rmi;

import java.util.ArrayList;
import brugerautorisation.data.Bruger;

@SuppressWarnings("NonAsciiCharacters")
public interface IConnectionHandlerRMI extends java.rmi.Remote {

    boolean login(int sesID, String username, String password) throws java.rmi.RemoteException;

    void startGame(int sesID, int i) throws Exception;

    boolean isGameOver(int sesID) throws java.rmi.RemoteException;

    boolean guessLetter(int sesID, String letter) throws java.rmi.RemoteException;

    String getVisibleWord(int sesID) throws java.rmi.RemoteException;

    ArrayList<String> getUsedLetters(int sesID) throws java.rmi.RemoteException;

    String getWord(int sesID) throws java.rmi.RemoteException;

    int informConnect() throws java.rmi.RemoteException;
    
    void informDisconnect(int clientID) throws java.rmi.RemoteException;

    boolean didPlayerWin(int sesID) throws java.rmi.RemoteException;

    Bruger getFullUser(int sesID, String password) throws java.rmi.RemoteException;

    Bruger changePassword(int sesID, String oldPassword, String newPassword) throws java.rmi.RemoteException;

    void forgotPassword(int sesID, String message) throws java.rmi.RemoteException;

    Bruger getPublicUser(int sesID) throws java.rmi.RemoteException;

}
