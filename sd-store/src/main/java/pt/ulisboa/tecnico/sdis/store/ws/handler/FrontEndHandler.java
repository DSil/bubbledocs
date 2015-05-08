package pt.ulisboa.tecnico.sdis.store.ws.handler;

import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * This SOAPHandler shows how to set/get values from headers in
 * inbound/outbound SOAP messages.
 *
 * A header is created in an outbound message and is read on an
 * inbound message.
 *
 * The value that is read from the header
 * is placed in a SOAP message context property
 * that can be accessed by other handlers or by the application.
 */
public class FrontEndHandler implements SOAPHandler<SOAPMessageContext> {

    //
    // Handler interface methods
    //
    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        try {
            if (outboundElement.booleanValue()) {
                if(smc.get("requestTag") != null) {
                    if((boolean) smc.get("requestTag")) {
                        System.out.println("Writing header in outbound SOAP message...");
                        try {
                        // get SOAP envelope
                        SOAPMessage msg = smc.getMessage();
                        SOAPPart sp = msg.getSOAPPart();
                        SOAPEnvelope se = sp.getEnvelope();
        
                        // add header
                        SOAPHeader sh = se.getHeader();
                        if (sh == null)
                            sh = se.addHeader();
        
                        // add header element (name, namespace prefix, namespace)
                        Name name = se.createName("requestTag", "rt", "http://requestTag");
                        SOAPHeaderElement element = sh.addHeaderElement(name);
        
                        // add header element value
                        boolean doTagHeader = true;
                        String value = new Boolean(doTagHeader).toString();
                        element.addTextNode(value);
                        } catch (SOAPException e) {
                            System.out.printf("Failed to add SOAP header because of %s%n", e);
                        }
                    }
                }
                else if(smc.get("newTag") != null) {
                    System.out.println("Writing header in outbound SOAP message...");
                    try {
                    // get SOAP envelope
                    SOAPMessage msg = smc.getMessage();
                    SOAPPart sp = msg.getSOAPPart();
                    SOAPEnvelope se = sp.getEnvelope();
    
                    // add header
                    SOAPHeader sh = se.getHeader();
                    if (sh == null)
                        sh = se.addHeader();
    
                    // add header element (name, namespace prefix, namespace)
                    Name name = se.createName("newTag", "nt", "http://newTag");
                    SOAPHeaderElement element = sh.addHeaderElement(name);
    
                    // add header element value
                    int tag = (int) smc.get("newTag");
                    String value = new Integer(tag).toString();
                    element.addTextNode(value);
                    } catch (SOAPException e) {
                        System.out.printf("Failed to add SOAP header because of %s%n", e);
                    }
                }

            } else {
                System.out.println("Reading header in inbound SOAP message...");

                // get SOAP envelope header
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                // check header
                if (sh == null) {
                    System.out.println("Header not found.");
                    return true;
                }

                // get first header element
                Name name = se.createName("Tag", "t", "http://Tag");
                Iterator it = sh.getChildElements(name);
                // check header element
                if (!it.hasNext()) {
                    name = se.createName("Ack", "a", "http://Ack");
                    it = sh.getChildElements(name);
                    
                    if (!it.hasNext()) {
                        System.out.println("Header element not found.");
                        return true;
                    }
                    
                    SOAPElement element = (SOAPElement) it.next();
                    
                    // get header element value
                    String valueString = element.getValue();
                    boolean value = Boolean.parseBoolean(valueString);
    
                    // print received header
                    System.out.println("Header value is " + value);
    
                    // put header in a property context
                    smc.put("Ack", value);
                    // set property scope to application client/server class can access it
                    smc.setScope("Ack", Scope.APPLICATION);
                }
                SOAPElement element = (SOAPElement) it.next();

                // get header element value
                String valueString = element.getValue();
                int value = Integer.parseInt(valueString);

                // print received header
                System.out.println("Header value is " + value);

                // put header in a property context
                smc.put("tag", value);
                // set property scope to application client/server class can access it
                smc.setScope("tag", Scope.APPLICATION);
            }
        } catch (Exception e) {
            System.out.print("Caught exception in handleMessage: ");
            System.out.println(e);
            System.out.println("Continue normal processing...");
        }

        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        System.out.println("Ignoring fault message...");
        return true;
    }

    public void close(MessageContext messageContext) {
    }

}