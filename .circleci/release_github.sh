#!/bin/bash
set -e

json_escape() {
    printf '%s' "$1" | python -c 'import json,sys; print(json.dumps(sys.stdin.read()))'
}

uploadReleaseToGitHub() {
    git fetch --tags
    THIS_TAG=$(git describe --tags --abbrev=0)
    PREVIOUS_TAG=$(git describe --tags --abbrev=0 ${THIS_TAG}^)
    local IFS=$'\n'
    RELEASE_NOTES_ARRAY=($(git log --format=%B ${PREVIOUS_TAG}..${THIS_TAG} | tr -d '\r'))
    { for i in "${RELEASE_NOTES_ARRAY[@]}"
    do
        RELEASE_NOTES="$RELEASE_NOTES$i\n"
    done
    }

    BODY="{
        \"tag_name\": \"$THIS_TAG\",
        \"target_commitish\": \"master\",
        \"name\": \"$THIS_TAG\",
        \"body\": \" \"
    }"

    # Create the release in GitHub and extract its id from the response
    RESPONSE_BODY=$(curl -s \
            -v \
            -u ${CIRCLE_USERNAME}:${GITHUB_TOKEN} \
            --header "Accept: application/vnd.github.v3+json" \
            --header "Content-Type: application/json; charset=utf-8" \
            --request POST \
            --data "${BODY}" \
            https://api.github.com/repos/"${REPO_SLUG}"/releases)

    # Extract the upload_url value
    UPLOAD_URL=$(echo ${RESPONSE_BODY} | jq -r .upload_url)
    # And the id for later use
    RELEASE_ID=$(echo ${RESPONSE_BODY} | jq -r .id)

    cp annotations/build/libs/annotations.jar .

    # Attach annotations
    ANNOTATIONS_UPLOAD_URL=$(echo ${UPLOAD_URL} | sed "s/{?name,label}/?name=annotations-${THIS_TAG}.jar/")
    curl -s \
        -v \
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
        -v \
        -u ${CIRCLE_USERNAME}:${GITHUB_TOKEN} \
        --header "Accept: application/vnd.github.v3+json" \
        --header "Content-Type: application/zip" \
        --data-binary "@processor-java.jar" \
        --request POST \
        ${PROCESSOR_JAVA_UPLOAD_URL}

    RELEASE_NOTES_BODY="\n**CHANGELOG**:\n$RELEASE_NOTES"

    echo -e $RELEASE_NOTES_BODY > changelog.txt

    json_escape $(cat changelog.txt) > changelog.txt

    BODY="{
        \"body\": $(cat changelog.txt)
    }"

    echo $BODY > changelog.json

    # Attach the release notes
    curl -s \
        -v \
        -u ${CIRCLE_USERNAME}:${GITHUB_TOKEN} \
        --header "Accept: application/vnd.github.v3+json" \
        --header "Content-Type: application/json; charset=utf-8" \
        --request PATCH \
        --data @changelog.json \
        https://api.github.com/repos/"${REPO_SLUG}"/releases/${RELEASE_ID}

    echo "GitHub release complete."
}

uploadReleaseToGitHub
