package pt.tecnico.bubbledocs.service.remote;

import pt.ist.fenixframework.FenixFramework;
import pt.tecnico.bubbledocs.domain.Session;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.CannotLoadDocumentException;
import pt.tecnico.bubbledocs.exception.CannotStoreDocumentException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.ServiceLookupException;
import pt.tecnico.bubbledocs.exception.UnauthorizedUserException;
import pt.ulisboa.tecnico.sdis.store.ws.CapacityExceeded_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocDoesNotExist_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;
import pt.ulisboa.tecnico.sdis.store.ws.UserDoesNotExist_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.client.StoreClient;

public class StoreRemoteServices {

    private StoreClient client;
    final private String uddiURL = "http://localhost:8081";
    final private String serviceName = "SD-STORE";

    public StoreRemoteServices() {
        try {
            // FIXME: ... Why? Why? Why? Hopefully will be changed.
            StoreClient client = new StoreClient(uddiURL, serviceName, 5, 3, 3); // because.
            this.client = client;
        } catch (Exception e) {
            // We are very optimistic. This will never happen :)
            e.printStackTrace();
            throw new ServiceLookupException();
        }
    }

    public void storeDocument(String username, String docName, byte[] document) throws CannotStoreDocumentException,
            RemoteInvocationException {

        if (username == null || docName == null)
            throw new CannotStoreDocumentException();

        DocUserPair pair = new DocUserPair();
        pair.setDocumentId(docName);
        pair.setUserId(username);
        try {
            client.store(pair, document, getTicketForUser(username), getKeyForUser(username));
        } catch (CapacityExceeded_Exception e) {
            throw new CannotStoreDocumentException();
        } catch (DocDoesNotExist_Exception e) {
            try {
                client.createDoc(pair, getTicketForUser(username), getKeyForUser(username));
            } catch (Exception e1) {
                throw new CannotStoreDocumentException();
            }
        } catch (UserDoesNotExist_Exception e) {
            throw new CannotStoreDocumentException();
        } catch (NullPointerException e) {
            throw new CannotStoreDocumentException();
        }

    }

    public byte[] loadDocument(String username, String docName) throws CannotLoadDocumentException, RemoteInvocationException {
        if (username == null || docName == null)
            throw new CannotStoreDocumentException();

        DocUserPair pair = new DocUserPair();
        pair.setDocumentId(docName);
        pair.setUserId(username);

        try {
            return client.load(pair, getTicketForUser(username), getKeyForUser(username));
        } catch (DocDoesNotExist_Exception e) {
            throw new CannotLoadDocumentException();
        } catch (UserDoesNotExist_Exception e) {
            throw new CannotLoadDocumentException();
        } catch (NullPointerException e) {
            throw new CannotLoadDocumentException();
        }

    }

    private String getTicketForUser(String username) {
        User user = FenixFramework.getDomainRoot().getBubbleDocs().getUserByUsername(username);
        Session session = user.getSession();
        if (session == null)
            throw new UnauthorizedUserException();
        return session.getTicket();
    }

    private String getKeyForUser(String username) {
        User user = FenixFramework.getDomainRoot().getBubbleDocs().getUserByUsername(username);
        Session session = user.getSession();
        if (session == null)
            throw new UnauthorizedUserException();
        return session.getKey();
    }
}