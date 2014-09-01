
package com.lyra.vads.ws.stub;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for custStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="custStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="PRIVATE"/>
 *     &lt;enumeration value="COMPANY"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "custStatus")
@XmlEnum
public enum CustStatus {

    PRIVATE,
    COMPANY;

    public String value() {
        return name();
    }

    public static CustStatus fromValue(String v) {
        return valueOf(v);
    }

}
