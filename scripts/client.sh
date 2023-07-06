scriptsdir=$(cd $(dirname $0) && pwd)
topdir=$(cd ${scriptsdir}/.. && pwd)
electron=${topdir}/bin/electron/electron
if [ ! -e ${electron} ]; then
  echo "!!! bin/electron/electron executable not found, abort"
  exit 1
fi
[ -x ${electron} ] || chmod u+x "${electron}"
cd ROOT/assets/js
electron -D --w=$1 --h=$2 . &
