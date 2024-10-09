default: install

install:
	mvn clean install

run:
	java -jar ponomar.jar

test:
	perl src/scripts/Perl/paschalion.pl
	perl src/scripts/Perl/test_lj.pl
