
package com.lyra.vads.ws.stub;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deliveryType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="deliveryType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="RECLAIM_IN_SHOP"/>
 *     &lt;enumeration value="RELAY_POINT"/>
 *     &lt;enumeration value="RECLAIM_IN_STATION"/>
 *     &lt;enumeration value="PACKAGE_DELIVERY_COMPANY"/>
 *     &lt;enumeration value="ETICKET"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "deliveryType")
@XmlEnum
public enum DeliveryType {

    RECLAIM_IN_SHOP,
    RELAY_POINT,
    RECLAIM_IN_STATION,
    PACKAGE_DELIVERY_COMPANY,
    ETICKET;

    public String value() {
        return name();
    }

    public static DeliveryType fromValue(String v) {
        return valueOf(v);
    }

}
