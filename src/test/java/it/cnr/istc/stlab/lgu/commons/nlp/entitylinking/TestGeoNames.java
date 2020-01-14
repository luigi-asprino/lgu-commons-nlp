package it.cnr.istc.stlab.lgu.commons.nlp.entitylinking ;

import java.io.IOException;

import org.junit.Test;

public class TestGeoNames{
	
	@Test
	public void test() {
		try {
			Geonames.getGeolocalizations("Naples").forEach(e->{
				System.out.println(e.toString());
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
  
}
