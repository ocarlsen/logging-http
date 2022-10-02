# Release Checklist

This document contains common steps when making a new release.

## Generate File

Run the following from your project directory to generate this checklist:

    project=$(basename $PWD)
    sed -e "s/\logging-http/${project}/" ~/Documents/Open\ Source/Release.md > Release.md
    open Release.md

<br>

## Release Items

* [x] Make changes
* [x] Unit tests
* [x] Manual tests
* [x] Build and verify

        mvn clean verify
        
* [x] Check [code coverage](./target/site/jacoco/index.html) report
	* May be [aggregate](./report/target/site/jacoco-aggregate/index.html) instead

* [x] Update these for versions, match `develop` branch instead of `main`:
	* [x] README
   	* [x] POM
   	* [x] Example Code 
   	* [x] Link(s) to example(s)
	* [x] .github/workflows/build.yml

* [x] Commit and push

        git add -u
        git commit -m "Preparing for 1.0.0 release"  # TODO: Factor out version
        git push  # develop

* [x] Verify in GitHub: [develop](https://github.com/ocarlsen/logging-http/tree/develop) branch

* [x] Confirm build running on GitHub [Actions](https://github.com/ocarlsen/logging-http/actions)

* [x] Check develop metrics in [Sonar Cloud](https://sonarcloud.io/dashboard?branch=develop&id=ocarlsen_logging-http)
* [x] Deploy snapshot to OSSRH

        mvn clean deploy

* [ ] Fix errors, if any

		[ERROR] Failed to execute goal org.sonatype.plugins:nexus-staging-maven-plugin:1.6.13:deploy (injected-nexus-deploy) on project mock-slf4j-impl: Failed to deploy artifacts: Could not transfer artifact com.ocarlsen.test:mock-slf4j-impl:jar:2.0.0-20220925.162838-1 from/to ossrh (https://s01.oss.sonatype.org/content/repositories/snapshots): Transfer failed for https://s01.oss.sonatype.org/content/repositories/snapshots/com/ocarlsen/test/mock-slf4j-impl/2.0.0-SNAPSHOT/mock-slf4j-impl-2.0.0-20220925.162838-1.jar 401 Unauthorized -> [Help 1]
		
	Check `~/.m2/settings.xml`:
	
		<settings>
		  <servers>
		    <server>
		      <id>ossrh</id>
		      <username>ocarlsen</username>
		      <password>LodmmdYC2Hp6%</password>
		    </server>
		  </servers>
		</settings>

* [x] Confirm in [Staging](https://s01.oss.sonatype.org/content/groups/staging/com/ocarlsen/) repo
	* https://s01.oss.sonatype.org/content/groups/staging/com/ocarlsen/logging/http/logging-http/1.0.0-SNAPSHOT/

* [x] Confirm in [Snapshots](https://s01.oss.sonatype.org/content/repositories/snapshots/com/ocarlsen/) repo
	* https://s01.oss.sonatype.org/content/repositories/snapshots/com/ocarlsen/logging/http/logging-http/1.0.0-SNAPSHOT/

* [x] Test in another project, e.g. `open-source-tester`

* [x] Make a dry-run release

    	mvn release:prepare -DdryRun=true
        
    To do non-interactively:
    
    	mvn -B release:prepare -DdryRun=true
    
* [x] Fix any problems.  (You may need to update XCode too.)

* [x] Prepare the release for real

        mvn release:clean    # Clean up from dry run
        mvn release:prepare

* [x] Confirm and clean up

        git tag  # List tags
        mvn release:clean

* [x] Set some variables

        # We'll use these below
        tag=$(git tag | tail -1)
        echo $tag

* [x] Check out main

        git co main

* [x] Merge from release commit without committing:

        git merge $tag --no-commit
        
    You should see a message like this:
    
        Automatic merge went well; stopped before committing as requested
        
* [x] Resolve conflicts, if any:

        git mergetool
        git clean -f

* [x] Update these for versions, and use `main` branch instead of `develop`:
    * [x] `src/main/doc/README.md`
    * [x] .github/workflows/build.yml

* [x] Confirm versions match:

        pom_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
        readme_version=$(grep '<version>.*</version>' README.md | sed -e 's/<version>//' -e 's/<\/version>//' | tr -d '[:space:]')
        if [[ "$pom_version" != "$readme_version" ]]; then
            echo "Versions don't match: $pom_version != $readme_version"
        else
            echo "Versions match"
        fi

* [x] Commit and push when ready:
    
        git add -u
        git difftool --cached
        git commit
        git push # main
        git push origin ${tag}
        
* [x] Confirm changes on GitHub [main](https://github.com/ocarlsen/logging-http/tree/main)
        
* [x] Confirm build running on GitHub [Actions](https://github.com/ocarlsen/logging-http/actions)

* [x] Check [SonarCloud](https://sonarcloud.io/dashboard?id=ocarlsen_logging-http) for quality metrics

* [x] Go go GitHub [Tags](https://github.com/ocarlsen/logging-http/tags) and make release from new tag

    * [x] Use tag as release name
    * [x] Use markdown with format like this

            # Release Notes
    
            ## Enhancements
            
            * Added GitHub Pages [site](https://ocarlsen.github.io/logging-http/)
    
            ## Issues Fixed
    
            * Using `ConcurrentHashMap` to prevent `ConcurrentModificationException`s

            ## Dependencies
            
            ### Compile
            
            ### Test

    * [x] Generate list of dependencies for Release Notes

            mvn -o dependency:list | grep ":.*:.*:.*" | grep -v "Finished at" | sed 's/^\[INFO\] *//' | sort -k 5 -k 1 -t ':'

    * [x] Add to Release Notes with separate sections for compile, test, etc. with formatting.

* [x] Confirm on [Releases](https://github.com/ocarlsen/logging-http/releases) page
        
* [x] Build and deploy artifacts to OSSRH

        mvn clean deploy -P release

      You may have to set this variable if you see (`gpg: signing failed: Inappropriate ioctl for device`): 
      
        GPG_TTY=$(tty)
        export GPG_TTY 
      
      You may also have to enter the password to sign the JAR: `QsiGzbu9ADu2`

* [x] Confirm in [Staging](https://s01.oss.sonatype.org/content/groups/staging/com/ocarlsen/) repo
	* https://s01.oss.sonatype.org/content/groups/staging/com/ocarlsen/logging/http/logging-http/1.0.0/

* [x] Confirm in [Releases](https://s01.oss.sonatype.org/content/repositories/releases/com/ocarlsen/) repo
	* https://s01.oss.sonatype.org/content/repositories/releases/com/ocarlsen/logging/http/logging-http/1.0.0/

* [x] Wait for [Maven Central](https://repo.maven.apache.org/maven2/com/ocarlsen/) to sync
	* https://repo.maven.apache.org/maven2/com/ocarlsen/logging/http/logging-http/1.0.0/

* [ ] Build and publish site

        mvn clean site-deploy
        
    (Will push directly to `gh-pages` branch)
        
* [x] Confirm site w/ latest version at [GitHub Pages](https://ocarlsen.github.io/logging-http/)

* [x] For multi-module project, may need to fix links.
    * Filed [Fix site](https://github.com/ocarlsen/logging-http/issues/9) issue



Yay!  You are done with the release.

<br>

## Develop

* [x] Check out `develop` to continue work:

        git co develop

* [x] Merge any changes from `main`:

        git difftool main

* [x] Confirm versions match with:

        pom_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
        readme_version=$(grep '<version>.*</version>' README.md | sed -e 's/<version>//' -e 's/<\/version>//' | tr -d '[:space:]')
        if [[ "$pom_version" != "$readme_version" ]]; then
            echo "Versions don't match: $pom_version != $readme_version"
        else
            echo "Versions match"
        fi
        
* [x] Commit, push

        git add -u
        git commit -m "Updating develop from main"
        git push # Release changes

* [x] Compare this document with [original](/Users/ocarlsen/Documents/Open\ Source/Release.md):

        vdiff Release.md ~/Documents/Open\ Source/Release.md

* [x] Move this document to `src/main/audit`:

        # TODO: Compute version from POM during release
        version=1.0.0
        mv Release.md src/main/audit/Release_${version}.md
        git add src/main/audit
        git commit -m "Release checklist for ${version} release"

Yay!  Now you are done with this document.



***


