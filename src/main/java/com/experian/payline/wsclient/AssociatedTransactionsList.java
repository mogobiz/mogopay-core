
package com.experian.payline.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 						An array of associatedTransactions
 * 					
 * 
 * <p>Java class for associatedTransactionsList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="associatedTransactionsList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="associatedTransactions" type="{http://obj.ws.payline.experian.com}associatedTransactions" maxOccurs="100" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "associatedTransactionsList", namespace = "http://obj.ws.payline.experian.com", propOrder = {
    "associatedTransactions"
})
public class AssociatedTransactionsList {

    protected List<AssociatedTransactions> associatedTransactions;

    /**
     * Gets the value of the associatedTransactions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the associatedTransactions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAssociatedTransactions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link com.experian.payline.wsclient.AssociatedTransactions }
     * 
     * 
     */
    public List<AssociatedTransactions> getAssociatedTransactions() {
        if (associatedTransactions == null) {
            associatedTransactions = new ArrayList<AssociatedTransactions>();
        }
        return this.associatedTransactions;
    }

}
