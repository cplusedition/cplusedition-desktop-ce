scriptsdir=$(cd $(dirname $0) && pwd)
topdir=$(cd ${scriptsdir}/.. && pwd)
rootdir=ROOT
electron=${topdir}/bin/electron/electron
if [ ! -e ${electron} ]; then
  echo "!!! bin/electron/electron executable not found, abort"
  exit 1
fi
[ -x ${electron} ] || chmod u+x "${electron}"
# !!!
#- Password file for the app keystore at ROOT/etc/.keystore, require only at startup.
#- The keystore is used to store private keys for backups, ... etc.
#- Make sure to change the default password in the file. You have to remove any
#  existing keystore file in order to create a new keystore with the new password.
#- Make sure it point to a file in an encrypted volume and only readable by owner.
#- Use "-" to read the password from stdin.
# !!!
passfile="$(pwd)/${rootdir}/changeit.txt"
echo "# topdir=${topdir}"
pushd "$rootdir" > /dev/null
if [ -e "run/.server" ]; then
  rm -f "run/.server"
  sleep 1
fi
classpath=$(ls -1d lib/*.jar | perl -e 'my @a=<>; chomp @a; print(join(":", @a))')
[ -d "run" ] || mkdir -p "run"
cat "$passfile" | java -classpath ${classpath} \
  sf/andrians/cplusedition/war/StandaloneServer . $@ &
while [ ! -e "run/.server" ]; do sleep 1; done
popd > /dev/null
sleep 1
pushd "${rootdir}/assets/js" > /dev/null
${electron} -D $@ .
popd > /dev/null
bash "${scriptsdir}/shutdown.sh"
