GPG_TTY=$(tty)
export GPG_TTY
plannedTag=v$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
if git show-ref --tags ${plannedTag} --quiet; then
  echo "${plannedTag} tag already exists, update your POM version"
  exit 1
fi
mvn deploy -Possrh
echo "Adding tag: ${plannedTag}"
git tag "${plannedTag}"

