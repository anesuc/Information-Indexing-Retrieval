# Information-Retrieval

Student Name&ID:
Anesu Chiodze s3542943
Shuijia Zhuo s3384039

How to compile and run:

Do "javac *.java" under src folder to compile the source code.


Type 
"java index -s <stoplistfile> -p <sourcefile>"
to index the sourcefile. 

-s <stoplistfile> is an option that apply stopword into program.
-p is an option that print the result on screen.
 
 
Type 
"java search lexicon invlist map word"
to search word after index.

You can add more word after follow by " ". 
For example, 
"java search lexicon invlist map word1 word2 word3" 
etc..


