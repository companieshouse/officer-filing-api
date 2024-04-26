artifact_name       := officer-filing-api
version             := unversioned

.PHONY: clean
clean:
	mvn clean
	rm -f ./$(artifact_name).jar
	rm -f ./$(artifact_name)-*.zip
	rm -rf ./build-*
	rm -rf ./build.log-*

.PHONY: submodules
submodules:
	test -f ./api-enumerations/constants.yml || git submodule update --init --recursive -- src/main/resources/api-enumerations

.PHONY: test
test: test-unit test-integration

.PHONY: test-unit
test-unit: clean
	mvn test -Dskip.integration.tests=true

.PHONY: test-integration
test-integration: clean
	mvn verify -Dskip.unit.tests=true

.PHONY: verify
verify: test-integration

.PHONY: package
package:
ifndef version
	$(error No version given. Aborting)
endif
	mvn versions:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	$(info Packaging version: $(version))
	@test -s ./$(artifact_name).jar || { echo "ERROR: Service JAR not found"; exit 1; }
	$(eval tmpdir:=$(shell mktemp -d build-XXXXXXXXXX))
	cp ./start.sh $(tmpdir)
	cp ./routes.yaml $(tmpdir)
	cp ./$(artifact_name).jar $(tmpdir)/$(artifact_name).jar
	cd $(tmpdir); zip -r ../$(artifact_name)-$(version).zip *
	rm -rf $(tmpdir)

.PHONY: build
build: clean submodules
	mvn versions:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	mvn package -Dmaven.test.skip=true
	cp ./target/$(artifact_name)-$(version).jar ./$(artifact_name).jar

.PHONY: dist
dist: clean build submodules package coverage

.PHONY: coverage
coverage:
	mvn verify

.PHONY: sonar
sonar: security-check
	mvn sonar:sonar

.PHONY: sonar-pr-analysis
sonar-pr-analysis:
	mvn sonar:sonar -P sonar-pr-analysis

.PHONY: security-check
security-check:
	mvn org.owasp:dependency-check-maven:check -DassemblyAnalyzerEnabled=false -DfailBuildOnCVSS=4 -DsuppressionFiles=suppressed-security-checks.xml
	mvn org.owasp:dependency-check-maven:update-only
	mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=4 -DassemblyAnalyzerEnabled=false
