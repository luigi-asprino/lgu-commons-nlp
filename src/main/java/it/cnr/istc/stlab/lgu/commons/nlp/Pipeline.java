package it.cnr.istc.stlab.lgu.commons.nlp;

import java.util.List;

import it.cnr.istc.stlab.lgu.commons.entitylinking.model.Mention;
import it.cnr.istc.stlab.lgu.commons.nlp.pipelines.EngPipeline;
import it.cnr.istc.stlab.lgu.commons.nlp.pipelines.ItaPipeline;

public interface Pipeline {

	public static Pipeline getPipeline(Lang l) {
		switch (l) {
		case EN:
		default:
			return EngPipeline.getInstance();
		case IT:
			return ItaPipeline.getInstance();
		}
	}

	public static List<Mention> extractLocations(String text, Lang l) {
		switch (l) {
		case EN:
		default:
			return EngPipeline.getInstance().extractLocations(text);
		case IT:
			return ItaPipeline.getInstance().extractLocations(text);
		}

	}

}
