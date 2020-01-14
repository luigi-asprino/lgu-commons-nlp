package it.cnr.istc.stlab.lgu.commons.nlp.pipelines;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import it.cnr.istc.stlab.lgu.commons.entitylinking.model.Mention;
import it.cnr.istc.stlab.lgu.commons.nlp.Pipeline;

public class ItaPipeline implements Pipeline {

	private static ItaPipeline pipeline;
	private TintPipeline pipelineTint;
	private static Logger logger = LoggerFactory.getLogger(ItaPipeline.class);

	private ItaPipeline() {
		try {
			init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void init() throws IOException {

		// Initialize the Tint pipeline
		logger.trace("Init tint pipeline");
		pipelineTint = new TintPipeline();

		// Load the default properties
		pipelineTint.loadDefaultProperties();
		pipelineTint.setProperty("annotators", "ita_toksent,pos,ita_morpho,ita_lemma,ner");

		// Load the models
		logger.trace("Loading TINT models");
		pipelineTint.load();
		logger.trace("loaded");

	}

	public static ItaPipeline getInstance() {
		logger.trace("Getting instance of Ita Pipeline");
		if (pipeline == null) {
			pipeline = new ItaPipeline();
		}
		return pipeline;
	}

	public List<Mention> extractPersons(String text) {

		List<Mention> mentionedPersons = new ArrayList<>();

		// Get the original Annotation (Stanford CoreNLP)
		Annotation annotation = pipelineTint.runRaw(text);

		List<CoreLabel> tokenAnnotations = new ArrayList<>(annotation.get(TokensAnnotation.class));
		for (int i = 0; i < tokenAnnotations.size(); i++) {
			CoreLabel token = tokenAnnotations.get(i);
			if (token.ner().equals("PER")) {
				int beginBefore = 0, endAfter = 0;
				String tokenBefore = null, tokenAfter = null;
				if (i > 1) {
					CoreLabel bef = tokenAnnotations.get(i - 1);
					if (bef.ner().equals("PER")) {
						beginBefore = bef.beginPosition();
						tokenBefore = bef.word();
					}
				}
				if (i < tokenAnnotations.size() - 1) {
					CoreLabel aft = tokenAnnotations.get(i + 1);
					if (aft.ner().equals("PER")) {
						endAfter = aft.endPosition();
						tokenAfter = aft.word();
					}
				}
				int beg = token.beginPosition();
				int after = token.endPosition();
				StringBuilder sb = new StringBuilder();
				if (tokenBefore != null) {
					beg = beginBefore;
					sb.append(tokenBefore);
					sb.append(' ');
				}
				sb.append(token.word());
				if (tokenAfter != null) {
					sb.append(' ');
					after = endAfter;
					sb.append(tokenAfter);
				}
				Mention m = new Mention(beg, after, sb.toString());
				if (!mentionedPersons.contains(m)) {
					mentionedPersons.add(m);
				}
			}
		}

		return mentionedPersons;
	}

	public List<Mention> extractLocations(String text) {

		List<Mention> mentionedPersons = new ArrayList<>();

		// Get the original Annotation (Stanford CoreNLP)
		Annotation annotation = pipelineTint.runRaw(text);

		List<CoreLabel> tokenAnnotations = new ArrayList<>(annotation.get(TokensAnnotation.class));
		for (int i = 0; i < tokenAnnotations.size(); i++) {
			CoreLabel token = tokenAnnotations.get(i);
			if (token.ner().equals("LOC")) {
				int beginBefore = 0, endAfter = 0;
				String tokenBefore = null, tokenAfter = null;
				if (i > 1) {
					CoreLabel bef = tokenAnnotations.get(i - 1);
					if (bef.ner().equals("LOC")) {
						beginBefore = bef.beginPosition();
						tokenBefore = bef.word();
					}
				}
				if (i < tokenAnnotations.size() - 1) {
					CoreLabel aft = tokenAnnotations.get(i + 1);
					if (aft.ner().equals("LOC")) {
						endAfter = aft.endPosition();
						tokenAfter = aft.word();
					}
				}
				int beg = token.beginPosition();
				int after = token.endPosition();
				StringBuilder sb = new StringBuilder();
				if (tokenBefore != null) {
					beg = beginBefore;
					sb.append(tokenBefore);
					sb.append(' ');
				}
				sb.append(token.word());
				if (tokenAfter != null) {
					sb.append(' ');
					after = endAfter;
					sb.append(tokenAfter);
				}
				Mention m = new Mention(beg, after, sb.toString());
				if (!mentionedPersons.contains(m)) {
					mentionedPersons.add(m);
				}
			}
		}

		return mentionedPersons;
	}

}
