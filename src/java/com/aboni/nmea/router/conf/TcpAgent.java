//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.08.28 at 06:46:07 AM CEST 
//


package com.aboni.nmea.router.conf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TcpAgent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TcpAgent">
 *   &lt;complexContent>
 *     &lt;extension base="{}AgentBase">
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" fixed="TCP" />
 *       &lt;attribute name="host" type="{http://www.w3.org/2001/XMLSchema}string" default="localhost" />
 *       &lt;attribute name="port" type="{http://www.w3.org/2001/XMLSchema}int" default="4800" />
 *       &lt;attribute name="inout" use="required" type="{}InOut" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TcpAgent")
public class TcpAgent
    extends AgentBase
{

    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "host")
    protected String host;
    @XmlAttribute(name = "port")
    protected Integer port;
    @XmlAttribute(name = "inout", required = true)
    protected InOut inout;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        if (type == null) {
            return "TCP";
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the host property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHost() {
        if (host == null) {
            return "localhost";
        } else {
            return host;
        }
    }

    /**
     * Sets the value of the host property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHost(String value) {
        this.host = value;
    }

    /**
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getPort() {
        if (port == null) {
            return  4800;
        } else {
            return port;
        }
    }

    /**
     * Sets the value of the port property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPort(Integer value) {
        this.port = value;
    }

    /**
     * Gets the value of the inout property.
     * 
     * @return
     *     possible object is
     *     {@link InOut }
     *     
     */
    public InOut getInout() {
        return inout;
    }

    /**
     * Sets the value of the inout property.
     * 
     * @param value
     *     allowed object is
     *     {@link InOut }
     *     
     */
    public void setInout(InOut value) {
        this.inout = value;
    }

}