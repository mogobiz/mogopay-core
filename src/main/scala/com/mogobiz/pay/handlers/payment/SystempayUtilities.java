/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.pay.handlers.payment;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import com.lyra.vads.ws.stub.StandardWS;
import com.lyra.vads.ws3ds.stub.ThreeDSecure;
import com.lyra.vads.ws3ds.stub.ThreeDSecure_Service;

public class SystempayUtilities {
    /**
     * Permet de verifier une signature a chaque reponse
     */
    public static Boolean checkSignature(String expectedSignature,
                                         String certificate, List<String> parameters) {
        String newSignature = makeSignature(certificate, parameters);
        return newSignature.equals(expectedSignature);
    }

    /**
     * Cree un signature pour la requete
     */
    static public final String SEPARATOR = "+";

    public static String makeSignature(String certificat,
                                       List<String> parameters) {
        StringBuilder builder = new StringBuilder();
        for (Object param : parameters) {
            if (builder.length() > 0) {
                builder.append(SEPARATOR);
            }
            if (param != null) {
                if (param instanceof Date
                        || param instanceof XMLGregorianCalendar) {
                    Date date = null;
                    if (param instanceof XMLGregorianCalendar) {
                        XMLGregorianCalendar xmlDate = (XMLGregorianCalendar) param;
                        date = xmlDate.toGregorianCalendar().getTime();
                    } else {
                        date = (Date) param;
                    }
                    SimpleDateFormat myDateFormat = new SimpleDateFormat(
                            "yyyyMMdd");
                    myDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    builder.append(myDateFormat.format(date));
                } else if (param instanceof Boolean) {
                    if ((Boolean) param) {
                        builder.append(1);
                    } else {
                        builder.append(0);
                    }
                } else {
                    builder.append(param);
                }
            }
        }
        String toSign = builder.toString() + SEPARATOR + certificat;
        return encode(toSign);
    }

    public static String encode(String src) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            byte bytes[] = src.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            byte[] sha1hash = digest.digest();
            return convertToHex(sha1hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static String convertToHex(byte[] sha1hash) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sha1hash.length; i++) {
            byte c = sha1hash[i];
            addHex(builder, (c >> 4) & 0xf);
            addHex(builder, c & 0xf);
        }
        return builder.toString();
    }

    private static void addHex(StringBuilder builder, int c) {
        if (c < 10)
            builder.append((char) (c + '0'));
        else
            builder.append((char) (c + 'a' - 10));
    }

    public static ThreeDSecure create3DSProxy(String wsdlLocation,
                                              Handler handler) {
        URL url = SystempayUtilities.class.getResource(wsdlLocation);
        ThreeDSecure_Service service = new ThreeDSecure_Service(url);
        ThreeDSecure secured = service.getThreeDSecureServicePort();
        javax.xml.ws.Binding binding = ((BindingProvider) secured).getBinding();

        ((BindingProvider) secured).getRequestContext().put(
                BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        if (handler != null) {
            List<Handler> handlerList = binding.getHandlerChain();
            handlerList.add(handler);
            binding.setHandlerChain(handlerList);
        }
        return secured;
    }

    public static StandardWS createStandardWSProxy(String wsdllocation) {
        URL url = SystempayUtilities.class.getResource(wsdllocation);
        QName qname = new QName("http://v3.ws.vads.lyra.com/", "StandardWS");
        Service service = Service.create(url, qname);
        StandardWS standardWs = service.getPort(StandardWS.class);
        ((BindingProvider) standardWs).getRequestContext().put(
                BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        return standardWs;
    }

    public static void main(String[] args) {
        System.out
                .println(encode("INTERACTIVE+25000+TEST+978+PAYMENT+SINGLE+GET+34889127+20130522164719+001645+http://www.codechic.org:8090/mogopay/systempay/done+V2+7736291283331938"));
    }
}