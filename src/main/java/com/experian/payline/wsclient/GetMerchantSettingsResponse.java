
package com.experian.payline.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 							This element is the response from the
 * 							getMerchantSettings method
 * 						
 * 
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://obj.ws.payline.experian.com}result"/>
 *         &lt;element name="listPointOfSell">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="pointOfSell" type="{http://obj.ws.payline.experian.com}pointOfSell" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "result",
    "listPointOfSell"
})
@XmlRootElement(name = "getMerchantSettingsResponse")
public class GetMerchantSettingsResponse {

    @XmlElement(required = true)
    protected Result result;
    @XmlElement(required = true)
    protected ListPointOfSell listPointOfSell;

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Result }
     *     
     */
    public Result getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.Result }
     *     
     */
    public void setResult(Result value) {
        this.result = value;
    }

    /**
     * Gets the value of the listPointOfSell property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.GetMerchantSettingsResponse.ListPointOfSell }
     *     
     */
    public ListPointOfSell getListPointOfSell() {
        return listPointOfSell;
    }

    /**
     * Sets the value of the listPointOfSell property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.GetMerchantSettingsResponse.ListPointOfSell }
     *     
     */
    public void setListPointOfSell(ListPointOfSell value) {
        this.listPointOfSell = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="pointOfSell" type="{http://obj.ws.payline.experian.com}pointOfSell" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "pointOfSell"
    })
    public static class ListPointOfSell {

        protected List<PointOfSell> pointOfSell;

        /**
         * Gets the value of the pointOfSell property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the pointOfSell property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPointOfSell().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link com.experian.payline.wsclient.PointOfSell }
         * 
         * 
         */
        public List<PointOfSell> getPointOfSell() {
            if (pointOfSell == null) {
                pointOfSell = new ArrayList<PointOfSell>();
            }
            return this.pointOfSell;
        }

    }

}
