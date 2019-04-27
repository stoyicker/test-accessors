#!/bin/bash
set -e

uploadReleaseToGitHub() {
    git fetch --tags
    ARTIFACT_VERSION=$(git rev-list --count HEAD)
    LAST_TAG=$(git describe --tags --abbrev=0)
    THIS_RELEASE=$(git rev-parse --short HEAD)
    local IFS=$'\n'
    RELEASE_NOTES_ARRAY=($(git log --format=%B ${LAST_TAG}..${THIS_RELEASE} | tr -d '\r'))
    { for i in "${RELEASE_NOTES_ARRAY[@]}"
    do
        RELEASE_NOTES="$RELEASE_NOTES\\n$i"
    done
    }

    BODY="{
        \"tag_name\": \"$ARTIFACT_VERSION\",
        \"target_commitish\": \"master\",
        \"name\": \"$ARTIFACT_VERSION\",
        \"body\": \" \"
    }"

    # Create the release in GitHub and extract its id from the response
    RESPONSE_BODY=$(curl -s \
            -u ${REPO_USER}:${GITHUB_TOKEN} \
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

    # Attach processor-java
    UPLOAD_URL=$(echo ${UPLOAD_URL} | sed "s/{?name,label}/?name=annotations-${ARTIFACT_VERSION}.jar/")
    curl -s \
        -u ${REPO_USER}:${GITHUB_TOKEN} \
        --header "Accept: application/vnd.github.v3+json" \
        --header "Content-Type: application/zip" \
        --data-binary "@annotations.jar" \
        --request POST \
        ${UPLOAD_URL}

    cp processor-java/build/libs/processor-java.jar .

    # Attach processor-java
    UPLOAD_URL=$(echo ${UPLOAD_URL} | sed "s/{?name,label}/?name=processor-java-${ARTIFACT_VERSION}.jar/")
    curl -s \
        -u ${REPO_USER}:${GITHUB_TOKEN} \
        --header "Accept: application/vnd.github.v3+json" \
        --header "Content-Type: application/zip" \
        --data-binary "@processor-java.jar" \
        --request POST \
        ${UPLOAD_URL}

    RELEASE_NOTES_BODY="\\n**CHANGELOG**:\\n$RELEASE_NOTES"

    jq -n --arg msg "$(git log -n 1 --format=oneline | grep -o ' .\+')" \   '{body: { message: $msg }}' > changelog.txt

    # Attach the release notes
    curl -s \
        -u ${REPO_USER}:${GITHUB_TOKEN} \
        --header "Accept: application/vnd.github.v3+json" \
        --header "Content-Type: application/json; charset=utf-8" \
        --request PATCH \
        --data @changelog.txt \
        https://api.github.com/repos/"${REPO_SLUG}"/releases/${RELEASE_ID}

    echo "GitHub release complete."
}

uploadReleaseToGitHub
