You need to provide path to the XML file and name of output file, also specify list of child elements which need to be extracted from provided XML
# XMLExtractor

How to run:
1. docker build -t xml-extractor .
2. docker run -p 8080:8080 xml-extractor
3. Open http://localhost:8080/swagger-ui/index.html

4. if you want to skip tests you need to add -DskipTests for mvn build: mvn clean install -DskipTests
