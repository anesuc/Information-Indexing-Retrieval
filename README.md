# Information-Retrieval

Contributors:  
Anesu Chiodze  
Shuijia Zhuo  

How to compile and run:

Do "javac *.java" under src folder to compile the source code.


Type 
java search -BM25 -q <querylabel> -n <number-results> -l <lexicon> -i <invlists>  -m <map> -d <latimefile> -t <type-of-summary(1 or 2)> [-s <stoplistfile>] <queryterm-1> [<queryterm-2>...<queryterm-N>]
to search for the result. 

-BM25 specifies that the BM25 similarity function is to be used.
-q <query-label> is an integer that identifies the current query.
-n <num-results> is an integer number specifying the number of top-ranked documents that should be returned as an answer.
-l <lexicon> and -i <invlists> are the inverted index lexicon and inverted list files; and -m <map> is the mapping table from internal document numbers to actual document identifiers.
-d <latimesfile> is the latimes documents which for getting the document text for summary. For example, latimes-100 in local dist or latimes on server which is located at /home/inforet/a1/latimes.
-t <type-of-summary> is the type of summary.1 is based on query-biased information, taking the user's query into account. 2 is include evidence other than the user's current query
-s <stoplistfile> is an option that apply stopword into program.
<queryterm> is the term for searching. Can add more words after follow by " ".
 


