# Release Checklist

This document contains common steps when making a new release.


## Variables

Set the following in your shell for the scripts below

    version=TODO
    next_version=$(echo ${version} | awk -F. -v OFS=. '{$NF += 1 ; print}')
    echo ${version}
    echo ${next_version}


<br>

## Generate File

Run the following from your project directory to generate this checklist:

    project=$(basename $PWD)
    sed -e "s/\logging-http/${project}/" ~/Documents/Open\ Source/Release.md > Release.md
    mkdir -p src/main/audit
    mv Release.md src/main/audit/Release_${version}.md
    git add !$
    open !$

<br>

## Release

* [ ] Make changes
* [ ] Unit tests
* [ ] Manual tests
* [ ] Build and verify

        mvn clean verify
        
* [ ] Check [code coverage](./target/site/jacoco/index.html) report
	* May be [aggregate](./report/target/site/jacoco-aggregate/index.html) instead

* [ ] Update these for versions, match `develop` branch instead of `main`:
	* [ ] README
   	* [ ] POM
   	* [ ] Example Code 
   	* [ ] Link(s) to example(s)
	* [ ] .github/workflows/build.yml

* [ ] Commit and push

        git add -u
        git commit -m "Preparing for ${version} release"
        git push  # develop

* [ ] Verify in GitHub: [develop](https://github.com/ocarlsen/${project}/tree/develop) branch

* [ ] Confirm build running on GitHub [Actions](https://github.com/ocarlsen/${project}/actions)

* [ ] Check develop metrics in [Sonar Cloud](https://sonarcloud.io/dashboard?branch=develop&id=ocarlsen_${project})
* [ ] Deploy snapshot to OSSRH

        mvn clean deploy

* [ ] Fix errors, if any

		[ERROR] Failed to execute goal org.sonatype.plugins:nexus-staging-maven-plugin:1.6.13:deploy (injected-nexus-deploy) on project mock-slf4j-impl: Failed to deploy artifacts: Could not transfer artifact com.ocarlsen.test:mock-slf4j-impl:jar:2.0.0-20220925.162838-1 from/to ossrh (https://s01.oss.sonatype.org/content/repositories/snapshots): Transfer failed for https://s01.oss.sonatype.org/content/repositories/snapshots/com/ocarlsen/test/mock-slf4j-impl/2.0.0-SNAPSHOT/mock-slf4j-impl-2.0.0-20220925.162838-1.jar 401 Unauthorized -> [Help 1]
		
	Check credentials in `~/.m2/settings.xml`.
	
* [ ] Confirm in [Staging](https://s01.oss.sonatype.org/content/groups/staging/com/ocarlsen/) repo
	* 

* [ ] Confirm in [Snapshots](https://s01.oss.sonatype.org/content/repositories/snapshots/com/ocarlsen/) repo
	* 

* [ ] Test in another project, e.g. `open-source-tester`

* [ ] Make a dry-run release

        mvn release:prepare \
            -DreleaseVersion=${version} \
            -DdevelopmentVersion=${next_version}-SNAPSHOT \
            -Dtag="v${version}" \
            -DdryRun=true

* [ ] Fix any problems.  (You may need to update XCode too.)

* [ ] Prepare the release for real

        mvn release:clean    # Clean up from dry run
        mvn release:prepare \
            -DreleaseVersion=${version} \
            -DdevelopmentVersion=${next_version}-SNAPSHOT \
            -Dtag="v${version}" 

* [ ] Confirm and clean up

        git tag  # List tags
        mvn release:clean

* [ ] Set some variables

        # We'll use these below
        tag=$(git tag | tail -1)
        echo $tag

* [ ] Check out main

        git co main

* [ ] Merge from release commit without committing:

        git merge "v${version}" --no-commit
        
    You should see a message like this:
    
        Automatic merge went well; stopped before committing as requested
        
* [ ] Resolve conflicts, if any:

        git mergetool
        # Do not Release.md! git clean -f

* [ ] Update these for versions, and use `main` branch instead of `develop`:
    * [ ] `src/main/doc/README.md`
    * [ ] .github/workflows/build.yml

* [ ] Confirm versions match:

        pom_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
        readme_version=$(grep '<version>.*</version>' README.md | sed -e 's/<version>//' -e 's/<\/version>//' | tr -d '[:space:]')
        if [[ "$pom_version" != "$readme_version" ]]; then
            echo "Versions don't match: $pom_version != $readme_version"
        else
            echo "Versions match"
        fi

* [ ] Commit and push when ready:
    
        git add -u
        git difftool --cached
        git commit
        git push # main
        git push origin ${tag}
        
* [ ] Confirm changes on GitHub [main](https://github.com/ocarlsen/${project}/tree/main)
        
* [ ] Confirm build running on GitHub [Actions](https://github.com/ocarlsen/${project}/actions)

* [ ] Check [SonarCloud](https://sonarcloud.io/dashboard?id=ocarlsen_${project}) for quality metrics

* [ ] Go go GitHub [Tags](https://github.com/ocarlsen/${project}/tags) and make release from new tag

    * [ ] Use tag as release name
    * [ ] Use markdown with format like this

            # Release Notes
    
            ## Enhancements
            
            * Added GitHub Pages [site](https://ocarlsen.github.io/${project}/)
    
            ## Issues Fixed
    
            * Using `ConcurrentHashMap` to prevent `ConcurrentModificationException`s

            ## Dependencies
            
            ### Compile
            
            ### Test

    * [ ] Generate list of dependencies for Release Notes

            mvn dependency:list | grep ":.*:.*:.*" | grep -v "Finished at" | sed 's/^\[INFO\] *//' | sort -k 5 -k 1 -t ':'

    * [ ] Add to Release Notes with separate sections for compile, test, etc. with formatting.

* [ ] Confirm on [Releases](https://github.com/ocarlsen/${project}/releases) page
    *         
* [ ] Build and deploy artifacts to OSSRH

        mvn clean deploy -P release

      You may have to set this variable if you see (`gpg: signing failed: Inappropriate ioctl for device`): 
      
        GPG_TTY=$(tty)
        export GPG_TTY 
      
      You may also have to enter the password to sign the JAR: `QsiGzbu9ADu2`

* [ ] Confirm in [Staging](https://s01.oss.sonatype.org/content/groups/staging/com/ocarlsen/) repo
	* 

* [ ] Confirm in [Releases](https://s01.oss.sonatype.org/content/repositories/releases/com/ocarlsen/) repo
	* 

* [ ] Wait for [Maven Central](https://repo.maven.apache.org/maven2/com/ocarlsen/) to sync
	* 

## Site

* [x] Test building the site

        mvn clean site
    
    It will build to `target/site`:

        open target/site/index.html
    
* [ ] Check no links are broken.  For example:

    * License
    * Project Summary, "Java Version" field

* [ ] Build and publish site

        mvn clean site-deploy
        
    (Will push directly to `gh-pages` branch.)  

    For multi-module builds, you may have to follow steps in [Pages](/Users/ocarlsen/Documents/Open\ Source/Pages.md#multi-module) document.

* [ ] Confirm site w/ latest version at [GitHub Pages](https://ocarlsen.github.io/${project}/)

Yay!  You are done with the release.

<br>

## Develop

* [ ] Check out `develop` to continue work:

        git co develop

* [ ] Merge any changes from `main`:

        git difftool main

* [ ] Confirm versions match with:

        pom_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
        readme_version=$(grep '<version>.*</version>' README.md | sed -e 's/<version>//' -e 's/<\/version>//' | tr -d '[:space:]')
        if [[ "$pom_version" != "$readme_version" ]]; then
            echo "Versions don't match: $pom_version != $readme_version"
        else
            echo "Versions match"
        fi
        
* [ ] Commit, push

        git add -u
        git commit -m "Updating develop to match main"
        git push # Includes Release changes from earlier

* [ ] Compare this document with [original](/Users/ocarlsen/Documents/Open\ Source/Release.md):

        vdiff src/main/audit/Release_${version}.md ~/Documents/Open\ Source/Release.md

* [ ] Commit this document to `src/main/audit`:

        git add src/main/audit
        git commit -m "Release checklist for ${version} release"

Yay!  Now you are done with this document.



***


