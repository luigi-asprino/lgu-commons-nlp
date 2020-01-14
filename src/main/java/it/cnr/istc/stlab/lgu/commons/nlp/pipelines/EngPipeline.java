package it.cnr.istc.stlab.lgu.commons.nlp.pipelines;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import it.cnr.istc.stlab.lgu.commons.entitylinking.model.Mention;
import it.cnr.istc.stlab.lgu.commons.nlp.Pipeline;

public class EngPipeline implements Pipeline {

	private static Logger logger = LogManager.getLogger(ItaPipeline.class);

	private static StanfordCoreNLP pipeline;
	private static EngPipeline instance;

	private EngPipeline() {
		init();
	}

	public static void init() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		pipeline = new StanfordCoreNLP(props);
	}

	public static EngPipeline getInstance() {
		logger.trace("Getting instance of English Pipeline");
		if (instance == null) {
			instance = new EngPipeline();
		}
		return instance;
	}

	public List<Mention> extractLocations(String text) {

		List<Mention> mentionedPersons = new ArrayList<>();

		// Get the original Annotation (Stanford CoreNLP)
		Annotation annotation = pipeline.process(text);

		List<CoreLabel> tokenAnnotations = new ArrayList<>(annotation.get(TokensAnnotation.class));
		for (int i = 0; i < tokenAnnotations.size(); i++) {
			CoreLabel token = tokenAnnotations.get(i);
			logger.trace(token.ner());
			if (token.ner().equals("LOCATION")) {
				int beginBefore = 0, endAfter = 0;
				String tokenBefore = null, tokenAfter = null;
				if (i > 1) {
					CoreLabel bef = tokenAnnotations.get(i - 1);
					if (bef.ner().equals("LOCATION")) {
						beginBefore = bef.beginPosition();
						tokenBefore = bef.word();
					}
				}
				if (i < tokenAnnotations.size() - 1) {
					CoreLabel aft = tokenAnnotations.get(i + 1);
					if (aft.ner().equals("LOCATION")) {
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
