def call(body) {

  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('kaniko') {
    sh '''
      REGISTRY="harbor.localhost.com/lab"
      REPOSITORY=${JOB_NAME%/*}
      TAG=""

      if [ $(echo $GIT_BRANCH | grep ^develop$) ]; then
        TAG="dev-${GIT_COMMIT:0:10}"
      elif [ $(echo $GIT_BRANCH | grep -E "^(release-.*)|(hotfix-.*)") ]; then
        TAG="${GIT_BRANCH#*-}-${GIT_COMMIT:0:10}"
      elif [ $(echo $GIT_BRANCH | grep -E "v[0-9]\\.[0-9]{1,2}\\.[0-9]{1,3}$") ]; then
        TAG="${GIT_BRANCH}"
      fi

      DESTINATION="${REGISTRY}/${REPOSITORY}:${TAG}"

      /kaniko/executor \
        --insecure \
        --destination "${DESTINATION}" \
        --context $(pwd)
    '''
  }
}
