package com.aboni.nmea.router.conf;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.File;

public class ConfParser {

	private Router r;
	
	public ConfParser() {
		// nothing to initialize
	}
	
	@SuppressWarnings("unchecked")
	public ConfParser init(String f) throws MalformedConfigurationException {
		try {
			File file = new File(f);
			JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			r = ((JAXBElement<Router>) jaxbContext.createUnmarshaller().unmarshal(file)).getValue();
		} catch (JAXBException e) {
		  	throw new MalformedConfigurationException(e);
	  	}
		return this;
	}
	
	public Router getConf() {
		return r;
	}

}
