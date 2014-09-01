
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 							This element is the reponse from the
 * 							doScoringCheque method
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
 *         &lt;element name="transaction" type="{http://obj.ws.payline.experian.com}transaction"/>
 *         &lt;element name="scoringCheque" type="{http://obj.ws.payline.experian.com}scoringCheque"/>
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
    "transaction",
    "scoringCheque"
})
@XmlRootElement(name = "doScoringChequeResponse")
public class DoScoringChequeResponse {

    @XmlElement(required = true)
    protected Result result;
    @XmlElement(required = true)
    protected Transaction transaction;
    @XmlElement(required = true)
    protected ScoringCheque scoringCheque;

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
     * Gets the value of the transaction property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Transaction }
     *     
     */
    public Transaction getTransaction() {
        return transaction;
    }

    /**
     * Sets the value of the transaction property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.Transaction }
     *     
     */
    public void setTransaction(Transaction value) {
        this.transaction = value;
    }

    /**
     * Gets the value of the scoringCheque property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.ScoringCheque }
     *     
     */
    public ScoringCheque getScoringCheque() {
        return scoringCheque;
    }

    /**
     * Sets the value of the scoringCheque property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.ScoringCheque }
     *     
     */
    public void setScoringCheque(ScoringCheque value) {
        this.scoringCheque = value;
    }

}
