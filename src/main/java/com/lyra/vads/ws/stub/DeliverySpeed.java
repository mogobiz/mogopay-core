
package com.lyra.vads.ws.stub;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deliverySpeed.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="deliverySpeed">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="STANDARD"/>
 *     &lt;enumeration value="EXPRESS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "deliverySpeed")
@XmlEnum
public enum DeliverySpeed {

    STANDARD,
    EXPRESS;

    public String value() {
        return name();
    }

    public static DeliverySpeed fromValue(String v) {
        return valueOf(v);
    }

}
