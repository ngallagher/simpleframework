mvn clean

find . -name FileIndexerTest.java | xargs rm
find . -name PayloadTest.java | xargs rm
find . -name UploadTest.java | xargs rm 
find . -name SessionTest.java | xargs rm 
find . -name SecureSoakTest.java | xargs rm 
find . -name PostTest.java | xargs rm 
find . -name SoakTest.java | xargs rm 
find . -name QueryTest.java | xargs rm 
find . -name ReactorProcessorTest.java | xargs rm 
find . -name FormTest.java | xargs rm 
find . -name RequestTest.java | xargs rm 
find . -name PartListConsumerTest.java | xargs rm 
find . -name ContentConsumerTest.java | xargs rm 
find . -name SecureQueryTest.java | xargs rm 
find . -name DistributorTest.java | xargs rm 
find . -name EchoTest.java | xargs rm 

mkdir -p maven/org/simpleframework/simple/@core.version@
scp -r -v maven niallg,simpleweb@web.sourceforge.net:/home/groups/s/si/simpleweb/htdocs/.
#mvn -DcreateChecksum=true install
mvn deploy
svn update
