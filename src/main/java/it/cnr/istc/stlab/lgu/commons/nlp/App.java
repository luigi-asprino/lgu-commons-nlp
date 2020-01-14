package it.cnr.istc.stlab.lgu.commons.nlp ;

import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.lgu.commons.entitylinking.model.Mention;
import it.cnr.istc.stlab.lgu.commons.nlp.pipelines.ItaPipeline;

public class App {

	private static Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args){	
		try {
			Configurations configs = new Configurations();
			Configuration config = configs.properties("config.properties");
			logger.info(config.getString("prova"));
			
			String s = "Roma è la capitale d'Italia";
			
			ItaPipeline ip = ItaPipeline.getInstance();
			
			List<Mention> locs = ip.extractLocations(s);
			locs.forEach(m->{
				System.out.println(m.toString());
			});
			
			
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
}
