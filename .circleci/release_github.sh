#!/bin/bash
set -e

uploadReleaseToGitHub() {
    git fetch --tags
    THIS_TAG=$(git describe --tags --abbrev=0)

    BODY="{
        \"tag_name\": \"$THIS_TAG\",
        \"target_commitish\": \"master\",
        \"name\": \"$THIS_TAG\",
        \"body\": \" \"
    }"

    # Create the release in GitHub and extract its id from the response
    RESPONSE_BODY=$(curl -s \
            -u ${CIRCLE_USERNAME}:${GITHUB_TOKEN} \
            --header "Accept: application/vnd.github.v3+json" \
            --header "Content-Type: application/json; charset=utf-8" \
            --request POST \
            --data "${BODY}" \
            https://api.github.com/repos/"${REPO_SLUG}"/releases)

    # Extract the upload_url value
    UPLOAD_URL=$(echo ${RESPONSE_BODY} | jq -r .upload_url)

    cp annotations/build/libs/annotations.jar .

    # Attach annotations
    ANNOTATIONS_UPLOAD_URL=$(echo ${UPLOAD_URL} | sed "s/{?name,label}/?name=annotations-${THIS_TAG}.jar/")
    curl -s \
        -u ${CIRCLE_USERNAME}:${GITHUB_TOKEN} \
        --header "Accept: application/vnd.github.v3+json" \
        --header "Content-Type: application/zip" \
        --data-binary "@annotations.jar" \
        --request POST \
        ${ANNOTATIONS_UPLOAD_URL}

    cp processor-java/build/libs/processor-java.jar .

    # Attach processor-java
    PROCESSOR_JAVA_UPLOAD_URL=$(echo ${UPLOAD_URL} | sed "s/{?name,label}/?name=processor-java-${THIS_TAG}.jar/")
    curl -s \
        -u ${CIRCLE_USERNAME}:${GITHUB_TOKEN} \
        --header "Accept: application/vnd.github.v3+json" \
        --header "Content-Type: application/zip" \
        --data-binary "@processor-java.jar" \
        --request POST \
        ${PROCESSOR_JAVA_UPLOAD_URL}

    echo "GitHub release complete."
}

uploadReleaseToGitHub
