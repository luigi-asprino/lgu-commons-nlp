# ## TODO parametri

import codecs
import logging
import os
import sys

import faiss  # make faiss available
from gensim import corpora
from gensim import models
from gensim import similarities
from gensim.corpora.textcorpus import TextCorpus
from gensim.models import LsiModel
from gensim.models import Phrases
from gensim.models.phrases import Phraser
from gensim.test.utils import common_corpus, common_dictionary, get_tmpfile
from gensim.test.utils import datapath, get_tmpfile
from sklearn.preprocessing import normalize
from stop_words import get_stop_words, AVAILABLE_LANGUAGES

import numpy as np

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

logging.getLogger("gensim").setLevel(logging.INFO)
logger = logging.getLogger(__name__)


class CorpusPlain(object):
     
    def __init__(self, corpus_tokenized_file, sep):
        self.corpus_tokenized_file = corpus_tokenized_file
        self.sep = sep
         
    def __iter__(self):
        for line in open(self.corpus_tokenized_file):
            # assume there's one document per line, tokens separated by whitespace
            yield line[line.index(sep) + 4:].lower().rstrip('\n').split(' ')


class CorpusBigrams(object):
     
    def __init__(self, corpus_tokenized_file, d, bigrams, sep):
        self.corpus_tokenized_file = corpus_tokenized_file
        self.dictionary = d
        self.bigrams = bigrams
        self.sep = sep
         
    def __iter__(self):
        for line in open(self.corpus_tokenized_file):
            # assume there's one document per line, tokens separated by whitespace
            yield self.dictionary.doc2bow(bigrams[line[line.index(sep) + 4:].lower().rstrip('\n').split(' ')])


class Corpus(object):
     
    def __init__(self, corpus_tokenized_file, d, sep):
        self.corpus_tokenized_file = corpus_tokenized_file
        self.dictionary = d
        self.sep = sep
         
    def __iter__(self):
        for line in open(self.corpus_tokenized_file):
            # assume there's one document per line, tokens separated by whitespace
            yield self.dictionary.doc2bow(line[line.index(sep) + 4:].lower().rstrip('\n').split(' '))


if __name__ == '__main__':
    
    parser = argparse.ArgumentParser(description='Compute the similarity among a list entities.')
    
    parser.add_argument('corpus', metavar='C', help='The file containing the corpus to analyse. The corpus consists in a single text file where each line of the file stores a document with the following format <DOC_IC> ##\t<DOD_CONTENT>')
    parser.add_argument('entities', metavar='E', help='The file containing the list of doc ids (i.e. entities to analyse).')
    parser.add_argument('out', metavar='O', help='The output folder.')
    parser.add_argument('dimensionality', metavar='d', type=int, help='The dimensionality of the embeddings.')
    parser.add_argument('threshold', metavar='t', type=int, help='The number of similar entities to take for each entity.')
    
    
    corpus_file = "/Users/lgu/Desktop/tempPSS/walks"
    entity_to_analyse_file = "/Users/lgu/Desktop/tempPSS/entities"
    folder_out = "/Users/lgu/Desktop/tempPSS/lsi/"
    num_of_topics = 300
    num_of_similar_docs = 40
    
    # Create the dictionary
    dictionary = corpora.Dictionary()
    
    # Create a stopword list
    stop = set(["$", "\"", "en", ":", ".", ">", "<", "^", "/", "@", "#", "(", ")", "-", "%", ",", "[", "]", "{", "}", ";", "`", "!"])
    
    # # Importing stopwords for available languages https://github.com/Alir3z4/python-stop-words
    for l in AVAILABLE_LANGUAGES:
        for sw in get_stop_words(l):
            stop.add(sw)
    
    logger.info("Number of Stopwords {}".format(len(stop)))    
    
    if(not os.path.exists(folder_out)):
        os.mkdir(folder_out)
    
    dictionary_file = folder_out + "walks_dictionary"
    corpusMM_file = folder_out + "corpusMM"
    lsi_model_file = folder_out + "lsi_model"
    lsi_corpus_file = folder_out + "lsi_corpus"
    bigrams_model_file = folder_out + "bigram_model"
    entity_list_file = folder_out + "entity_list"
    compute_bigrams = False
    
    progress_cnt = 5
    sep = " ##\t"
    tokens_no_belove = 5 
    tokens_no_above = 0.9
    
    
    file_out = folder_out + "similarities"
    
    if os.path.isfile(dictionary_file):
        dictionary = dictionary.load(dictionary_file)
        if(compute_bigrams):
            bigrams = Phrases.load(bigrams_model_file)
        logger.info("Dictionary loaded!")
    else:
        
        # Compute bigrams
        if(compute_bigrams):
            logger.info("Computing bigrams")
            bigrams = Phrases(CorpusPlain(corpus_file, sep))
            bigrams.save(bigrams_model_file)
            logger.info("Bigrams computed")
        
        # Build Dictionary
        fp_out = open(entity_list_file, "w") 
        with codecs.open(corpus_file, 'r') as dump_file: 
            current_line_number = 0
            line = dump_file.readline()
            while line:
                if (current_line_number > 0 and current_line_number % progress_cnt == 0):
                    logger.info("Tokenized {}".format(current_line_number))
                
                start_index = line.index(sep)
                fp_out.write(line[:start_index] + "\n")
                tokens = line[start_index + 4:].rstrip('\n').lower().split()
                
                doc = [t for t in tokens if t not in stop]
                
                if(compute_bigrams):
                    dictionary.add_documents([bigrams[doc]])
                else:
                    dictionary.add_documents([doc])
                
                current_line_number += 1
                
                line = dump_file.readline()
                
            dump_file.close()
        
        dictionary.filter_extremes(no_above=tokens_no_above)
        logger.info("Extremes filtered")
        dictionary.save(dictionary_file)
        logger.info("Dictionary saved")
    
    if os.path.isfile(lsi_corpus_file):
        corpus_lsi = corpora.MmCorpus(lsi_corpus_file)
        entities_all = [line.rstrip('\n') for line in open(entity_list_file)]
        entities_to_analyse_ids = [entities_all.index(line.rstrip('\n')) for line in open(entity_to_analyse_file)]
        
        m = np.zeros((len(entities_to_analyse_ids), num_of_topics), dtype="float32")
        for i in range(0, len(entities_to_analyse_ids)):
            a = [e for i, e in corpus_lsi[entities_to_analyse_ids[i]]]
            if(len(a) == num_of_topics):
                m[i] = np.array(a, dtype=float)
            else:
                logger.error(f"Shape not 300 for index {i}")
        index = faiss.IndexFlatIP(num_of_topics)  # build the index cosine distance
        index.add(m)
        logger.info("Corpus indexed in faiss")
        logger.info(f"Numer of documents in m: {len(m)}")
       
        D, I = index.search(m, num_of_similar_docs)  # actual search 
        
        a = ["https://w3id.org/pss/resource/r15_s6-a1-t1_78f3ef53b0b6fb384c4ba59ed9e7b418"]
       
        logger.info("Search done")
   
        fp_out = open(file_out, "w") 
        fp_out2 = open(file_out + "2", "w") 
   
        for i in range(0, len(m)):
            for ii in range(0, len(I[i])):
                if(ii != i):
                    fp_out.write(f"{entities_all[entities_to_analyse_ids[i]]}\t{entities_all[entities_to_analyse_ids[I[i][ii]]]}\t{1-D[i][ii]}\n")
                if(entities_all[entities_to_analyse_ids[i]] in a):
                    fp_out2.write(f"{entities_all[entities_to_analyse_ids[i]]}\t{entities_all[entities_to_analyse_ids[I[i][ii]]]}\t{1-D[i][ii]}\n")
            
    else:
        if(compute_bigrams):
            corpus_memory_friendly = CorpusBigrams(corpus_file, dictionary, bigrams, sep)
        else:
            corpus_memory_friendly = Corpus(corpus_file, dictionary, sep)
        corpora.MmCorpus.serialize(corpusMM_file, corpus_memory_friendly)
        logger.info("MM Corpus Serialized")
        
        tfidf = models.TfidfModel(corpus_memory_friendly, normalize=True)
        corpus_tfidf = tfidf[corpus_memory_friendly]
        logger.info("TF-IDF model computed")
        
        lsi = models.LsiModel(corpus_tfidf, id2word=dictionary, num_topics=num_of_topics)
        corpus_lsi = lsi[corpus_tfidf]
        corpora.MmCorpus.serialize(lsi_corpus_file, corpus_lsi)
        lsi.save(lsi_model_file)
        logger.info("LSI model saved")
        
