JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Blockchain.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
